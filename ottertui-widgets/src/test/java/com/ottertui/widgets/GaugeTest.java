package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class GaugeTest {

    @Test
    @DisplayName("constructor clamps ratio to 0.0-1.0")
    void constructorClampsRatio() {
        Gauge g = new Gauge(-0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals('░', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("ratio above 1.0 clamped to 1.0")
    void ratioAboveOne() {
        Gauge g = new Gauge(2.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertNotEquals('░', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("zero ratio draws all background")
    void zeroRatio() {
        Gauge g = new Gauge(0.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        for (int x = 0; x < 10; x++) {
            assertEquals('░', b.getCell(x, 0).ch());
        }
    }

    @Test
    @DisplayName("full ratio draws all filled")
    void fullRatio() {
        Gauge g = new Gauge(1.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        for (int x = 0; x < 10; x++) {
            assertEquals('█', b.getCell(x, 0).ch());
        }
    }

    @Test
    @DisplayName("50 percent ratio")
    void halfRatio() {
        Gauge g = new Gauge(0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals('█', b.getCell(0, 0).ch());
        assertEquals('░', b.getCell(5, 0).ch());
    }

    @Test
    @DisplayName("custom style constructor")
    void customStyle() {
        Gauge g = new Gauge(0.5,
            new Style(Color.RED, Color.RESET, java.util.Set.of()),
            new Style(Color.BLUE, Color.RESET, java.util.Set.of()));
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals(Color.RED, b.getCell(0, 0).style().foreground());
        assertEquals(Color.BLUE, b.getCell(5, 0).style().foreground());
    }

    @Test
    @DisplayName("render at non-zero position")
    void renderAtOffset() {
        Gauge g = new Gauge(1.0);
        Buffer b = new Buffer(20, 5);
        g.render(new Rect(5, 2, 10, 1), b);
        assertEquals('█', b.getCell(5, 2).ch());
        assertEquals('█', b.getCell(14, 2).ch());
        assertEquals(Cell.EMPTY, b.getCell(4, 2));
        assertEquals(Cell.EMPTY, b.getCell(15, 2));
    }

    @Test
    @DisplayName("empty area width renders nothing")
    void emptyArea() {
        Gauge g = new Gauge(0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 0, 1), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }
}
