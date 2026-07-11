package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    record Item(String name, int value) {}

    @Test
    @DisplayName("default constructor")
    void defaultConstructor() {
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.of(new Item("foo", 1));
        Table<Item> w = new Table<>(cols, rows);
        assertNotNull(w);
    }

    @Test
    @DisplayName("full constructor with styles")
    void fullConstructor() {
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.<Item>of();
        Table<Item> w = new Table<>(cols, rows,
            new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)),
            new Style(Color.BLACK, Color.WHITE, Set.of()));
        assertNotNull(w);
    }

    @Test
    @DisplayName("render draws header")
    void renderDrawsHeader() {
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.<Item>of();
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('N', b.getCell(0, 0).ch());
        assertEquals('a', b.getCell(1, 0).ch());
        assertEquals('m', b.getCell(2, 0).ch());
        assertEquals('e', b.getCell(3, 0).ch());
    }

    @Test
    @DisplayName("render draws separator after header")
    void renderDrawsSeparator() {
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.<Item>of();
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('─', b.getCell(0, 1).ch());
    }

    @Test
    @DisplayName("render draws data rows")
    void renderDrawsDataRows() {
        Table.Column<Item> nameCol = new Table.Column<>("Name", Item::name, 6);
        Table.Column<Item> valueCol = new Table.Column<>("Value", i -> String.valueOf(i.value()), 6);
        var cols = List.of(nameCol, valueCol);
        var rows = List.of(new Item("foo", 42), new Item("bar", 99));
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        state.select(0);
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('f', b.getCell(0, 2).ch());
        assertEquals('4', b.getCell(7, 2).ch());
        assertEquals('b', b.getCell(0, 3).ch());
    }

    @Test
    @DisplayName("render selected row uses selected style")
    void renderSelectedRowStyle() {
        Style sel = new Style(Color.BLACK, Color.WHITE, Set.of());
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.of(new Item("foo", 1));
        Table<Item> w = new Table<>(cols, rows,
            new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)), sel);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        state.select(0);
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals(sel, b.getCell(0, 2).style());
    }

    @Test
    @DisplayName("render header truncated if too long")
    void renderHeaderTruncated() {
        var cols = List.of(new Table.Column<>("VeryLongHeader", Item::name, 5));
        var rows = List.<Item>of();
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('V', b.getCell(0, 0).ch());
        assertEquals('e', b.getCell(1, 0).ch());
    }

    @Test
    @DisplayName("render with empty rows renders only header and separator")
    void renderEmptyRows() {
        var cols = List.of(new Table.Column<>("Col", Item::name, 10));
        var rows = List.<Item>of();
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('─', b.getCell(0, 1).ch());
        assertEquals(Cell.EMPTY, b.getCell(0, 2));
    }

    @Test
    @DisplayName("render with columns wider than area overflows")
    void renderColumnsOverflowArea() {
        Table.Column<Item> col1 = new Table.Column<>("Col1", Item::name, 20);
        Table.Column<Item> col2 = new Table.Column<>("Col2", i -> String.valueOf(i.value()), 20);
        var cols = List.of(col1, col2);
        var rows = List.of(new Item("foo", 42));
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(30, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 15, 10), b);
        assertEquals('C', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("render in area with height 1")
    void renderHeightOne() {
        var cols = List.of(new Table.Column<>("Name", Item::name, 10));
        var rows = List.of(new Item("foo", 1));
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 1);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 1), b);
        assertEquals('N', b.getCell(0, 0).ch());
    }

    @Test
    @DisplayName("render with cell content longer than column width")
    void renderCellContentTruncated() {
        var cols = List.of(new Table.Column<>("Header", Item::name, 3));
        var rows = List.of(new Item("LongName", 1));
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 10), b);
        assertEquals('L', b.getCell(0, 2).ch());
        assertEquals('o', b.getCell(1, 2).ch());
        assertEquals('n', b.getCell(2, 2).ch());
    }

    @Test
    @DisplayName("render with many rows in small area")
    void renderManyRowsInSmallArea() {
        var cols = List.of(new Table.Column<>("N", Item::name, 5));
        var rows = List.of(
            new Item("a", 1), new Item("b", 2), new Item("c", 3),
            new Item("d", 4), new Item("e", 5)
        );
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(20, 4);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 20, 4), b);
        // Header at y=0, separator at y=1, data at y=2, y=3
        assertEquals('N', b.getCell(0, 0).ch());
        assertEquals('─', b.getCell(0, 1).ch());
        assertEquals('a', b.getCell(0, 2).ch());
        assertEquals('b', b.getCell(0, 3).ch());
    }

    @Test
    @DisplayName("render with multiple wide columns overflowing area")
    void renderMultipleWideColumnsOverflow() {
        Table.Column<Item> c1 = new Table.Column<>("C1", Item::name, 10);
        Table.Column<Item> c2 = new Table.Column<>("C2", i -> String.valueOf(i.value()), 10);
        Table.Column<Item> c3 = new Table.Column<>("C3", i -> String.valueOf(i.value()), 10);
        var cols = List.of(c1, c2, c3);
        var rows = List.of(new Item("foo", 42));
        Table<Item> w = new Table<>(cols, rows);
        Buffer b = new Buffer(40, 10);
        TableState state = new TableState();
        w.render(state, new Rect(0, 0, 15, 10), b);
        assertNotNull(w);
    }
}
