package com.ottertui.examples.apps;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.toolkit.Selector;
import com.ottertui.toolkit.StyleSheet;
import com.ottertui.toolkit.ThemeManager;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Demonstrates the CSS styling system and theme engine.
 *
 * Loads themes from .tcss files and applies them to styled UI components.
 * Press 'd' for dark theme, 'l' for light theme, 'q' to quit.
 */
public class ThemeExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) {
            System.err.println("Error: No terminal detected.");
            System.exit(1);
        }

        // Load themes and activate dark by default
        loadThemes();
        ThemeManager.global().activateTheme("dark");

        var root = new ThemedDashboard();
        var runner = new TuiRunner(backend, root);

        // Wire up ThemeManager redraw callback
        ThemeManager.global().onRedraw(r -> runner.requestRedraw());

        // Enable hot reload during development
        // ThemeManager.global().enableHotReload();

        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'd',
            () -> { ThemeManager.global().activateTheme("dark"); runner.requestRedraw(); });
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'l',
            () -> { ThemeManager.global().activateTheme("light"); runner.requestRedraw(); });

        runner.run();
    }

    private static void loadThemes() {
        try {
            var darkUrl = ThemeExample.class.getResource("/themes/dark.tcss");
            var lightUrl = ThemeExample.class.getResource("/themes/light.tcss");
            if (darkUrl != null) {
                ThemeManager.global().loadFromFile("dark", Path.of(darkUrl.toURI()));
            }
            if (lightUrl != null) {
                ThemeManager.global().loadFromFile("light", Path.of(lightUrl.toURI()));
            }
        } catch (IOException | URISyntaxException e) {
            // Fall back to programmatic theme definitions
            loadFallbackThemes();
        }
    }

    private static void loadFallbackThemes() {
        // Dark theme (programmatic fallback)
        var dark = new StyleSheet();
        dark.setVariable("--primary", "#5ea1ff");
        dark.setVariable("--bg", "#1a1a2e");
        dark.setVariable("--bg-card", "#16213e");
        dark.setVariable("--text", "#e0e0e0");
        dark.setVariable("--text-dim", "#8888a0");
        dark.setVariable("--border", "#4a4a6a");
        dark.setVariable("--danger", "#ff5e5e");
        dark.setVariable("--success", "#5eff6c");

        dark.addRule(Selector.type("Screen"), Map.of("background", "var(--bg)", "color", "var(--text)"));
        dark.addRule(Selector.clazz("panel"), Map.of("background", "var(--bg-card)"));
        dark.addRule(Selector.clazz("title"), Map.of("color", "var(--primary)", "bold", "true"));
        dark.addRule(Selector.clazz("sidebar"), Map.of("background", "#12122a"));
        dark.addRule(Selector.clazz("status"), Map.of("background", "var(--primary)", "color", "black", "bold", "true"));
        dark.addRule(Selector.clazz("metric-ok"), Map.of("color", "var(--success)", "bold", "true"));
        dark.addRule(Selector.clazz("metric-warn"), Map.of("color", "yellow", "bold", "true"));
        dark.addRule(Selector.clazz("metric-danger"), Map.of("color", "var(--danger)", "bold", "true"));
        dark.addRule(Selector.clazz("dim"), Map.of("color", "var(--text-dim)"));
        dark.addRule(Selector.clazz("button"), Map.of("background", "var(--primary)", "color", "black", "bold", "true"));
        dark.addRule(Selector.clazz("border"), Map.of("border-color", "var(--border)"));

        ThemeManager.loadTheme("dark", dark);

        // Light theme
        var light = new StyleSheet();
        light.setVariable("--primary", "#2255cc");
        light.setVariable("--bg", "#f5f5f5");
        light.setVariable("--bg-card", "#ffffff");
        light.setVariable("--text", "#222222");
        light.setVariable("--text-dim", "#777788");
        light.setVariable("--border", "#ccccdd");
        light.setVariable("--danger", "#cc2222");
        light.setVariable("--success", "#22aa44");

        light.addRule(Selector.type("Screen"), Map.of("background", "var(--bg)", "color", "var(--text)"));
        light.addRule(Selector.clazz("panel"), Map.of("background", "var(--bg-card)"));
        light.addRule(Selector.clazz("title"), Map.of("color", "var(--primary)", "bold", "true"));
        light.addRule(Selector.clazz("sidebar"), Map.of("background", "#eeeef5"));
        light.addRule(Selector.clazz("status"), Map.of("background", "var(--primary)", "color", "white", "bold", "true"));
        light.addRule(Selector.clazz("metric-ok"), Map.of("color", "var(--success)", "bold", "true"));
        light.addRule(Selector.clazz("metric-warn"), Map.of("color", "#cc8800", "bold", "true"));
        light.addRule(Selector.clazz("metric-danger"), Map.of("color", "var(--danger)", "bold", "true"));
        light.addRule(Selector.clazz("dim"), Map.of("color", "var(--text-dim)"));
        light.addRule(Selector.clazz("button"), Map.of("background", "var(--primary)", "color", "white", "bold", "true"));
        light.addRule(Selector.clazz("border"), Map.of("border-color", "var(--border)"));

        ThemeManager.loadTheme("light", light);
    }

    // Helper: resolve a style by widget type + classes
    private static Style css(String widgetType, String... classes) {
        return ThemeManager.resolve(widgetType, null, Set.of(classes), Set.of());
    }

    // Helper: styled block matching the border-color from the theme
    private static Block themedBlock(String title) {
        Style borderStyle = css("Panel", "border");
        Color borderColor = borderStyle.foreground();
        if (borderColor instanceof Color.Reset) {
            borderColor = Color.GRAY;
        }
        return Block.bordered(BorderStyle.ROUNDED)
            .title(" " + title + " ")
            .titleStyle(css("Label", "title"))
            .borderStyle(new Style(borderColor, Color.RESET, Set.of()));
    }

    public static ThemedDashboard createComponent() {
        return new ThemedDashboard();
    }

    static class ThemedDashboard extends Component {
        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            // Screen background
            Style screenBg = css("Screen");
            fill(area, screenBg.background(), buffer);

            var outer = themedBlock(" CSS Theme Demo — Press d/l to switch themes ");
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            // Layout: sidebar (30%) + content (70%)
            int sidebarW = Math.max(18, inner.width() * 30 / 100);
            int contentX = inner.x() + sidebarW + 1;

            // Sidebar
            renderSidebar(new Rect(inner.x(), inner.y(), sidebarW, inner.height()), buffer);

            // Content
            renderContent(new Rect(contentX, inner.y(),
                inner.width() - sidebarW - 1, inner.height()), buffer);

            // Status bar
            int statusY = area.y() + area.height() - 1;
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, statusY, new Cell(' ', css("status")));
            }
            String themeName = ThemeManager.global().resolveStyle("x", null, Set.of(), Set.of())
                .background().equals(new Color.Rgb(0x1a, 0x1a, 0x2e)) ? "DARK" : "LIGHT";
            buffer.setString(area.x() + 2, statusY,
                "  Theme: " + themeName
                + "  |  d: dark  |  l: light  |  q: quit  ",
                css("status"));
        }

        private void renderSidebar(Rect area, Buffer buffer) {
            var sidebar = themedBlock(" Navigation ");
            sidebar.render(area, buffer);
            var s = sidebar.innerRect(area);

            String[] items = {"Dashboard", "Metrics", "Logs", "Settings", "About"};
            for (int i = 0; i < items.length && i < s.height(); i++) {
                String item = "  " + items[i];
                Style itemStyle = (i == 0)
                    ? css("ListItem", "selected")
                    : css("ListItem");
                buffer.setString(s.x(), s.y() + i * 2, item, itemStyle);
                if (item.length() < s.width()) {
                    for (int x = s.x() + item.length(); x < s.x() + s.width(); x++) {
                        buffer.setCell(x, s.y() + i * 2, new Cell(' ', itemStyle));
                    }
                }
            }
        }

        private void renderContent(Rect area, Buffer buffer) {
            var content = themedBlock(" System Overview ");
            content.render(area, buffer);
            var c = content.innerRect(area);

            int y = c.y();

            // Title
            buffer.setString(c.x(), y, "Welcome to OtterTUI", css("Label", "title"));
            buffer.setString(c.x(), y + 1,
                "This dashboard is fully styled using CSS theme files (.tcss).",
                css("Label", "dim"));
            y += 3;

            // Metrics cards
            String[][] metrics = {
                {"CPU Usage", "23%", "metric-ok"},
                {"Memory", "67%", "metric-warn"},
                {"Disk I/O", "45%", "metric-ok"},
                {"Network", "89%", "metric-danger"}
            };

            for (int i = 0; i < metrics.length && y < c.y() + c.height() - 3; i++) {
                var card = themedBlock(metrics[i][0]);
                Rect cardArea = new Rect(c.x() + (i % 2) * (c.width() / 2),
                    y + (i / 2) * 4, c.width() / 2 - 1, 4);
                card.render(cardArea, buffer);
                var ci = card.innerRect(cardArea);
                buffer.setString(ci.x(), ci.y(), metrics[i][1],
                    css("Label", metrics[i][2]));
            }

            y += 8;

            // Action buttons
            String[] buttons = {" Refresh ", " Settings ", " Export "};
            String[] classes = {"button", "button", "button"};
            int bx = c.x();
            for (int i = 0; i < buttons.length && y < c.y() + c.height(); i++) {
                buffer.setString(bx, y, buttons[i], css("Label", classes[i]));
                // Fill button background
                for (int px = bx; px < bx + buttons[i].length(); px++) {
                    Cell cell = buffer.getCell(px, y);
                    buffer.setCell(px, y, new Cell(cell.ch(),
                        css("Label", classes[i])));
                }
                bx += buttons[i].length() + 2;
            }
        }

        private static void fill(Rect area, Color bg, Buffer buffer) {
            if (bg instanceof Color.Reset) return;
            for (int y = area.y(); y < area.y() + area.height(); y++) {
                for (int x = area.x(); x < area.x() + area.width(); x++) {
                    Cell current = buffer.getCell(x, y);
                    if (current.style().background() instanceof Color.Reset) {
                        buffer.setCell(x, y, new Cell(current.ch(),
                            current.style().bg(bg)));
                    }
                }
            }
        }
    }

}
