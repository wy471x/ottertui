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
import com.ottertui.widgets.SparklineWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class SparklineExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new SComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static SComponent createComponent() { return new SComponent(); }

    static class SComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" SparklineWidget ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            int y = inner.y() + 1;

            buffer.setString(inner.x() + 1, y,
                "Upward trend:", new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD)));
            var upData = new ArrayList<Double>();
            for (int i = 0; i < 60; i++) {
                upData.add(i * 0.5 + Math.random() * 5);
            }
            new SparklineWidget(upData, 40,
                new Style(Color.GREEN, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 16, y, 40, 1), buffer);

            y += 3;
            buffer.setString(inner.x() + 1, y,
                "Volatile:", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            var volData = new ArrayList<Double>();
            for (int i = 0; i < 60; i++) {
                volData.add(50 + Math.sin(i * 0.3) * 40 + Math.random() * 15);
            }
            new SparklineWidget(volData, 40,
                new Style(Color.YELLOW, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 16, y, 40, 1), buffer);

            y += 3;
            buffer.setString(inner.x() + 1, y,
                "Downward trend:", new Style(Color.RED, Color.RESET, Set.of(Modifier.BOLD)));
            var downData = new ArrayList<Double>();
            for (int i = 0; i < 60; i++) {
                downData.add(80 - i * 0.7 + Math.random() * 5);
            }
            new SparklineWidget(downData, 40,
                new Style(Color.RED, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 16, y, 40, 1), buffer);

            y += 3;
            buffer.setString(inner.x() + 1, y,
                "Steady:", new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            var steadyData = new ArrayList<Double>();
            for (int i = 0; i < 60; i++) {
                steadyData.add(30 + Math.random() * 5);
            }
            new SparklineWidget(steadyData, 40,
                new Style(Color.CYAN, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 16, y, 40, 1), buffer);

            y += 3;
            buffer.setString(inner.x() + 1, y,
                "Sparklines show data trends in a single line using Unicode block chars.",
                Style.DEFAULT);
            buffer.setString(inner.x() + 1, y + 1,
                "Use maxDataPoints to control how many values are displayed (windowed).",
                Style.DEFAULT);
        }
    }
}
