package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ClearTest {

    @Test
    @DisplayName("render fills area with EMPTY cells")
    void renderFillsWithEmpty() {
        Clear w = new Clear();
        Buffer b = new Buffer(5, 3);
        Cell nonEmpty = new Cell('X', Style.DEFAULT);
        b.setCell(2, 1, nonEmpty);

        w.render(new Rect(0, 0, 5, 3), b);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(Cell.EMPTY, b.getCell(x, y));
            }
        }
    }

    @Test
    @DisplayName("render clears specific area")
    void renderClearsSpecificArea() {
        Clear w = new Clear();
        Buffer b = new Buffer(10, 10);
        Cell nonEmpty = new Cell('X', Style.DEFAULT);
        b.setCell(0, 0, nonEmpty);
        b.setCell(9, 9, nonEmpty);

        w.render(new Rect(2, 2, 4, 4), b);

        assertEquals('X', b.getCell(0, 0).ch());
        assertEquals('X', b.getCell(9, 9).ch());
        assertEquals(Cell.EMPTY, b.getCell(2, 2));
        assertEquals(Cell.EMPTY, b.getCell(5, 5));
    }
}
