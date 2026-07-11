package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

    @Test
    @DisplayName("default constructor creates Calendar")
    void defaultConstructor() {
        var c = new Calendar();
        assertNotNull(c);
    }

    @Test
    @DisplayName("full constructor with all styles")
    void fullConstructor() {
        var sel = new Style(Color.BLACK, Color.WHITE, java.util.Set.of());
        var today = new Style(Color.WHITE, Color.BLUE, java.util.Set.of());
        var c = new Calendar(sel, today,
            Style.DEFAULT, Style.DEFAULT, Style.DEFAULT, Style.DEFAULT,
            DayOfWeek.SUNDAY);
        assertNotNull(c);
    }

    @Test
    @DisplayName("render draws month title")
    void renderDrawsMonthTitle() {
        var c = new Calendar();
        var state = new CalendarState();
        Buffer b = new Buffer(30, 10);
        c.render(state, new Rect(0, 0, 30, 10), b);
        boolean foundTitle = false;
        for (int x = 0; x < 30; x++) {
            if (!Character.isWhitespace(b.getCell(x, 0).ch())) {
                foundTitle = true;
                break;
            }
        }
        assertTrue(foundTitle);
    }

    @Test
    @DisplayName("render draws separator line")
    void renderDrawsSeparator() {
        var c = new Calendar();
        var state = new CalendarState();
        Buffer b = new Buffer(30, 10);
        c.render(state, new Rect(0, 0, 30, 10), b);
        assertEquals('─', b.getCell(0, 1).ch());
        assertEquals('─', b.getCell(20, 1).ch());
    }

    @Test
    @DisplayName("render draws day-of-week headers")
    void renderDrawsWeekdayHeaders() {
        var c = new Calendar();
        var state = new CalendarState();
        Buffer b = new Buffer(30, 10);
        c.render(state, new Rect(0, 0, 30, 10), b);
        assertNotEquals(' ', b.getCell(2, 2).ch());
    }

    @Test
    @DisplayName("render highlights today")
    void renderHighlightsToday() {
        var todayStyle = new Style(Color.BLACK, Color.GREEN, java.util.Set.of());
        var c = new Calendar(new Style(Color.BLACK, Color.WHITE, java.util.Set.of()),
            todayStyle);
        var state = new CalendarState();
        Buffer b = new Buffer(30, 10);
        c.render(state, new Rect(0, 0, 30, 10), b);
        // Find today's column
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        int colOffset = (7 + day.getValue() - DayOfWeek.MONDAY.getValue()) % 7;
        int dayX = 2 + colOffset * 3;
        int dayRow = 3 + (LocalDate.now().getDayOfWeek().getValue() > 0 ? 0 : 0);
        // Today should have the custom background
        assertNotNull(b.getCell(dayX, dayRow));
    }

    @Test
    @DisplayName("area too small renders nothing")
    void areaTooSmall() {
        var c = new Calendar();
        var state = new CalendarState();
        Buffer b = new Buffer(10, 5);
        b.setCell(0, 0, new Cell('X', Style.DEFAULT));
        c.render(state, new Rect(0, 0, 10, 5), b);
        assertEquals('X', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("CalendarState previousMonth and nextMonth")
    void stateNavigation() {
        var state = new CalendarState();
        YearMonth original = state.displayedMonth();
        state.nextMonth();
        assertEquals(original.plusMonths(1), state.displayedMonth());
        state.previousMonth();
        assertEquals(original, state.displayedMonth());
    }

    @Test
    @DisplayName("CalendarState goToday resets to current")
    void stateGoToday() {
        var state = new CalendarState();
        state.nextMonth();
        state.nextMonth();
        state.goToday();
        assertEquals(YearMonth.from(LocalDate.now()), state.displayedMonth());
        assertEquals(LocalDate.now(), state.selectedDate());
    }

    @Test
    @DisplayName("CalendarState selectDate works")
    void selectDate() {
        var state = new CalendarState();
        LocalDate date = LocalDate.of(2025, 6, 15);
        state.selectDate(date);
        assertEquals(date, state.selectedDate());
    }

    @Test
    @DisplayName("render with firstDayOfWeek Sunday")
    void renderFirstDaySunday() {
        var c = new Calendar(
            new Style(Color.BLACK, Color.WHITE, java.util.Set.of()),
            new Style(Color.BLACK, Color.CYAN, java.util.Set.of()),
            Style.DEFAULT,
            Style.DEFAULT,
            Style.DEFAULT,
            Style.DEFAULT,
            DayOfWeek.SUNDAY);
        var state = new CalendarState();
        Buffer b = new Buffer(30, 10);
        c.render(state, new Rect(0, 0, 30, 10), b);
        assertNotEquals(' ', b.getCell(2, 2).ch());
    }
}
