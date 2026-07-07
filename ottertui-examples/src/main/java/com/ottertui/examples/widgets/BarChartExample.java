package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.examples.InteractiveExample;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.BarChartWidget;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.ClearWidget;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class BarChartExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new BCComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static BCComponent createComponent() { return new BCComponent(); }

    static class BCComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" BarChartWidget ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            var chart1 = new BarChartWidget(List.of(
                new BarChartWidget.Bar("CPU", 45, new Style(Color.RED, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Mem", 72, new Style(Color.YELLOW, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Disk", 30, new Style(Color.GREEN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Net", 88, new Style(Color.BLUE, Color.RESET, Set.of())),
                new BarChartWidget.Bar("GPU", 55, new Style(Color.MAGENTA, Color.RESET, Set.of()))
            ), 5, 2);
            chart1.render(new Rect(inner.x() + 2, inner.y() + 1,
                inner.width() - 4, 8), buffer);

            int y2 = inner.y() + 11;
            var chart2 = new BarChartWidget(List.of(
                new BarChartWidget.Bar("Mon", 12, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Tue", 35, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Wed", 67, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Thu", 42, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Fri", 90, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Sat", 22, new Style(Color.CYAN, Color.RESET, Set.of())),
                new BarChartWidget.Bar("Sun", 8, new Style(Color.CYAN, Color.RESET, Set.of()))
            ), 3, 1);
            chart2.render(new Rect(inner.x() + 2, y2,
                inner.width() - 4, 6), buffer);

            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 1,
                "Weekly Traffic (thin bars, gap=1)", Style.DEFAULT);
        }
    }
}
