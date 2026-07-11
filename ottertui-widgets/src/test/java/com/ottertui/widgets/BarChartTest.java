package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BarChartTest {

    @Test
    @DisplayName("default constructor with bars")
    void defaultConstructor() {
        var bars = List.of(new BarChart.Bar("A", 10, Style.DEFAULT));
        BarChart w = new BarChart(bars);
        assertNotNull(w);
    }

    @Test
    @DisplayName("constructor with custom bar width and gap")
    void customConstructor() {
        var bars = List.of(new BarChart.Bar("A", 10, Style.DEFAULT));
        BarChart w = new BarChart(bars, 5, 2);
        assertNotNull(w);
    }

    @Test
    @DisplayName("render with empty bars does nothing")
    void renderEmptyBars() {
        BarChart w = new BarChart(List.of());
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render draws bars and labels")
    void renderDrawsBars() {
        var bars = List.of(
            new BarChart.Bar("A", 50, new Style(Color.RED, Color.RESET, java.util.Set.of())),
            new BarChart.Bar("B", 100, new Style(Color.BLUE, Color.RESET, java.util.Set.of()))
        );
        BarChart w = new BarChart(bars, 2, 1);
        Buffer b = new Buffer(20, 8);
        w.render(new Rect(0, 0, 20, 8), b);

        assertNotEquals(' ', b.getCell(0, 6).ch());
        assertEquals('A', b.getCell(0, 7).ch());
    }

    @Test
    @DisplayName("render bar heights proportional to values")
    void renderProportionalBars() {
        var bars = List.of(
            new BarChart.Bar("A", 50, Style.DEFAULT),
            new BarChart.Bar("B", 100, Style.DEFAULT)
        );
        BarChart w = new BarChart(bars, 1, 1);
        Buffer b = new Buffer(20, 10);
        w.render(new Rect(0, 0, 20, 10), b);
        assertNotEquals(' ', b.getCell(0, 8).ch());
        assertEquals('█', b.getCell(2, 8).ch());
    }

    @Test
    @DisplayName("render label truncated to bar width")
    void renderLabelTruncated() {
        var bars = List.of(
            new BarChart.Bar("LongLabel", 50, Style.DEFAULT)
        );
        BarChart w = new BarChart(bars, 3, 1);
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals('L', b.getCell(0, 4).ch());
    }

    @Test
    @DisplayName("render clips at area width")
    void renderClipsAtWidth() {
        var bars = List.of(
            new BarChart.Bar("A", 10, Style.DEFAULT),
            new BarChart.Bar("B", 10, Style.DEFAULT),
            new BarChart.Bar("C", 10, Style.DEFAULT)
        );
        BarChart w = new BarChart(bars, 5, 1);
        Buffer b = new Buffer(8, 8);
        w.render(new Rect(0, 0, 8, 8), b);
        assertEquals('A', b.getCell(0, 7).ch());
        assertEquals('B', b.getCell(6, 7).ch());
    }
}
