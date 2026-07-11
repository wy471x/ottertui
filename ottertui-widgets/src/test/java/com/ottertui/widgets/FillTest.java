package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class FillTest {

    @Test
    @DisplayName("default style constructor")
    void defaultConstructor() {
        Fill f = new Fill('*');
        Buffer b = new Buffer(5, 3);
        f.render(new Rect(0, 0, 5, 3), b);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals('*', b.getCell(x, y).ch());
                assertEquals(Style.DEFAULT, b.getCell(x, y).style());
            }
        }
    }

    @Test
    @DisplayName("fill with custom style")
    void customStyle() {
        Style s = new Style(Color.RED, Color.BLUE, java.util.Set.of(Modifier.BOLD));
        Fill f = new Fill('X', s);
        Buffer b = new Buffer(3, 2);
        f.render(new Rect(0, 0, 3, 2), b);
        assertEquals(s, b.getCell(0, 0).style());
        assertEquals(s, b.getCell(2, 1).style());
    }

    @Test
    @DisplayName("fill at non-zero offset")
    void fillAtOffset() {
        Fill f = new Fill('#');
        Buffer b = new Buffer(10, 10);
        f.render(new Rect(3, 2, 4, 3), b);
        assertEquals('#', b.getCell(3, 2).ch());
        assertEquals('#', b.getCell(6, 4).ch());
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
        assertEquals(Cell.EMPTY, b.getCell(7, 2));
    }

    @Test
    @DisplayName("zero-area rect does nothing")
    void zeroArea() {
        Fill f = new Fill('*');
        Buffer b = new Buffer(5, 5);
        f.render(new Rect(0, 0, 0, 5), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }
}
