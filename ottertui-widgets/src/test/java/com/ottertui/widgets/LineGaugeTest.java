package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class LineGaugeTest {

    @Test
    @DisplayName("constructor clamps ratio to 0.0-1.0")
    void constructorClampsRatio() {
        LineGauge g = new LineGauge(-0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals('\u2500', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("ratio above 1.0 clamped to 1.0")
    void ratioAboveOne() {
        LineGauge g = new LineGauge(2.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals('\u2501', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("zero ratio draws all unfilled")
    void zeroRatio() {
        LineGauge g = new LineGauge(0.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        for (int x = 0; x < 10; x++) {
            assertEquals('\u2500', b.getCell(x, 0).ch());
        }
    }

    @Test
    @DisplayName("full ratio draws all filled")
    void fullRatio() {
        LineGauge g = new LineGauge(1.0);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        for (int x = 0; x < 10; x++) {
            assertEquals('\u2501', b.getCell(x, 0).ch());
        }
    }

    @Test
    @DisplayName("50 percent ratio")
    void halfRatio() {
        LineGauge g = new LineGauge(0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals('\u2501', b.getCell(0, 0).ch());
        assertEquals('\u2501', b.getCell(4, 0).ch());
        assertEquals('\u2500', b.getCell(5, 0).ch());
        assertEquals('\u2500', b.getCell(9, 0).ch());
    }

    @Test
    @DisplayName("custom style constructor")
    void customStyle() {
        Style filled = new Style(Color.RED, Color.RESET, java.util.Set.of());
        Style unfilled = new Style(Color.BLUE, Color.RESET, java.util.Set.of());
        LineGauge g = new LineGauge(0.5, filled, unfilled);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 10, 1), b);
        assertEquals(filled, b.getCell(0, 0).style());
        assertEquals(unfilled, b.getCell(5, 0).style());
    }

    @Test
    @DisplayName("render at non-zero position")
    void renderAtOffset() {
        LineGauge g = new LineGauge(1.0);
        Buffer b = new Buffer(20, 5);
        g.render(new Rect(5, 2, 10, 1), b);
        assertEquals('\u2501', b.getCell(5, 2).ch());
        assertEquals('\u2501', b.getCell(14, 2).ch());
        assertEquals(Cell.EMPTY, b.getCell(4, 2));
    }

    @Test
    @DisplayName("empty area width renders nothing")
    void emptyArea() {
        LineGauge g = new LineGauge(0.5);
        Buffer b = new Buffer(10, 1);
        g.render(new Rect(0, 0, 0, 1), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }
}
