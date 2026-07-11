package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SparklineTest {

    @Test
    @DisplayName("default constructor")
    void defaultConstructor() {
        Sparkline w = new Sparkline(List.of(1.0, 2.0, 3.0));
        assertNotNull(w);
    }

    @Test
    @DisplayName("full constructor with max data points and style")
    void fullConstructor() {
        Sparkline w = new Sparkline(List.of(1.0, 2.0), 50,
            new Style(Color.RED, Color.RESET, java.util.Set.of()));
        assertNotNull(w);
    }

    @Test
    @DisplayName("render with empty data does nothing")
    void renderEmptyData() {
        Sparkline w = new Sparkline(List.of());
        Buffer b = new Buffer(10, 1);
        w.render(new Rect(0, 0, 10, 1), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render draws sparkline blocks")
    void renderDrawsBlocks() {
        Sparkline w = new Sparkline(List.of(0.0, 0.5, 1.0));
        Buffer b = new Buffer(10, 1);
        w.render(new Rect(0, 0, 10, 1), b);
        assertEquals('▁', b.getCell(0, 0).ch());
        assertNotEquals(' ', b.getCell(1, 0).ch());
        assertEquals('█', b.getCell(2, 0).ch());
    }

    @Test
    @DisplayName("render clips to area width")
    void renderClipsToAreaWidth() {
        Sparkline w = new Sparkline(List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        Buffer b = new Buffer(3, 1);
        w.render(new Rect(0, 0, 3, 1), b);
        assertNotEquals(Cell.EMPTY.ch(), b.getCell(0, 0).ch());
        assertNotEquals(Cell.EMPTY.ch(), b.getCell(1, 0).ch());
        assertNotEquals(Cell.EMPTY.ch(), b.getCell(2, 0).ch());
    }

    @Test
    @DisplayName("render with uniform data produces middle block")
    void renderUniformData() {
        Sparkline w = new Sparkline(List.of(5.0, 5.0, 5.0));
        Buffer b = new Buffer(10, 1);
        w.render(new Rect(0, 0, 10, 1), b);
        assertEquals('▄', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("render with custom style")
    void renderCustomStyle() {
        Style style = new Style(Color.GREEN, Color.RESET, java.util.Set.of(Modifier.BOLD));
        Sparkline w = new Sparkline(List.of(0.0, 1.0), 80, style);
        Buffer b = new Buffer(10, 1);
        w.render(new Rect(0, 0, 10, 1), b);
        assertEquals(style, b.getCell(0, 0).style());
    }

    @Test
    @DisplayName("render uses window of last maxDataPoints")
    void renderUsesWindow() {
        List<Double> data = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) data.add((double) i);
        Sparkline w = new Sparkline(data, 10, Style.DEFAULT);
        Buffer b = new Buffer(20, 1);
        w.render(new Rect(0, 0, 20, 1), b);
        assertEquals('▁', b.getCell(0, 0).ch());
    }
}
