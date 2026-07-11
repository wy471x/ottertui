package com.ottertui.widgets;

import com.ottertui.core.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Calendar implements StatefulWidget<CalendarState> {
    private static final int MIN_WIDTH = 20;
    private static final int MIN_HEIGHT = 7;

    private final Style selectedStyle;
    private final Style todayStyle;
    private final Style headerStyle;
    private final Style weekdayStyle;
    private final Style weekendStyle;
    private final Style otherMonthStyle;
    private final DayOfWeek firstDayOfWeek;

    public Calendar() {
        this(new Style(Color.BLACK, Color.WHITE, java.util.Set.of()),
             new Style(Color.BLACK, Color.CYAN, java.util.Set.of(Modifier.BOLD)));
    }

    public Calendar(Style selectedStyle, Style todayStyle) {
        this(selectedStyle, todayStyle,
             new Style(Color.CYAN, Color.RESET, java.util.Set.of(Modifier.BOLD)),
             new Style(Color.WHITE, Color.RESET, java.util.Set.of(Modifier.BOLD)),
             new Style(Color.RED, Color.RESET, java.util.Set.of(Modifier.BOLD)),
             new Style(Color.DARK_GRAY, Color.RESET, java.util.Set.of()),
             DayOfWeek.MONDAY);
    }

    public Calendar(Style selectedStyle, Style todayStyle,
                    Style headerStyle, Style weekdayStyle,
                    Style weekendStyle, Style otherMonthStyle,
                    DayOfWeek firstDayOfWeek) {
        this.selectedStyle = selectedStyle;
        this.todayStyle = todayStyle;
        this.headerStyle = headerStyle;
        this.weekdayStyle = weekdayStyle;
        this.weekendStyle = weekendStyle;
        this.otherMonthStyle = otherMonthStyle;
        this.firstDayOfWeek = firstDayOfWeek;
    }

    @Override
    public void render(CalendarState state, Rect area, Buffer buffer) {
        if (area.width() < MIN_WIDTH || area.height() < MIN_HEIGHT) return;

        YearMonth month = state.displayedMonth();
        LocalDate today = LocalDate.now();

        // Month/year header
        String title = month.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
            + " " + month.getYear();
        int titleX = area.x() + (area.width() - title.length()) / 2;
        buffer.setString(titleX, area.y(), title, headerStyle);

        // Separator
        for (int x = area.x(); x < area.x() + area.width(); x++) {
            buffer.setCell(x, area.y() + 1, new Cell('─', headerStyle));
        }

        // Day-of-week headers
        DayOfWeek day = firstDayOfWeek;
        for (int col = 0; col < 7; col++) {
            String label = day.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            int cellX = area.x() + 2 + col * 3;
            if (cellX + label.length() <= area.x() + area.width()) {
                boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                Style s = isWeekend ? weekendStyle : weekdayStyle;
                buffer.setString(cellX, area.y() + 2, label, s);
            }
            day = day.plus(1);
        }

        // Find first displayed day (may be in previous month)
        LocalDate firstOfMonth = month.atDay(1);
        int offset = (7 + firstOfMonth.getDayOfWeek().getValue()
            - firstDayOfWeek.getValue()) % 7;
        LocalDate cellDate = firstOfMonth.minusDays(offset);

        // Draw day grid
        for (int row = 0; row < 6; row++) {
            int y = area.y() + 3 + row;
            if (y >= area.y() + area.height()) break;

            for (int col = 0; col < 7; col++) {
                int cellX = area.x() + 2 + col * 3;
                if (cellX + 2 > area.x() + area.width()) break;

                Style s = dayStyle(cellDate, month, today, state.selectedDate());
                String cellStr = String.format("%2d", cellDate.getDayOfMonth());
                buffer.setString(cellX, y, cellStr, s);

                cellDate = cellDate.plusDays(1);
            }
        }
    }

    private Style dayStyle(LocalDate date, YearMonth month, LocalDate today,
                           LocalDate selected) {
        if (date.equals(selected)) return selectedStyle;
        if (date.equals(today)) return todayStyle;
        if (YearMonth.from(date).equals(month)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return weekendStyle;
            }
            return Style.DEFAULT;
        }
        return otherMonthStyle;
    }
}
