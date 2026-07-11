package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TabsTest {

    @Test
    @DisplayName("constructor with titles")
    void constructorWithTitles() {
        Tabs w = new Tabs(List.of("Tab1", "Tab2"));
        assertNotNull(w);
    }

    @Test
    @DisplayName("default selected is 0")
    void defaultSelected() {
        Tabs w = new Tabs(List.of("A", "B", "C"));
        assertEquals(0, w.selected());
    }

    @Test
    @DisplayName("select valid index")
    void selectValid() {
        Tabs w = new Tabs(List.of("A", "B", "C"));
        w.select(2);
        assertEquals(2, w.selected());
    }

    @Test
    @DisplayName("select invalid index ignored")
    void selectInvalid() {
        Tabs w = new Tabs(List.of("A", "B"));
        w.select(5);
        assertEquals(0, w.selected());
        w.select(-1);
        assertEquals(0, w.selected());
    }

    @Test
    @DisplayName("render draws tabs")
    void renderDrawsTabs() {
        Tabs w = new Tabs(List.of("Tab1", "Tab2"));
        Buffer b = new Buffer(20, 3);
        w.render(new Rect(0, 0, 20, 3), b);
        assertEquals(' ', b.getCell(0, 0).ch());
        assertEquals('T', b.getCell(1, 0).ch());
        assertEquals('1', b.getCell(4, 0).ch());
    }

    @Test
    @DisplayName("render selected tab uses selected style")
    void renderSelectedStyle() {
        Style sel = new Style(Color.BLACK, Color.WHITE, Set.of(Modifier.BOLD));
        Style norm = Style.DEFAULT;
        Tabs w = new Tabs(List.of("A", "B"), sel, norm);
        w.select(1);
        Buffer b = new Buffer(20, 3);
        w.render(new Rect(0, 0, 20, 3), b);
        assertEquals(norm, b.getCell(1, 0).style());
        assertEquals(sel, b.getCell(4, 0).style());
    }

    @Test
    @DisplayName("render clips at area width")
    void renderClipsAtWidth() {
        Tabs w = new Tabs(List.of("Tab1", "Tab2", "Tab3"));
        Buffer b = new Buffer(10, 3);
        w.render(new Rect(0, 0, 10, 3), b);
        assertEquals(' ', b.getCell(0, 0).ch());
        assertEquals('T', b.getCell(1, 0).ch());
    }

    @Test
    @DisplayName("render with custom styles")
    void renderCustomStyles() {
        Style sel = new Style(Color.BLACK, Color.CYAN, Set.of());
        Style norm = new Style(Color.WHITE, Color.RESET, Set.of());
        Tabs w = new Tabs(List.of("Tab"), sel, norm);
        Buffer b = new Buffer(10, 1);
        w.render(new Rect(0, 0, 10, 1), b);
        assertEquals(sel, b.getCell(1, 0).style());
    }
}
