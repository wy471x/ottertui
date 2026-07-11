package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ScrollbarTest {

    @Test
    @DisplayName("vertical scrollbar with default styles")
    void verticalDefault() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(100);
        s.setViewportLength(25);
        s.setPosition(0);
        Buffer b = new Buffer(5, 20);
        s.render(new Rect(0, 0, 1, 20), b);
        assertEquals('█', b.getCell(0, 0).ch());
        assertEquals('█', b.getCell(0, 4).ch());
        assertEquals('│', b.getCell(0, 5).ch());
    }

    @Test
    @DisplayName("horizontal scrollbar with default styles")
    void horizontalDefault() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        s.setContentLength(100);
        s.setViewportLength(25);
        s.setPosition(0);
        Buffer b = new Buffer(20, 5);
        s.render(new Rect(0, 0, 20, 1), b);
        assertEquals('█', b.getCell(0, 0).ch());
        assertEquals('█', b.getCell(4, 0).ch());
        assertEquals('─', b.getCell(5, 0).ch());
    }

    @Test
    @DisplayName("content fits viewport shows full thumb")
    void contentFitsViewport() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(50);
        s.setViewportLength(100);
        Buffer b = new Buffer(5, 10);
        s.render(new Rect(0, 0, 1, 10), b);
        for (int y = 0; y < 10; y++) {
            assertEquals('█', b.getCell(0, y).ch());
        }
    }

    @Test
    @DisplayName("position at mid shows thumb at mid")
    void positionAtMid() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(100);
        s.setViewportLength(10);
        s.setPosition(45);
        Buffer b = new Buffer(5, 20);
        s.render(new Rect(0, 0, 1, 20), b);
        assertEquals('│', b.getCell(0, 0).ch());
        assertNotEquals('│', b.getCell(0, 9).ch());
        assertEquals('│', b.getCell(0, 19).ch());
    }

    @Test
    @DisplayName("position at end shows thumb at end")
    void positionAtEnd() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(100);
        s.setViewportLength(10);
        s.setPosition(90);
        Buffer b = new Buffer(5, 20);
        s.render(new Rect(0, 0, 1, 20), b);
        assertEquals('│', b.getCell(0, 0).ch());
        assertEquals('█', b.getCell(0, 19).ch());
    }

    @Test
    @DisplayName("custom styles")
    void customStyles() {
        Style track = new Style(Color.GRAY, Color.RESET, java.util.Set.of());
        Style thumb = new Style(Color.YELLOW, Color.RESET, java.util.Set.of());
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.HORIZONTAL, track, thumb);
        s.setContentLength(100);
        s.setViewportLength(10);
        s.setPosition(0);
        Buffer b = new Buffer(20, 5);
        s.render(new Rect(0, 0, 20, 1), b);
        assertEquals(thumb, b.getCell(0, 0).style());
        assertEquals(track, b.getCell(2, 0).style());
    }

    @Test
    @DisplayName("zero-area rect does nothing")
    void zeroArea() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        Buffer b = new Buffer(5, 5);
        s.render(new Rect(0, 0, 0, 0), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("setPosition clamps negative to zero")
    void setPositionClampsNegative() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setPosition(-10);
        assertEquals(0, s.position());
    }

    @Test
    @DisplayName("setContentLength clamps to minimum 1")
    void setContentLengthClamps() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(0);
        assertEquals(1, s.contentLength());
    }

    @Test
    @DisplayName("viewportLength getter works")
    void viewportLengthGetter() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setViewportLength(50);
        assertEquals(50, s.viewportLength());
    }

    @Test
    @DisplayName("thumb size clamps to minimum 1")
    void tinyThumbClamps() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        s.setContentLength(10000);
        s.setViewportLength(1);
        s.setPosition(0);
        Buffer b = new Buffer(5, 5);
        s.render(new Rect(0, 0, 5, 1), b);
        assertEquals('█', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("thumb at max position does not overflow bar")
    void thumbAtMaxPositionDoesNotOverflow() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(100);
        s.setViewportLength(10);
        s.setPosition(90);
        Buffer b = new Buffer(5, 10);
        s.render(new Rect(0, 0, 1, 10), b);
        for (int y = 0; y < 10; y++) {
            char ch = b.getCell(0, y).ch();
            assertTrue(ch == '│' || ch == '█');
        }
    }

    @Test
    @DisplayName("contentLength getter works")
    void contentLengthGetter() {
        Scrollbar s = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        s.setContentLength(200);
        assertEquals(200, s.contentLength());
    }
}
