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
import com.ottertui.widgets.ClearWidget;
import com.ottertui.widgets.GaugeWidget;

import java.io.IOException;
import java.util.Set;

public class GaugeExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new GComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static GComponent createComponent() { return new GComponent(); }

    static class GComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" GaugeWidget ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            record GaugeInfo(String label, double ratio, Style color) {}
            GaugeInfo[] gauges = {
                new GaugeInfo("CPU Usage  ", 0.23, new Style(Color.GREEN, Color.RESET, Set.of())),
                new GaugeInfo("Memory     ", 0.67, new Style(Color.YELLOW, Color.RESET, Set.of())),
                new GaugeInfo("Disk I/O   ", 0.45, new Style(Color.CYAN, Color.RESET, Set.of())),
                new GaugeInfo("Network    ", 0.89, new Style(Color.RED, Color.RESET, Set.of())),
                new GaugeInfo("GPU Load   ", 0.95, new Style(Color.MAGENTA, Color.RESET, Set.of())),
                new GaugeInfo("Cache Hit  ", 0.12, new Style(Color.BLUE, Color.RESET, Set.of())),
            };

            int gaugeWidth = Math.min(50, inner.width() - 20);
            for (int i = 0; i < gauges.length; i++) {
                int y = inner.y() + 1 + i * 3;
                if (y + 2 >= inner.y() + inner.height()) break;

                var g = gauges[i];
                buffer.setString(inner.x() + 1, y, g.label(),
                    new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)));

                var gauge = new GaugeWidget(g.ratio(), g.color(),
                    new Style(Color.DARK_GRAY, Color.RESET, Set.of()));
                gauge.render(new Rect(inner.x() + 14, y, gaugeWidth, 1), buffer);

                String pct = String.format("%3.0f%%", g.ratio() * 100);
                buffer.setString(inner.x() + 14 + gaugeWidth + 1, y, pct, g.color());
            }
        }
    }
}
