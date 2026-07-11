package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Alignment;
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
import com.ottertui.widgets.Paragraph;

import java.io.IOException;
import java.util.Set;

public class ParagraphExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new PComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static PComponent createComponent() { return new PComponent(); }

    static class PComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Paragraph ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            buffer.setString(inner.x(), inner.y(),
                "LEFT (default):", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            var left = new Paragraph(
                "This text is left-aligned with word wrapping enabled. "
                + "Long lines will automatically wrap to fit the available width.",
                Style.DEFAULT, Alignment.LEFT, true);
            left.render(new Rect(inner.x(), inner.y() + 1,
                inner.width() - 2, 3), buffer);

            int cy = inner.y() + 5;
            buffer.setString(inner.x(), cy,
                "CENTER:", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            var center = new Paragraph(
                "Centered text with a custom color.",
                new Style(Color.CYAN, Color.RESET, Set.of(Modifier.ITALIC)),
                Alignment.CENTER, true);
            center.render(new Rect(inner.x(), cy + 1, inner.width() - 2, 2), buffer);

            int ry = inner.y() + 8;
            buffer.setString(inner.x(), ry,
                "RIGHT:", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            var right = new Paragraph(
                "Right-aligned paragraph — useful for RTL or numeric data.",
                new Style(Color.MAGENTA, Color.RESET, Set.of()),
                Alignment.RIGHT, true);
            right.render(new Rect(inner.x(), ry + 1, inner.width() - 2, 2), buffer);

            int ny = inner.y() + 11;
            buffer.setString(inner.x(), ny,
                "NO WRAP:", new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            var nowrap = new Paragraph(
                "This is too long and will be truncated at the edge...",
                new Style(Color.RED, Color.RESET, Set.of(Modifier.BOLD)),
                Alignment.LEFT, false);
            nowrap.render(new Rect(inner.x(), ny + 1, 25, 1), buffer);
        }
    }
}
