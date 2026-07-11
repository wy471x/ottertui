package com.ottertui.widgets;

import java.time.LocalDate;
import java.time.YearMonth;

public class CalendarState {
    private LocalDate selectedDate;
    private YearMonth displayedMonth;

    public CalendarState() {
        LocalDate today = LocalDate.now();
        this.selectedDate = today;
        this.displayedMonth = YearMonth.from(today);
    }

    public LocalDate selectedDate() {
        return selectedDate;
    }

    public void selectDate(LocalDate date) {
        this.selectedDate = date;
    }

    public YearMonth displayedMonth() {
        return displayedMonth;
    }

    public void previousMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
    }

    public void nextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
    }

    public void goToday() {
        LocalDate today = LocalDate.now();
        selectedDate = today;
        displayedMonth = YearMonth.from(today);
    }
}
