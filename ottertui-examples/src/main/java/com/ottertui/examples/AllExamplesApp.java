package com.ottertui.examples;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.examples.apps.CalendarExample;
import com.ottertui.examples.apps.TetrisGame;
import com.ottertui.examples.apps.ThemeExample;
import com.ottertui.examples.widgets.BarChartExample;
import com.ottertui.examples.widgets.BlockExample;
import com.ottertui.examples.widgets.ClearWidgetExample;
import com.ottertui.examples.widgets.GaugeExample;
import com.ottertui.examples.widgets.LayoutExample;
import com.ottertui.examples.widgets.ListExample;
import com.ottertui.examples.widgets.ParagraphExample;
import com.ottertui.examples.widgets.SparklineExample;
import com.ottertui.examples.widgets.TableExample;
import com.ottertui.examples.widgets.TabsExample;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class AllExamplesApp {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) {
            System.err.println("Error: No terminal detected.");
            System.err.println("Try: java -cp <classpath> com.ottertui.examples.AllExamplesApp");
            System.exit(1);
        }

        var root = new MenuComponent();
        var runner = new TuiRunner(backend, root);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', () -> root.handleKey("q"));
        runner.keyBindings().bind(KeyCode.UP, Set.of(), 0, () -> root.handleKey("up"));
        runner.keyBindings().bind(KeyCode.DOWN, Set.of(), 0, () -> root.handleKey("down"));
        runner.keyBindings().bind(KeyCode.ENTER, Set.of(), 0, () -> root.handleKey("enter"));
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> root.handleKey("left"));
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> root.handleKey("right"));
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 't', () -> root.handleKey("t"));
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), ' ', () -> root.handleKey("space"));
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'p', () -> root.handleKey("p"));
        runner.run();
    }

    /** Top-level menu that dispatches to individual examples. */
    static class MenuComponent extends Component {
        private int selected = 0;
        private Component activeExample;

        private final List<String> examples = List.of(
            "Paragraph", "Block", "List", "Tabs", "Table",
            "Gauge", "Sparkline", "BarChart", "Layout", "ClearWidget",
            "Calendar", "Theme Demo", "Tetris"
        );

        private final String[] descs = {
            "Styled text with alignment and word-wrap",
            "Bordered containers with titles and styles",
            "Selectable scrollable list",
            "Horizontal tab navigation",
            "Data table with columns, headers, and selection",
            "Horizontal progress bar",
            "Inline sparkline chart from data series",
            "Vertical bar chart with labels",
            "Layout system with constraints (%, fixed, proportional)",
            "Clears a region to blank cells",
            "Monthly calendar with navigation and mini calendars",
            "CSS theme engine demo with dark/light switching",
            "Classic Tetris game with scoring and levels"
        };

        void handleKey(String key) {
            if (activeExample != null) {
                if (key.equals("q")) {
                    activeExample = null;
                } else if (activeExample instanceof InteractiveExample ie) {
                    ie.handleKey(key);
                }
                return;
            }
            switch (key) {
                case "up"   -> selected = (selected - 1 + examples.size()) % examples.size();
                case "down" -> selected = (selected + 1) % examples.size();
                case "enter" -> activeExample = createExample(selected);
                case "q"    -> System.exit(0);
            }
        }

        private Component createExample(int i) {
            return switch (i) {
                case 0  -> ParagraphExample.createComponent();
                case 1  -> BlockExample.createComponent();
                case 2  -> ListExample.createComponent();
                case 3  -> TabsExample.createComponent();
                case 4  -> TableExample.createComponent();
                case 5  -> GaugeExample.createComponent();
                case 6  -> SparklineExample.createComponent();
                case 7  -> BarChartExample.createComponent();
                case 8  -> LayoutExample.createComponent();
                case 9  -> ClearWidgetExample.createComponent();
                case 10 -> CalendarExample.createComponent();
                case 11 -> ThemeExample.createComponent();
                case 12 -> TetrisGame.createComponent();
                default -> null;
            };
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            if (activeExample != null) {
                activeExample.render(area, buffer);
                int hintY = area.y() + area.height() - 1;
                for (int x = area.x(); x < area.x() + area.width(); x++) {
                    buffer.setCell(x, hintY,
                        new Cell(' ', new Style(Color.WHITE, Color.BLUE, Set.of())));
                }
                buffer.setString(area.x() + 2, hintY,
                    " q: Back to menu ",
                    new Style(Color.WHITE, Color.BLUE, Set.of()));
                return;
            }

            // Clear
            for (int y = area.y(); y < area.y() + area.height(); y++) {
                for (int x = area.x(); x < area.x() + area.width(); x++) {
                    buffer.setCell(x, y, Cell.EMPTY);
                }
            }

            var titleStyle = new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD));
            buffer.setString(area.x() + 2, area.y(),
                "OtterTUI — Widget Examples", titleStyle);
            buffer.setString(area.x() + 2, area.y() + 1,
                "Use UP/DOWN to navigate, ENTER to select, q to quit", Style.DEFAULT);

            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, area.y() + 2, new Cell('─', Style.DEFAULT));
            }

            for (int i = 0; i < examples.size(); i++) {
                int y = area.y() + 4 + i;
                if (y >= area.y() + area.height() - 1) break;

                String label = (i == selected ? " > " : "   ") + examples.get(i);
                var style = (i == selected)
                    ? new Style(Color.BLACK, Color.CYAN, Set.of(Modifier.BOLD))
                    : Style.DEFAULT;

                buffer.setString(area.x() + 2, y, label, style);
                for (int x = area.x() + 2 + label.length();
                     x < area.x() + area.width() - 2; x++) {
                    buffer.setCell(x, y, new Cell(' ', style));
                }
            }

            for (int i = 0; i < descs.length && i < examples.size(); i++) {
                int y = area.y() + 4 + i;
                if (y >= area.y() + area.height() - 1) break;
                if (i == selected) {
                    buffer.setString(area.x() + 30, y, descs[i],
                        new Style(Color.CYAN, Color.RESET, Set.of()));
                }
            }
        }
    }
}
