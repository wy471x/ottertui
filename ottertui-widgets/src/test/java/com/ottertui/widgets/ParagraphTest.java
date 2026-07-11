package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphTest {

    @Test
    @DisplayName("default constructor with text")
    void defaultConstructor() {
        Paragraph w = new Paragraph("Hello");
        assertNotNull(w);
    }

    @Test
    @DisplayName("full constructor with style, alignment, wrap")
    void fullConstructor() {
        Paragraph w = new Paragraph("Hello", Style.DEFAULT, Alignment.CENTER, false);
        assertNotNull(w);
    }

    @Test
    @DisplayName("render left aligned with wrap")
    void renderLeftAligned() {
        Paragraph w = new Paragraph("Hello World");
        Buffer b = new Buffer(20, 3);
        w.render(new Rect(0, 0, 20, 3), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('e', b.getCell(1, 0).ch());
    }

    @Test
    @DisplayName("render center aligned")
    void renderCenterAligned() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.CENTER, false);
        Buffer b = new Buffer(20, 3);
        w.render(new Rect(0, 0, 20, 3), b);
        assertEquals('H', b.getCell(9, 0).ch());
    }

    @Test
    @DisplayName("render right aligned")
    void renderRightAligned() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.RIGHT, false);
        Buffer b = new Buffer(20, 3);
        w.render(new Rect(0, 0, 20, 3), b);
        assertEquals('H', b.getCell(18, 0).ch());
    }

    @Test
    @DisplayName("render no-wrap truncates text")
    void renderNoWrapTruncates() {
        Paragraph w = new Paragraph("HelloWorld", Style.DEFAULT, Alignment.LEFT, false);
        Buffer b = new Buffer(5, 3);
        w.render(new Rect(0, 0, 5, 3), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('o', b.getCell(4, 0).ch());
    }

    @Test
    @DisplayName("render wraps long text to multiple lines")
    void renderWrapsText() {
        Paragraph w = new Paragraph("Hello World Test");
        Buffer b = new Buffer(6, 5);
        w.render(new Rect(0, 0, 6, 5), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertNotEquals(Cell.EMPTY.ch(), b.getCell(0, 1).ch());
    }

    @Test
    @DisplayName("render wraps on word boundaries")
    void renderWrapsOnWordBoundaries() {
        Paragraph w = new Paragraph("Hello World");
        Buffer b = new Buffer(6, 5);
        w.render(new Rect(0, 0, 6, 5), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('W', b.getCell(0, 1).ch());
    }

    @Test
    @DisplayName("render clips at area height with wrap")
    void renderClipsAtHeight() {
        Paragraph w = new Paragraph("A\nB\nC\nD\nE");
        Buffer b = new Buffer(10, 2);
        w.render(new Rect(0, 0, 10, 2), b);
        assertEquals('A', b.getCell(0, 0).ch());
        assertEquals('B', b.getCell(0, 1).ch());
        assertEquals(Cell.EMPTY, b.getCell(0, 2));
    }

    @Test
    @DisplayName("render with empty paragraph lines")
    void renderEmptyLines() {
        Paragraph w = new Paragraph("\n\n");
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals(Cell.EMPTY.ch(), b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("render wrap with zero width does nothing")
    void renderZeroWidth() {
        Paragraph w = new Paragraph("Hello");
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 0, 5), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render wraps at exact space boundary")
    void renderWrapsAtSpaceBoundary() {
        Paragraph w = new Paragraph("Hello World");
        Buffer b = new Buffer(6, 5);
        w.render(new Rect(0, 0, 6, 5), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('W', b.getCell(0, 1).ch());
    }

    @Test
    @DisplayName("render trims leading spaces after wrap")
    void renderTrimsLeadingSpaces() {
        Paragraph w = new Paragraph("Hello   World");
        Buffer b = new Buffer(6, 5);
        w.render(new Rect(0, 0, 6, 5), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('W', b.getCell(0, 1).ch());
    }

    @Test
    @DisplayName("render with CENTER alignment")
    void renderCenterAlignment() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.CENTER, false);
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals('H', b.getCell(4, 0).ch());
        assertEquals('i', b.getCell(5, 0).ch());
    }

    @Test
    @DisplayName("render with RIGHT alignment")
    void renderRightAlignment() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.RIGHT, false);
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals('H', b.getCell(8, 0).ch());
        assertEquals('i', b.getCell(9, 0).ch());
    }

    @Test
    @DisplayName("render CENTER alignment with wrap")
    void renderCenterAlignmentWithWrap() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.CENTER, true);
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals('H', b.getCell(4, 0).ch());
        assertEquals('i', b.getCell(5, 0).ch());
    }

    @Test
    @DisplayName("render RIGHT alignment with wrap")
    void renderRightAlignmentWithWrap() {
        Paragraph w = new Paragraph("Hi", Style.DEFAULT, Alignment.RIGHT, true);
        Buffer b = new Buffer(10, 5);
        w.render(new Rect(0, 0, 10, 5), b);
        assertEquals('H', b.getCell(8, 0).ch());
        assertEquals('i', b.getCell(9, 0).ch());
    }

    @Test
    @DisplayName("render unwrapped text truncation")
    void renderUnwrappedTruncation() {
        Paragraph w = new Paragraph("Hello World", Style.DEFAULT, Alignment.LEFT, false);
        Buffer b = new Buffer(5, 5);
        w.render(new Rect(0, 0, 5, 5), b);
        assertEquals('H', b.getCell(0, 0).ch());
        assertEquals('o', b.getCell(4, 0).ch());
    }
}
