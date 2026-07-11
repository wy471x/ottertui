package com.ottertui.examples.apps;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.examples.InteractiveExample;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Calendar;
import com.ottertui.widgets.CalendarState;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;

public class CalendarExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) {
            System.err.println("Error: No terminal detected.");
            System.exit(1);
        }

        var root = new CalendarComponent();
        var runner = new TuiRunner(backend, root);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> root.prevMonth());
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> root.nextMonth());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 't', () -> root.goToday());
        runner.run();
    }

    public static CalendarComponent createComponent() {
        return new CalendarComponent();
    }

    static class CalendarComponent extends Component implements InteractiveExample {
        private final Calendar calendar;
        private final CalendarState state;
        private final LocalDate today;

        private static final Style NAV_HINT_STYLE =
            new Style(Color.GRAY, Color.RESET, Set.of());

        CalendarComponent() {
            this.today = LocalDate.now();
            this.state = new CalendarState();
            this.calendar = new Calendar();
        }

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "left"  -> prevMonth();
                case "right" -> nextMonth();
                case "t"     -> goToday();
                default -> { }
            }
        }

        void prevMonth() {
            state.previousMonth();
        }

        void nextMonth() {
            state.nextMonth();
        }

        void goToday() {
            state.goToday();
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            YearMonth month = state.displayedMonth();
            String monthTitle = month.getMonth().getDisplayName(
                TextStyle.FULL, Locale.getDefault())
                + " " + month.getYear();

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" " + monthTitle + " ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            if (inner.width() < 30 || inner.height() < 8) {
                buffer.setString(inner.x(), inner.y(),
                    "Terminal too small for calendar", Style.DEFAULT);
                return;
            }

            // Render the calendar widget
            calendar.render(state,
                new Rect(inner.x() + 2, inner.y() + 1,
                    Math.min(28, inner.width() - 18), Math.min(10, inner.height() - 4)),
                buffer);

            renderMiniCalendars(inner, buffer);
            renderKeyBindings(inner, buffer);
        }

        private void renderMiniCalendars(Rect inner, Buffer buffer) {
            int miniX = inner.x() + 32;
            if (miniX + 16 > inner.x() + inner.width()) return;

            int miniY = inner.y() + 1;

            // Previous month mini calendar
            YearMonth prev = state.displayedMonth().minusMonths(1);
            buffer.setString(miniX, miniY,
                prev.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                new Style(Color.GRAY, Color.RESET, Set.of(Modifier.BOLD)));

            renderMiniMonth(prev, miniX, miniY + 1, buffer);

            // Next month mini calendar
            YearMonth next = state.displayedMonth().plusMonths(1);
            buffer.setString(miniX, miniY + 8,
                next.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                new Style(Color.GRAY, Color.RESET, Set.of(Modifier.BOLD)));

            renderMiniMonth(next, miniX, miniY + 9, buffer);
        }

        private void renderMiniMonth(YearMonth ym, int x, int y, Buffer buffer) {
            String[] dayHeaders = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
            for (int i = 0; i < 7; i++) {
                var style = (i >= 5)
                    ? new Style(Color.RED, Color.RESET, Set.of())
                    : new Style(Color.DARK_GRAY, Color.RESET, Set.of());
                buffer.setString(x + i * 2, y, dayHeaders[i], style);
            }

            LocalDate first = ym.atDay(1);
            int startDow = first.getDayOfWeek().getValue() - 1;

            for (int row = 0; row < 6; row++) {
                int ry = y + 1 + row;
                for (int col = 0; col < 7; col++) {
                    int dayNum = row * 7 + col - startDow + 1;
                    if (dayNum < 1 || dayNum > ym.lengthOfMonth()) continue;

                    int cx = x + col * 2;
                    LocalDate d = ym.atDay(dayNum);
                    String text = String.format("%2d", dayNum);
                    var style = d.equals(today)
                        ? new Style(Color.BLACK, Color.CYAN, Set.of(Modifier.BOLD))
                        : Style.DEFAULT;
                    buffer.setString(cx, ry, text, style);
                }
            }
        }

        private void renderKeyBindings(Rect inner, Buffer buffer) {
            int infoY = inner.y() + inner.height() - 3;
            if (infoY <= inner.y() + 12) return;

            long monthDiff = ChronoUnit.MONTHS.between(
                YearMonth.from(today), state.displayedMonth());
            String offsetStr;
            if (monthDiff == 0) {
                offsetStr = "This month";
            } else if (monthDiff > 0) {
                offsetStr = monthDiff + " month(s) ahead";
            } else {
                offsetStr = (-monthDiff) + " month(s) ago";
            }

            buffer.setString(inner.x() + 2, infoY,
                offsetStr, new Style(Color.YELLOW, Color.RESET, Set.of()));

            buffer.setString(inner.x() + 2, infoY + 1,
                "\u2190 \u2192 navigate months  |  t: today  |  q: quit",
                NAV_HINT_STYLE);
        }
    }
}
