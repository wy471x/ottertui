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
import com.ottertui.widgets.Scrollbar;

import java.io.IOException;
import java.util.Set;

public class ScrollbarExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var root = new SComponent();
        var runner = new TuiRunner(backend, root);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.UP, Set.of(), 0, () -> root.scrollUp());
        runner.keyBindings().bind(KeyCode.DOWN, Set.of(), 0, () -> root.scrollDown());
        runner.run();
    }

    public static SComponent createComponent() { return new SComponent(); }

    static class SComponent extends Component implements InteractiveExample {
        private final Scrollbar vScrollbar;
        private final Scrollbar hScrollbar;
        private int position;

        SComponent() {
            var thumbStyle = new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD));
            var trackStyle = new Style(Color.DARK_GRAY, Color.RESET, Set.of());
            vScrollbar = new Scrollbar(Scrollbar.Orientation.VERTICAL, trackStyle, thumbStyle);
            vScrollbar.setContentLength(200);
            vScrollbar.setViewportLength(30);
            hScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL, trackStyle, thumbStyle);
            hScrollbar.setContentLength(150);
            hScrollbar.setViewportLength(40);
            position = 0;
        }

        void scrollUp() {
            if (position > 0) position -= 10;
            vScrollbar.setPosition(position);
            hScrollbar.setPosition(position);
        }

        void scrollDown() {
            if (position < 170) position += 10;
            vScrollbar.setPosition(position);
            hScrollbar.setPosition(position);
        }

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "up"   -> scrollUp();
                case "down" -> scrollDown();
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Scrollbar ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            int contentX = inner.x() + 2;
            int contentY = inner.y() + 2;
            int contentW = inner.width() - 6;
            int contentH = inner.height() - 6;

            // Content area border
            Block.bordered(BorderStyle.PLAIN)
                .title(" Content Area")
                .render(new Rect(contentX, contentY, contentW, contentH), buffer);

            // Vertical scrollbar on the right
            vScrollbar.render(new Rect(contentX + contentW - 2, contentY + 1,
                1, contentH - 2), buffer);

            // Horizontal scrollbar at the bottom
            hScrollbar.render(new Rect(contentX + 1, contentY + contentH - 2,
                contentW - 3, 1), buffer);

            // Position info
            var infoStyle = new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD));
            String info = String.format("Scroll position: %d / 170 (UP/DOWN to scroll)",
                position);
            buffer.setString(inner.x() + 2, inner.y() + inner.height() - 1,
                info, infoStyle);
        }
    }
}
