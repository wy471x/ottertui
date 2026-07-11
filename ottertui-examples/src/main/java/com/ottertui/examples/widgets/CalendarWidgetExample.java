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
import com.ottertui.widgets.Calendar;
import com.ottertui.widgets.CalendarState;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.util.Set;

public class CalendarWidgetExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var root = new CalComponent();
        var runner = new TuiRunner(backend, root);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> root.prevMonth());
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> root.nextMonth());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 't', () -> root.goToday());
        runner.run();
    }

    public static CalComponent createComponent() { return new CalComponent(); }

    static class CalComponent extends Component implements InteractiveExample {
        private final Calendar calendar;
        private final CalendarState state;

        CalComponent() {
            this.calendar = new Calendar();
            this.state = new CalendarState();
        }

        void prevMonth() { state.previousMonth(); }

        void nextMonth() { state.nextMonth(); }

        void goToday() { state.goToday(); }

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "left"  -> prevMonth();
                case "right" -> nextMonth();
                case "t"     -> goToday();
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" Calendar Widget ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            calendar.render(state, new Rect(inner.x() + 2, inner.y() + 1,
                Math.min(30, inner.width() - 4), Math.min(10, inner.height() - 3)), buffer);

            var hintStyle = new Style(Color.GRAY, Color.RESET, Set.of());
            int hintY = inner.y() + inner.height() - 2;
            buffer.setString(inner.x() + 2, hintY,
                "LEFT/RIGHT: navigate months  t: go to today  q: quit", hintStyle);
        }
    }
}
