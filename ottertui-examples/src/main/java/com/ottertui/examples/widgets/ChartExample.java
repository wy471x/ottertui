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
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Chart;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChartExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new ChComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static ChComponent createComponent() { return new ChComponent(); }

    static class ChComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Chart (Braille-dot Lines) ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            if (inner.width() < 30 || inner.height() < 15) {
                buffer.setString(inner.x(), inner.y(),
                    "Terminal too small for chart", Style.DEFAULT);
                return;
            }

            // Left chart: sine wave
            var sinePoints = new ArrayList<Chart.Point>();
            for (int i = 0; i < 40; i++) {
                double x = i * 0.25;
                sinePoints.add(new Chart.Point(x, Math.sin(x)));
            }
            var sine = new Chart.Dataset("sine", sinePoints,
                new Style(Color.CYAN, Color.RESET, Set.of()));

            var cosinePoints = new ArrayList<Chart.Point>();
            for (int i = 0; i < 40; i++) {
                double x = i * 0.25;
                cosinePoints.add(new Chart.Point(x, Math.cos(x)));
            }
            var cosine = new Chart.Dataset("cosine", cosinePoints,
                new Style(Color.YELLOW, Color.RESET, Set.of()));

            var chart1 = new Chart(List.of(sine, cosine));
            chart1.render(new Rect(inner.x() + 1, inner.y() + 1,
                inner.width() / 2 - 2, inner.height() - 4), buffer);

            // Right chart: linear with auto-scale
            var line1Points = new ArrayList<Chart.Point>();
            for (int i = 0; i < 20; i++) {
                line1Points.add(new Chart.Point(i, Math.pow(i * 0.5, 2)));
            }
            var line1 = new Chart.Dataset("x^2", line1Points,
                new Style(Color.GREEN, Color.RESET, Set.of()));

            var line2Points = new ArrayList<Chart.Point>();
            for (int i = 0; i < 20; i++) {
                line2Points.add(new Chart.Point(i, i * 1.5));
            }
            var line2 = new Chart.Dataset("linear", line2Points,
                new Style(Color.MAGENTA, Color.RESET, Set.of()));

            var chart2 = new Chart(List.of(line1, line2));
            chart2.render(new Rect(inner.x() + inner.width() / 2, inner.y() + 1,
                inner.width() / 2 - 2, inner.height() - 4), buffer);

            var hintStyle = new Style(Color.GRAY, Color.RESET, Set.of());
            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 2,
                "Chart renders lines with Braille dot patterns (2x4 dots per cell)",
                hintStyle);
        }
    }
}
