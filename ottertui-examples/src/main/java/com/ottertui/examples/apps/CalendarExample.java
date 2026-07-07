package com.ottertui.examples.apps;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
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
import com.ottertui.widgets.ClearWidget;

import java.io.IOException;
import java.time.DayOfWeek;
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
        private YearMonth currentMonth;
        private final LocalDate today;
        private LocalDate selectedDate;

        // Styles
        private static final Style HEADER_STYLE = new Style(Color.CYAN, Color.RESET,
            Set.of(Modifier.BOLD));
        private static final Style TODAY_STYLE = new Style(Color.BLACK, Color.CYAN,
            Set.of(Modifier.BOLD));
        private static final Style SELECTED_STYLE = new Style(Color.BLACK, Color.WHITE,
            Set.of());
        private static final Style WEEKDAY_HEADER_STYLE = new Style(Color.WHITE, Color.RESET,
            Set.of(Modifier.BOLD));
        private static final Style WEEKEND_HEADER_STYLE = new Style(Color.RED, Color.RESET,
            Set.of(Modifier.BOLD));
        private static final Style OTHER_MONTH_STYLE = new Style(Color.DARK_GRAY, Color.RESET,
            Set.of());
        private static final Style NORMAL_DAY_STYLE = Style.DEFAULT;
        private static final Style SATURDAY_STYLE = new Style(Color.BLUE, Color.RESET, Set.of());
        private static final Style SUNDAY_STYLE = new Style(Color.RED, Color.RESET, Set.of());
        private static final Style NAV_HINT_STYLE = new Style(Color.GRAY, Color.RESET, Set.of());

        // Calendar layout constants
        private static final int CELL_WIDTH = 4;
        private static final int HEADER_HEIGHT = 3;

        CalendarComponent() {
            this.today = LocalDate.now();
            this.currentMonth = YearMonth.from(today);
            this.selectedDate = today;
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
            currentMonth = currentMonth.minusMonths(1);
            selectedDate = currentMonth.atDay(1);
        }

        void nextMonth() {
            currentMonth = currentMonth.plusMonths(1);
            selectedDate = currentMonth.atDay(1);
        }

        void goToday() {
            currentMonth = YearMonth.from(today);
            selectedDate = today;
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            String monthTitle = currentMonth.getMonth().getDisplayName(
                TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear();

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

            renderCalendar(inner, buffer);
            renderKeyBindings(inner, buffer);
            renderMiniCalendars(inner, buffer);
        }

        private void renderCalendar(Rect inner, Buffer buffer) {
            int calWidth = 7 * CELL_WIDTH + 6;
            int calX = inner.x() + 2;
            int calY = inner.y() + 1;

            // Weekday headers
            DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

            for (int i = 0; i < 7; i++) {
                String label = days[i].getDisplayName(TextStyle.SHORT, Locale.getDefault());
                int x = calX + i * (CELL_WIDTH + 1);
                var style = (i >= 5) ? WEEKEND_HEADER_STYLE : WEEKDAY_HEADER_STYLE;
                buffer.setString(x, calY, center(label, CELL_WIDTH), style);
            }

            // Separator under header
            for (int x = calX; x < calX + calWidth; x++) {
                buffer.setCell(x, calY + 1, new Cell('─', Style.DEFAULT));
            }

            // Calculate first day position
            LocalDate firstOfMonth = currentMonth.atDay(1);
            int startDow = firstOfMonth.getDayOfWeek().getValue() - 1; // Mon=0
            int daysInMonth = currentMonth.lengthOfMonth();

            // Also calculate days from previous/next month to fill the grid
            YearMonth prevMonth = currentMonth.minusMonths(1);
            int daysInPrevMonth = prevMonth.lengthOfMonth();

            // Render 6 rows
            for (int row = 0; row < 6; row++) {
                int y = calY + HEADER_HEIGHT + row;
                if (y >= inner.y() + inner.height()) break;

                for (int col = 0; col < 7; col++) {
                    int dayNum = row * 7 + col - startDow + 1;
                    int x = calX + col * (CELL_WIDTH + 1);

                    LocalDate date;
                    boolean otherMonth;

                    if (dayNum < 1) {
                        date = prevMonth.atDay(daysInPrevMonth + dayNum);
                        otherMonth = true;
                    } else if (dayNum > daysInMonth) {
                        date = currentMonth.atDay(dayNum - daysInMonth);
                        otherMonth = true;
                    } else {
                        date = currentMonth.atDay(dayNum);
                        otherMonth = false;
                    }

                    String text = String.format("%2d", date.getDayOfMonth());
                    String padded = center(text, CELL_WIDTH);

                    // Determine cell style
                    Style dayStyle;
                    if (otherMonth) {
                        dayStyle = OTHER_MONTH_STYLE;
                    } else if (date.equals(today)) {
                        dayStyle = TODAY_STYLE;
                    } else if (date.equals(selectedDate) && !date.equals(today)) {
                        dayStyle = SELECTED_STYLE;
                    } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                        dayStyle = SATURDAY_STYLE;
                    } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        dayStyle = SUNDAY_STYLE;
                    } else {
                        dayStyle = NORMAL_DAY_STYLE;
                    }

                    buffer.setString(x, y, padded, dayStyle);
                }
            }
        }

        private void renderMiniCalendars(Rect inner, Buffer buffer) {
            int calWidth = 7 * CELL_WIDTH + 6;
            int miniX = inner.x() + calWidth + 4;
            if (miniX + 16 > inner.x() + inner.width()) return;

            int miniY = inner.y() + 1;

            // Previous month mini calendar
            YearMonth prev = currentMonth.minusMonths(1);
            buffer.setString(miniX, miniY,
                prev.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                new Style(Color.GRAY, Color.RESET, Set.of(Modifier.BOLD)));

            renderMiniMonth(prev, miniX, miniY + 1, buffer);

            // Next month mini calendar
            YearMonth next = currentMonth.plusMonths(1);
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
            if (infoY <= inner.y() + HEADER_HEIGHT + 6) return;

            long monthDiff = ChronoUnit.MONTHS.between(
                YearMonth.from(today), currentMonth);
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
                "← → navigate months  |  t: today  |  q: quit",
                NAV_HINT_STYLE);
        }

        private static String center(String s, int width) {
            int pad = width - s.length();
            int left = pad / 2;
            int right = pad - left;
            return " ".repeat(Math.max(0, left)) + s + " ".repeat(Math.max(0, right));
        }
    }
}
