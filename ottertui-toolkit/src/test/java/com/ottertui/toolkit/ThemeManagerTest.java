package com.ottertui.toolkit;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ThemeManagerTest {

    @BeforeEach
    void setUp() {
        ThemeManager.activate(null);
    }

    @Test
    @DisplayName("resolve without active theme returns DEFAULT")
    void resolveNoActive() {
        Style s = ThemeManager.resolve("Button", "x", Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("loadTheme and activate")
    void loadAndActivate() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "red"));

        ThemeManager.loadTheme("dark", sheet);
        ThemeManager.activate("dark");

        Style s = ThemeManager.resolve("Button", "x", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("activate non-existent theme resolves to DEFAULT")
    void activateNonExistent() {
        ThemeManager.activate("nonexistent");
        Style s = ThemeManager.resolve("Button", "x", Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("switch between themes")
    void switchThemes() {
        StyleSheet dark = new StyleSheet();
        dark.addRule(Selector.universal(), Map.of("background", "black"));
        StyleSheet light = new StyleSheet();
        light.addRule(Selector.universal(), Map.of("background", "white"));

        ThemeManager.loadTheme("dark", dark);
        ThemeManager.loadTheme("light", light);

        ThemeManager.activate("dark");
        assertEquals(Color.BLACK, ThemeManager.resolve("x", "y", Set.of()).background());

        ThemeManager.activate("light");
        assertEquals(Color.WHITE, ThemeManager.resolve("x", "y", Set.of()).background());
    }

    @Test
    @DisplayName("resolve with pseudo-classes")
    void resolveWithPseudoClasses() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.pseudo("hover"), Map.of("color", "cyan"));

        ThemeManager.loadTheme("test", sheet);
        ThemeManager.activate("test");

        Style s = ThemeManager.resolve("Button", "x", Set.of(), Set.of("hover"));
        assertEquals(Color.CYAN, s.foreground());
    }

    @Test
    @DisplayName("instance register and activate")
    void instanceRegisterAndActivate() {
        ThemeManager tm = new ThemeManager();
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "magenta"));
        tm.register("test", sheet);
        tm.activateTheme("test");

        Style s = tm.resolveStyle("x", "y", Set.of(), Set.of());
        assertEquals(Color.MAGENTA, s.foreground());
    }

    @Test
    @DisplayName("instance activate non-existent does not throw")
    void instanceActivateNonExistent() {
        ThemeManager tm = new ThemeManager();
        tm.activateTheme("nonexistent");
        Style s = tm.resolveStyle("x", "y", Set.of(), Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("disable hot reload when not enabled does not throw")
    void disableHotReloadWhenNotEnabled() {
        ThemeManager tm = new ThemeManager();
        assertDoesNotThrow(tm::disableHotReload);
    }

    @Test
    @DisplayName("empty global resolves to DEFAULT")
    void emptyGlobalResolve() {
        // After setUp activates null, global resolve should return DEFAULT
        Style s = ThemeManager.resolve("x", "y", Set.of(), Set.of("hover"));
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("instance resolveStyle with no active theme")
    void instanceResolveStyleNoActive() {
        ThemeManager tm = new ThemeManager();
        Style s = tm.resolveStyle("x", "y", Set.of(), Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("global method singleton returns same instance")
    void globalSingleton() {
        assertSame(ThemeManager.global(), ThemeManager.global());
    }

    @Test
    @DisplayName("loadFromFile loads theme from .tcss file")
    void loadFromFile(@TempDir Path tempDir) throws IOException {
        Path tcssFile = tempDir.resolve("test.tcss");
        Files.writeString(tcssFile, "Button { color: red; }");

        ThemeManager tm = new ThemeManager();
        tm.loadFromFile("test", tcssFile);
        tm.activateTheme("test");

        Style s = tm.resolveStyle("Button", "x", Set.of(), Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("loadExtending merges base and child themes")
    void loadExtendingMerges(@TempDir Path tempDir) throws IOException {
        Path baseFile = tempDir.resolve("base.tcss");
        Files.writeString(baseFile, ":root { --bg: black; }\n* { background: var(--bg); }");
        Path childFile = tempDir.resolve("child.tcss");
        Files.writeString(childFile, "* { color: white; }");

        ThemeManager tm = new ThemeManager();
        tm.loadFromFile("base", baseFile);
        tm.loadExtending("child", childFile, "base");
        tm.activateTheme("child");

        Style s = tm.resolveStyle("x", "y", Set.of(), Set.of());
        assertEquals(Color.WHITE, s.foreground());
        assertEquals(Color.BLACK, s.background());
    }

    @Test
    @DisplayName("activateTheme falls back to base when child has no matching rule")
    void activateThemeBaseFallback(@TempDir Path tempDir) throws IOException {
        Path baseFile = tempDir.resolve("base.tcss");
        Files.writeString(baseFile, "* { color: green; }");
        Path childFile = tempDir.resolve("child.tcss");
        Files.writeString(childFile, "* { background: blue; }");

        ThemeManager tm = new ThemeManager();
        tm.loadFromFile("base", baseFile);
        tm.loadExtending("child", childFile, "base");
        tm.activateTheme("child");

        Style s = tm.resolveStyle("x", "y", Set.of(), Set.of());
        assertEquals(Color.GREEN, s.foreground());
        assertEquals(Color.BLUE, s.background());
    }

    @Test
    @DisplayName("disableHotReload after enableHotReload")
    void disableHotReloadAfterEnable() {
        ThemeManager tm = new ThemeManager();
        tm.enableHotReload();
        tm.disableHotReload();
        // Second call should be safe
        assertDoesNotThrow(tm::disableHotReload);
    }
}
