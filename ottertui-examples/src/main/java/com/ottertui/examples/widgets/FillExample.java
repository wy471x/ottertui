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
import com.ottertui.widgets.Fill;

import java.io.IOException;
import java.util.Set;

public class FillExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new FComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static FComponent createComponent() { return new FComponent(); }

    static class FComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Fill ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            // Fill regions with different characters and colors
            new Fill('█', new Style(Color.RED, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 2, inner.y() + 1, 10, 3), buffer);
            new Fill('▓', new Style(Color.GREEN, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 14, inner.y() + 1, 10, 3), buffer);
            new Fill('▒', new Style(Color.YELLOW, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 26, inner.y() + 1, 10, 3), buffer);
            new Fill('░', new Style(Color.BLUE, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 38, inner.y() + 1, 10, 3), buffer);

            new Fill('*', new Style(Color.MAGENTA, Color.RESET, Set.of(Modifier.BOLD)))
                .render(new Rect(inner.x() + 2, inner.y() + 6, 20, 5), buffer);
            new Fill('#', new Style(Color.CYAN, Color.RESET, Set.of()))
                .render(new Rect(inner.x() + 24, inner.y() + 6, 20, 5), buffer);

            var labelStyle = new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD));
            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 1,
                "Fill renders solid regions with custom characters and styles", labelStyle);
        }
    }
}
