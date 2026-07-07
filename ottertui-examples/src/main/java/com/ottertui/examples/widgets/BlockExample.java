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

import java.io.IOException;
import java.util.Set;

public class BlockExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new BComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static BComponent createComponent() { return new BComponent(); }

    static class BComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" Block Widget — Border Styles ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            BorderStyle[] styles = {BorderStyle.PLAIN, BorderStyle.ROUNDED,
                BorderStyle.DOUBLE, BorderStyle.THICK};
            String[] names = {"PLAIN", "ROUNDED", "DOUBLE", "THICK"};

            int boxW = Math.min(22, (inner.width() - 8) / 4);
            for (int i = 0; i < 4; i++) {
                int bx = inner.x() + 1 + i * (boxW + 2);
                var block = Block.bordered(styles[i])
                    .title(" " + names[i] + " ")
                    .titleStyle(new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
                Rect boxArea = new Rect(bx, inner.y(), boxW + 2, 6);
                block.render(boxArea, buffer);

                var bi = block.innerRect(boxArea);
                buffer.setString(bi.x(), bi.y(), "Content inside", Style.DEFAULT);
                buffer.setString(bi.x(), bi.y() + 1, "the bordered", Style.DEFAULT);
                buffer.setString(bi.x(), bi.y() + 2, "block.", Style.DEFAULT);
            }

            int cy = inner.y() + 8;
            var colored = Block.bordered(BorderStyle.PLAIN)
                .title(" Colored Border ")
                .borderStyle(new Style(Color.GREEN, Color.RESET, Set.of()))
                .titleStyle(new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD)));
            Rect coloredArea = new Rect(inner.x() + 1, cy, 35, 5);
            colored.render(coloredArea, buffer);
            var ci = colored.innerRect(coloredArea);
            buffer.setString(ci.x(), ci.y(), "Border: green", Style.DEFAULT);
            buffer.setString(ci.x(), ci.y() + 1, "Use .borderStyle() to", Style.DEFAULT);
            buffer.setString(ci.x(), ci.y() + 2, "customize border colors.", Style.DEFAULT);
        }
    }
}
