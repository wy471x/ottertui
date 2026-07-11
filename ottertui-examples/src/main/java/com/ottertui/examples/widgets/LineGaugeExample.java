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
import com.ottertui.widgets.Clear;
import com.ottertui.widgets.LineGauge;

import java.io.IOException;
import java.util.Set;

public class LineGaugeExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new LGComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static LGComponent createComponent() { return new LGComponent(); }

    static class LGComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" LineGauge ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            int gaugeWidth = Math.min(50, inner.width() - 20);

            record GInfo(String label, double ratio, Style color) {}
            GInfo[] gauges = {
                new GInfo("Download  ", 0.35, new Style(Color.GREEN, Color.RESET, Set.of())),
                new GInfo("Upload    ", 0.12, new Style(Color.CYAN, Color.RESET, Set.of())),
                new GInfo("Processing", 0.78, new Style(Color.YELLOW, Color.RESET, Set.of())),
                new GInfo("Verifying ", 0.56, new Style(Color.BLUE, Color.RESET, Set.of())),
                new GInfo("Complete  ", 1.0, new Style(Color.MAGENTA, Color.RESET, Set.of())),
            };

            for (int i = 0; i < gauges.length; i++) {
                int y = inner.y() + 1 + i * 2;
                if (y >= inner.y() + inner.height()) break;

                var g = gauges[i];
                buffer.setString(inner.x() + 2, y, g.label(),
                    new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)));

                var gauge = new LineGauge(g.ratio(), g.color(),
                    new Style(Color.DARK_GRAY, Color.RESET, Set.of()));
                gauge.render(new Rect(inner.x() + 14, y, gaugeWidth, 1), buffer);

                String pct = String.format("%3.0f%%", g.ratio() * 100);
                buffer.setString(inner.x() + 14 + gaugeWidth + 1, y, pct, g.color());
            }

            var hint = new Style(Color.GRAY, Color.RESET, Set.of());
            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 2,
                "LineGauge uses ━ (filled) and ─ (unfilled) line chars", hint);
        }
    }
}
