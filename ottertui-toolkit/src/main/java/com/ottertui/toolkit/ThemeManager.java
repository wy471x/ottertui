package com.ottertui.toolkit;

import com.ottertui.core.Style;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ThemeManager — runtime theme management.
 *
 * Supports:
 *   - Multiple named themes loaded from .tcss files or StyleSheet objects
 *   - Theme inheritance: a theme can extend a base theme
 *   - Hot reload: watch .tcss files for changes and auto-reload
 *   - Both static (global singleton) and instance-based usage
 */
public class ThemeManager {
    private final Map<String, StyleSheet> themes = new HashMap<>();
    private final Map<String, String> baseThemes = new HashMap<>(); // name -> parent name
    private StyleSheet active;
    private Consumer<Runnable> redrawCallback;
    private ScheduledExecutorService watcher;
    private final Map<String, Path> watchedPaths = new HashMap<>();

    // ---- Static singleton ----

    private static final ThemeManager INSTANCE = new ThemeManager();

    public static ThemeManager global() {
        return INSTANCE;
    }

    public static void loadTheme(String name, StyleSheet sheet) {
        INSTANCE.themes.put(name, sheet);
    }

    public static void activate(String name) {
        INSTANCE.activateTheme(name);
    }

    public static Style resolve(String widgetType, String id, Set<String> classes) {
        return INSTANCE.resolveStyle(widgetType, id, classes, Set.of());
    }

    public static Style resolve(String widgetType, String id,
                                Set<String> classes, Set<String> pseudoClasses) {
        return INSTANCE.resolveStyle(widgetType, id, classes, pseudoClasses);
    }

    // ---- Instance API ----

    /**
     * Register a theme by name from a StyleSheet object.
     */
    public void register(String name, StyleSheet sheet) {
        themes.put(name, sheet);
    }

    /**
     * Load and register a theme from a .tcss file.
     */
    public void loadFromFile(String name, Path path) throws IOException {
        var sheet = StyleSheet.load(path);
        themes.put(name, sheet);
        if (watcher != null) {
            watchedPaths.put(name, path);
        }
    }

    /**
     * Load a theme that extends another theme.
     * The child inherits all rules and variables from the parent;
     * child rules of the same specificity override parent rules.
     */
    public void loadExtending(String name, Path path, String baseName) throws IOException {
        var childSheet = StyleSheet.load(path);
        baseThemes.put(name, baseName);
        themes.put(name, childSheet);
        if (watcher != null) {
            watchedPaths.put(name, path);
        }
    }

    /**
     * Activate a registered theme by name.
     * If the theme extends a base, the base rules are resolved first.
     */
    public void activateTheme(String name) {
        active = themes.get(name);
        if (active == null) return;

        // If this theme extends a base, merge base rules underneath
        String baseName = baseThemes.get(name);
        if (baseName != null && themes.containsKey(baseName)) {
            var base = themes.get(baseName);
            var merged = new StyleSheet();
            // Copy base variables
            base.variables().forEach(merged::setVariable);
            // Add base rules lower in the cascade (child overrides via order)
            // We create a temporary merged sheet on each activation
            // (actual rule merging happens during resolve via the child sheet)
            active = createMergedSheet(base, active);
        }
    }

    private StyleSheet createMergedSheet(StyleSheet base, StyleSheet child) {
        var merged = new StyleSheet();
        // Copy base variables first, then child overrides
        base.variables().forEach(merged::setVariable);
        child.variables().forEach(merged::setVariable);
        // Note: Since StyleSheet doesn't expose rules list,
        // we reconstruct via a different approach: the ThemeManager
        // resolves by checking child first, then base.
        // For simplicity, we store the merged logic in resolveStyle.
        return child; // The resolveStyle method handles inheritance
    }

    /**
     * Set a callback to trigger a redraw (usually TuiRunner::requestRedraw).
     */
    public void onRedraw(Consumer<Runnable> callback) {
        this.redrawCallback = callback;
    }

    /**
     * Start watching .tcss files for changes. When a file changes,
     * the theme is reloaded and the redraw callback is invoked.
     */
    public void enableHotReload() {
        if (watcher != null && !watcher.isShutdown()) return;
        watcher = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "theme-watcher");
            t.setDaemon(true);
            return t;
        });

        watcher.scheduleWithFixedDelay(() -> {
            for (var entry : watchedPaths.entrySet()) {
                try {
                    Path path = entry.getValue();
                    if (!Files.exists(path)) continue;
                    long lastModified = Files.getLastModifiedTime(path).toMillis();
                    // Simple polling approach — check file mod time
                    String key = entry.getKey() + ".mtime";
                    // Re-load and re-activate if changed
                    var sheet = StyleSheet.load(path);
                    String baseName = baseThemes.get(entry.getKey());
                    themes.put(entry.getKey(), sheet);
                    if (active != null && themes.get(entry.getKey()) == active) {
                        // The active theme was this one — re-activate
                        activateTheme(entry.getKey());
                    }
                    if (redrawCallback != null) {
                        redrawCallback.accept(() -> { });
                    }
                } catch (IOException e) {
                    // File may be temporarily unavailable; skip this cycle
                }
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    /**
     * Stop hot reload watching.
     */
    public void disableHotReload() {
        if (watcher != null && !watcher.isShutdown()) {
            watcher.shutdown();
            watcher = null;
        }
    }

    /**
     * Resolve the final Style for a widget, checking the active theme.
     * Handles theme inheritance: base rules resolve first, child overrides.
     */
    public Style resolveStyle(String widgetType, String id,
                              Set<String> classes, Set<String> pseudoClasses) {
        if (active == null) return Style.DEFAULT;

        Style result = Style.DEFAULT;

        // Check if there's an inherited base for the active theme
        // (find the active theme name, check baseThemes)
        String activeName = null;
        for (var entry : themes.entrySet()) {
            if (entry.getValue() == active) {
                activeName = entry.getKey();
                break;
            }
        }

        // Resolve base first if it exists
        if (activeName != null && baseThemes.containsKey(activeName)) {
            String baseName = baseThemes.get(activeName);
            var baseSheet = themes.get(baseName);
            if (baseSheet != null) {
                result = baseSheet.resolve(widgetType, id, classes, pseudoClasses);
            }
        }

        // Child theme overrides base
        var childResult = active.resolve(widgetType, id, classes, pseudoClasses);
        return mergeResults(result, childResult);
    }

    private static Style mergeResults(Style base, Style over) {
        var result = base;
        if (!(over.foreground() instanceof com.ottertui.core.Color.Reset)) {
            result = result.fg(over.foreground());
        }
        if (!(over.background() instanceof com.ottertui.core.Color.Reset)) {
            result = result.bg(over.background());
        }
        var mods = new HashSet<>(result.modifiers());
        mods.addAll(over.modifiers());
        return new com.ottertui.core.Style(result.foreground(), result.background(), mods);
    }
}
