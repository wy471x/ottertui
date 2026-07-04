package com.ottertui.examples;

import com.ottertui.core.*;
import com.ottertui.tui.*;
import com.ottertui.widgets.*;
import com.ottertui.backend.jline.JLineBackend;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DemoApp {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) {
            System.err.println("Error: No terminal detected. "
                + "OtterTUI must be run from a real terminal, not from an IDE or build tool.");
            System.err.println("Try: java -cp <classpath> com.ottertui.examples.DemoApp");
            System.exit(1);
        }

        var root = new DashboardComponent();
        var runner = new TuiRunner(backend, root);

        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.UP, Set.of(), 0, () -> root.moveSelection(-1));
        runner.keyBindings().bind(KeyCode.DOWN, Set.of(), 0, () -> root.moveSelection(1));

        runner.run();
    }

    static class DashboardComponent extends Component {
        private int selectedTab = 0;
        private int selectedItem = 0;
        private final List<String> menuItems = List.of(
            "Dashboard", "Metrics", "Logs", "Settings", "About");

        void moveSelection(int delta) {
            selectedItem = Math.clamp(selectedItem + delta, 0, menuItems.size() - 1);
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            if (area.width() <= 0 || area.height() <= 0) return;

            // Clear
            for (int y = area.y(); y < area.y() + area.height(); y++) {
                for (int x = area.x(); x < area.x() + area.width(); x++) {
                    buffer.setCell(x, y, Cell.EMPTY);
                }
            }

            // Layout: tabs at top, then sidebar + main content
            var mainLayout = Layout.horizontal(List.of(
                Constraint.percentage(25),
                Constraint.percentage(75)
            ));

            var mainAreas = mainLayout.split(area.inner(1, 0));

            // Sidebar
            var sidebar = Block.bordered().title("Menu");
            sidebar.render(mainAreas.get(0), buffer);

            var listInner = sidebar.innerRect(mainAreas.get(0));
            for (int i = 0; i < menuItems.size() && i < listInner.height(); i++) {
                var style = (i == selectedItem)
                    ? new Style(Color.BLACK, Color.WHITE, Set.of())
                    : Style.DEFAULT;
                String item = menuItems.get(i);
                String display = item.length() > listInner.width()
                    ? item.substring(0, listInner.width()) : item;
                buffer.setString(listInner.x(), listInner.y() + i, display, style);
                for (int x = listInner.x() + display.length();
                     x < listInner.x() + listInner.width(); x++) {
                    buffer.setCell(x, listInner.y() + i, new Cell(' ', style));
                }
            }

            // Content area
            var content = Block.bordered().title("Dashboard");
            content.render(mainAreas.get(1), buffer);

            var inner = content.innerRect(mainAreas.get(1));

            if (inner.height() < 5) return;

            // Welcome text
            var titleStyle = new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD));
            buffer.setString(inner.x(), inner.y(), "Welcome to OtterTUI", titleStyle);

            // Subtitle
            buffer.setString(inner.x(), inner.y() + 2,
                "Use UP/DOWN arrows to navigate. Press 'q' to quit.", Style.DEFAULT);

            // Gauge
            if (inner.height() >= 5) {
                int gaugeY = inner.y() + 4;
                buffer.setString(inner.x(), gaugeY, "CPU: ", Style.DEFAULT);
                var gauge = new GaugeWidget(0.73,
                    new Style(Color.GREEN, Color.RESET, Set.of()),
                    new Style(Color.RESET, Color.RESET, Set.of()));
                gauge.render(new Rect(inner.x() + 5, gaugeY,
                    Math.min(40, inner.width() - 5), 1), buffer);
            }

            // Sparkline
            if (inner.height() >= 7) {
                int chartY = inner.y() + 6;
                buffer.setString(inner.x(), chartY, "Trend:", Style.DEFAULT);
                var sparkline = new SparklineWidget(
                    List.of(10.0, 25.0, 15.0, 40.0, 30.0, 55.0, 45.0, 70.0,
                        60.0, 85.0, 75.0, 90.0, 80.0, 65.0, 50.0, 35.0,
                        45.0, 60.0, 75.0, 80.0),
                    30, new Style(Color.CYAN, Color.RESET, Set.of()));
                sparkline.render(new Rect(inner.x() + 7, chartY,
                    Math.min(30, inner.width() - 7), 1), buffer);
            }

            // Bar chart
            if (inner.height() >= 14) {
                int barY = inner.y() + 8;
                var barChart = new BarChartWidget(List.of(
                    new BarChartWidget.Bar("CPU", 65, new Style(Color.RED, Color.RESET, Set.of())),
                    new BarChartWidget.Bar("Mem", 82, new Style(Color.YELLOW, Color.RESET, Set.of())),
                    new BarChartWidget.Bar("Disk", 45, new Style(Color.GREEN, Color.RESET, Set.of())),
                    new BarChartWidget.Bar("Net", 30, new Style(Color.BLUE, Color.RESET, Set.of()))
                ), 5, 2);
                barChart.render(new Rect(inner.x(), barY,
                    Math.min(40, inner.width()), 6), buffer);
            }

            // Status bar at the bottom
            int statusY = area.y() + area.height() - 1;
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, statusY,
                    new Cell(' ', new Style(Color.WHITE, Color.BLUE, Set.of())));
            }
            buffer.setString(area.x() + 2, statusY,
                " OtterTUI v0.1.0 | Selected: " + menuItems.get(selectedItem)
                + " | Press q to quit ",
                new Style(Color.WHITE, Color.BLUE, Set.of()));
        }
    }
}
