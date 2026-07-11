package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    @DisplayName("bordered creates block with PLAIN style")
    void borderedPlain() {
        Block block = Block.bordered();
        assertNotNull(block);
    }

    @Test
    @DisplayName("bordered with custom BorderStyle")
    void borderedCustomStyle() {
        Block block = Block.bordered(BorderStyle.ROUNDED);
        assertNotNull(block);
    }

    @Test
    @DisplayName("title sets title text")
    void titleSetter() {
        Block block = Block.bordered().title("My Block");
        assertNotNull(block);
    }

    @Test
    @DisplayName("borderStyle sets custom style")
    void borderStyleSetter() {
        Style custom = new Style(Color.RED, Color.BLUE, Set.of());
        Block block = Block.bordered().borderStyle(custom);
        assertNotNull(block);
    }

    @Test
    @DisplayName("titleStyle sets title style")
    void titleStyleSetter() {
        Style custom = new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD));
        Block block = Block.bordered().titleStyle(custom);
        assertNotNull(block);
    }

    @Test
    @DisplayName("render draws borders on all sides")
    void renderDrawsBorders() {
        Block block = Block.bordered(BorderStyle.PLAIN);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);

        assertEquals('┌', b.getCell(0, 0).ch());
        assertEquals('┐', b.getCell(9, 0).ch());
        assertEquals('└', b.getCell(0, 4).ch());
        assertEquals('┘', b.getCell(9, 4).ch());
        assertEquals('│', b.getCell(0, 2).ch());
        assertEquals('│', b.getCell(9, 2).ch());
        assertEquals('─', b.getCell(5, 0).ch());
        assertEquals('─', b.getCell(5, 4).ch());
    }

    @Test
    @DisplayName("render with ROUNDED border")
    void renderRoundedBorders() {
        Block block = Block.bordered(BorderStyle.ROUNDED);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals('╭', b.getCell(0, 0).ch());
        assertEquals('╮', b.getCell(9, 0).ch());
        assertEquals('╰', b.getCell(0, 4).ch());
        assertEquals('╯', b.getCell(9, 4).ch());
    }

    @Test
    @DisplayName("render with DOUBLE border")
    void renderDoubleBorders() {
        Block block = Block.bordered(BorderStyle.DOUBLE);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals('╔', b.getCell(0, 0).ch());
        assertEquals('╗', b.getCell(9, 0).ch());
        assertEquals('╚', b.getCell(0, 4).ch());
        assertEquals('╝', b.getCell(9, 4).ch());
    }

    @Test
    @DisplayName("render with THICK border")
    void renderThickBorders() {
        Block block = Block.bordered(BorderStyle.THICK);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals('┏', b.getCell(0, 0).ch());
        assertEquals('┓', b.getCell(9, 0).ch());
    }

    @Test
    @DisplayName("render draws title")
    void renderDrawsTitle() {
        Block block = Block.bordered().title("Test");
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals('T', b.getCell(2, 0).ch());
        assertEquals('e', b.getCell(3, 0).ch());
        assertEquals('s', b.getCell(4, 0).ch());
        assertEquals('t', b.getCell(5, 0).ch());
    }

    @Test
    @DisplayName("render title truncated if too long")
    void renderTitleTruncated() {
        Block block = Block.bordered().title("VeryLongTitleHere");
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        String rendered = extractString(b, 2, 0, 6);
        assertEquals(6, rendered.length());
    }

    @Test
    @DisplayName("render skips if area too small")
    void renderSkipsIfTooSmall() {
        Block block = Block.bordered();
        Buffer b = new Buffer(1, 1);
        block.render(new Rect(0, 0, 1, 1), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render with custom border style color")
    void renderCustomBorderStyle() {
        Style red = new Style(Color.RED, Color.RESET, Set.of());
        Block block = Block.bordered().borderStyle(red);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals(Color.RED, b.getCell(0, 0).style().foreground());
    }

    @Test
    @DisplayName("render with custom titleStyle applies style to title")
    void renderCustomTitleStyle() {
        Style titleStyle = new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD));
        Block block = Block.bordered().title("Hi").titleStyle(titleStyle);
        Buffer b = new Buffer(10, 5);
        block.render(new Rect(0, 0, 10, 5), b);
        assertEquals(titleStyle, b.getCell(2, 0).style());
    }

    @Test
    @DisplayName("render skips with width < 2 but height >= 2")
    void renderSkipsWithNarrowWidth() {
        Block block = Block.bordered();
        Buffer b = new Buffer(1, 10);
        block.render(new Rect(0, 0, 1, 10), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render with narrow width truncates title guard")
    void renderNarrowTitleGuard() {
        Block block = Block.bordered().title("AB");
        Buffer b = new Buffer(3, 5);
        block.render(new Rect(0, 0, 3, 5), b);
        // maxWidth = 3 - 4 = -1 -> 0, so title is not rendered
        assertEquals('┌', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("innerRect returns area minus one on each side")
    void innerRect() {
        Block block = Block.bordered();
        Rect inner = block.innerRect(new Rect(0, 0, 10, 5));
        assertEquals(1, inner.x());
        assertEquals(1, inner.y());
        assertEquals(8, inner.width());
        assertEquals(3, inner.height());
    }

    private String extractString(Buffer b, int x, int y, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(b.getCell(x + i, y).ch());
        }
        return sb.toString();
    }
}
