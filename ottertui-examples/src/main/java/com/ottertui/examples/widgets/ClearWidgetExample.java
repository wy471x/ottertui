package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
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

import java.io.IOException;
import java.util.Set;

public class ClearWidgetExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new CwComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> { });
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> { });
        runner.run();
    }

    public static CwComponent createComponent() { return new CwComponent(); }

    static class CwComponent extends Component implements InteractiveExample {
        private int step = 0;

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "left", "right" -> step = (step + 1) % 3;
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            var bg = new Style(Color.BLUE, Color.BLUE, Set.of());
            for (int y = area.y(); y < area.y() + area.height(); y++) {
                for (int x = area.x(); x < area.x() + area.width(); x++) {
                    buffer.setCell(x, y, new Cell(' ', bg));
                }
            }

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" Clear ")
                .borderStyle(new Style(Color.WHITE, Color.RESET, Set.of()))
                .titleStyle(new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            for (int y = inner.y(); y < inner.y() + inner.height(); y++) {
                for (int x = inner.x(); x < inner.x() + inner.width(); x++) {
                    char ch = (x + y) % 2 == 0 ? '.' : ' ';
                    buffer.setCell(x, y, new Cell(ch,
                        new Style(Color.WHITE, Color.BLUE, Set.of())));
                }
            }
            buffer.setString(inner.x() + 2, inner.y() + 2,
                "This area has background content", Style.DEFAULT);
            buffer.setString(inner.x() + 2, inner.y() + 3,
                "Press LEFT/RIGHT to see Clear in action", Style.DEFAULT);

            if (step >= 1) {
                int cw = inner.width() / 2 - 2;
                int ch = inner.height() / 2 - 2;
                new Clear().render(new Rect(inner.x() + 2, inner.y() + 2,
                    cw, ch), buffer);
                buffer.setString(inner.x() + 4, inner.y() + 2,
                    "Cleared!", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            }
            if (step >= 2) {
                int cw = inner.width() / 2 - 2;
                int ch = inner.height() / 2 - 2;
                new Clear().render(new Rect(
                    inner.x() + inner.width() / 2, inner.y() + inner.height() / 2,
                    cw, ch), buffer);
                buffer.setString(inner.x() + inner.width() / 2 + 2,
                    inner.y() + inner.height() / 2,
                    "Also cleared!", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            }

            int infoY = area.y() + area.height() - 1;
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, infoY,
                    new Cell(' ', new Style(Color.WHITE, Color.BLUE, Set.of())));
            }
            buffer.setString(area.x() + 2, infoY,
                " Step " + step + "/2 | LEFT/RIGHT to cycle | q to quit ",
                new Style(Color.WHITE, Color.BLUE, Set.of()));
        }
    }
}
