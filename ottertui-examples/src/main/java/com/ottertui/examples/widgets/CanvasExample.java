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
import com.ottertui.widgets.Canvas;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.util.Set;

public class CanvasExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new CaComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static CaComponent createComponent() { return new CaComponent(); }

    static class CaComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Canvas (Braille-dot Drawing) ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            int canvasW = Math.min(80, inner.width() - 4);
            int canvasH = Math.min(40, inner.height() - 4);

            // Draw shapes on canvas
            var canvas = new Canvas(canvasW, canvasH, p -> {
                // Grid lines
                int midY = canvasH / 2;
                int midX = canvasW / 2;
                p.line(0, midY, canvasW - 1, midY, new Color.Rgb(64, 64, 64));
                p.line(midX, 0, midX, canvasH - 1, new Color.Rgb(64, 64, 64));

                // Circle
                p.circle(midX / 2, midY, Math.min(midX, midY) / 3, Color.GREEN);

                // Rectangle
                p.rect(midX + 10, 5, midX - 20, canvasH - 10, Color.CYAN);

                // Diagonal lines
                p.line(0, 0, canvasW - 1, canvasH - 1, Color.YELLOW);
                p.line(0, canvasH - 1, canvasW - 1, 0, Color.MAGENTA);

                // Small filled rectangle
                p.rect(midX - 5, midY - 5, 10, 10, Color.RED);
            });

            canvas.render(new Rect(inner.x() + 2, inner.y() + 1,
                canvasW, canvasH), buffer);

            var hintStyle = new Style(Color.GRAY, Color.RESET, Set.of());
            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 1,
                "Canvas: set() + line() + rect() + circle() with Braille dots",
                hintStyle);
        }
    }
}
