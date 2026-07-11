package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CanvasTest {

    @Test
    @DisplayName("single dot renders braille character")
    void singleDot() {
        var canvas = new Canvas(8, 8, p -> p.set(0, 0, Color.RED));
        Buffer b = new Buffer(10, 5);
        canvas.render(new Rect(0, 0, 10, 5), b);
        assertNotEquals(' ', b.getCell(0, 0).ch());
        assertEquals(Color.RED, b.getCell(0, 0).style().foreground());
    }

    @Test
    @DisplayName("all 8 dots in one cell renders full block")
    void allEightDots() {
        var canvas = new Canvas(8, 8, p -> {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 2; x++) {
                    p.set(x, y, Color.WHITE);
                }
            }
        });
        Buffer b = new Buffer(10, 5);
        canvas.render(new Rect(0, 0, 10, 5), b);
        assertEquals('⣿', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("horizontal line draws")
    void horizontalLine() {
        var canvas = new Canvas(16, 8,
            p -> p.line(0, 2, 15, 2, Color.GREEN));
        Buffer b = new Buffer(20, 5);
        canvas.render(new Rect(0, 0, 20, 5), b);
        int dotCount = 0;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 20; x++) {
                if (b.getCell(x, y).ch() != ' ') dotCount++;
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("vertical line draws")
    void verticalLine() {
        var canvas = new Canvas(8, 16,
            p -> p.line(2, 0, 2, 15, Color.BLUE));
        Buffer b = new Buffer(10, 10);
        canvas.render(new Rect(0, 0, 10, 10), b);
        int dotCount = 0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (b.getCell(x, y).ch() != ' ') dotCount++;
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("diagonal line draws")
    void diagonalLine() {
        var canvas = new Canvas(10, 10,
            p -> p.line(0, 0, 9, 9, Color.WHITE));
        Buffer b = new Buffer(10, 10);
        canvas.render(new Rect(0, 0, 10, 10), b);
        int dotCount = 0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (b.getCell(x, y).ch() != ' ') dotCount++;
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("rectangle drawing")
    void rectangle() {
        var canvas = new Canvas(20, 20,
            p -> p.rect(1, 1, 10, 8, Color.YELLOW));
        Buffer b = new Buffer(15, 10);
        canvas.render(new Rect(0, 0, 15, 10), b);
        int dotCount = 0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 15; x++) {
                if (b.getCell(x, y).ch() != ' ') dotCount++;
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("circle drawing")
    void circle() {
        var canvas = new Canvas(30, 30,
            p -> p.circle(15, 15, 10, Color.MAGENTA));
        Buffer b = new Buffer(20, 10);
        canvas.render(new Rect(0, 0, 20, 10), b);
        int dotCount = 0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 20; x++) {
                if (b.getCell(x, y).ch() != ' ') dotCount++;
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("empty canvas renders nothing")
    void emptyCanvas() {
        var canvas = new Canvas(10, 10, p -> {});
        Buffer b = new Buffer(10, 10);
        canvas.render(new Rect(0, 0, 10, 10), b);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertEquals(' ', b.getCell(x, y).ch());
            }
        }
    }

    @Test
    @DisplayName("area smaller than canvas clips")
    void clippedArea() {
        var canvas = new Canvas(40, 40,
            p -> p.line(0, 0, 39, 39, Color.RED));
        Buffer b = new Buffer(5, 3);
        canvas.render(new Rect(0, 0, 5, 3), b);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                if (b.getCell(x, y).ch() != ' ') {
                    return;
                }
            }
        }
        fail("Expected at least one dot");
    }

    @Test
    @DisplayName("offset area renders correctly")
    void offsetArea() {
        var canvas = new Canvas(8, 8,
            p -> p.set(0, 0, Color.CYAN));
        Buffer b = new Buffer(20, 10);
        canvas.render(new Rect(5, 3, 10, 5), b);
        assertNotEquals(' ', b.getCell(5, 3).ch());
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }
}
