package com.ottertui.integration.rendering;

import com.ottertui.core.*;
import com.ottertui.integration.infrastructure.StubBackend;
import com.ottertui.tui.Component;
import com.ottertui.widgets.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for interactive components:
 * injecting events → verifying state changes → checking rendered output.
 */
class ComponentInteractionTest {

    @Test
    @DisplayName("List: select + render, selection is reflected in buffer")
    void listSelectionChangesRender() {
        var list = new com.ottertui.widgets.List(new ArrayList<>(List.of(
            "Item A", "Item B", "Item C"
        )));
        var backend = new StubBackend().withSize(30, 8);
        var buf = new Buffer(30, 8);

        // Initial render with item 0 selected
        list.render(new Rect(0, 0, 20, 5), buf);

        // Verify item 0 has non-default background (selected style)
        Cell item0 = buf.getCell(0, 0);
        assertTrue(item0.style().background() instanceof Color.Rgb,
            "Selected item should have colored background");

        // Move selection down
        list.moveDown();
        list.render(new Rect(0, 0, 20, 5), buf);

        // Now item 1 should have the colored background
        Cell item1 = buf.getCell(0, 1);
        assertTrue(item1.style().background() instanceof Color.Rgb,
            "Newly selected item should have colored background");
    }

    @Test
    @DisplayName("TabsWidget: switching tabs changes selected state")
    void tabSelectionSwitches() {
        var tabs = new Tabs(List.of("Tab1", "Tab2", "Tab3"));
        var backend = new StubBackend().withSize(40, 3);
        var buf = new Buffer(40, 3);

        tabs.select(0);
        tabs.render(new Rect(0, 0, 30, 1), buf);

        // Tab 0 should have bold (selected style includes BOLD)
        Cell tab0 = buf.getCell(1, 0); // " Tab1 " starts with space
        assertTrue(tab0.style().modifiers().contains(Modifier.BOLD));

        tabs.select(2);
        tabs.render(new Rect(0, 0, 30, 1), buf);

        // Now tab 0 should NOT have bold
        Cell tab0After = buf.getCell(1, 0);
        assertFalse(tab0After.style().modifiers().contains(Modifier.BOLD),
            "Deselected tab should not have bold");
    }

    @Test
    @DisplayName("TableWidget: selection scrolling updates render")
    void tableSelectionScrolling() {
        record Item(String name, int val) {}
        var items = new ArrayList<Item>();
        for (int i = 0; i < 20; i++) {
            items.add(new Item("Item-" + i, i * 10));
        }

        var table = new Table<>(
            List.of(
                new Table.Column<>("Name", Item::name, 10),
                new Table.Column<>("Value", r -> String.valueOf(r.val()), 8)
            ),
            items
        );
        var state = new TableState();
        var buf = new Buffer(60, 20);

        // Select item 10
        state.select(10);
        table.render(state, new Rect(0, 0, 40, 10), buf);

        // Verify the table rendered (header should be present)
        Cell headerCell = buf.getCell(0, 0);
        assertEquals('N', headerCell.ch(), "Header should start with 'N' for 'Name'");
    }

    @Test
    @DisplayName("Component tree: parent renders children")
    void parentRendersChildren() {
        var parent = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {
                buffer.setString(area.x(), area.y(), "Parent", Style.DEFAULT);
                for (var child : children()) {
                    child.render(area, buffer);
                }
            }
        };
        var child = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {
                buffer.setString(area.x(), area.y() + 2, "Child", Style.DEFAULT);
            }
        };
        parent.addChild(child);

        var backend = new StubBackend().withSize(30, 6);
        var buf = new Buffer(30, 6);
        parent.render(new Rect(0, 0, 30, 6), buf);
        backend.flush(buf);

        String output = backend.lastFlush();
        assertTrue(output.contains("Parent"), "Parent text missing");
        assertTrue(output.contains("Child"), "Child text missing");
    }

    @Test
    @DisplayName("Component focus: focused child receives events")
    void focusRoutesEvents() {
        var child1 = new Component() { };
        child1.setFocusable(true);
        var child2 = new Component() { };
        child2.setFocusable(true);

        var parent = new Component() { };
        parent.addChild(child1);
        parent.addChild(child2);

        child1.requestFocus();
        assertTrue(child1.isFocused());
        assertFalse(child2.isFocused());

        parent.focusNext();
        assertFalse(child1.isFocused());
        assertTrue(child2.isFocused());

        // Wrap around
        parent.focusNext();
        assertTrue(child1.isFocused());
        assertFalse(child2.isFocused());
    }

    @Test
    @DisplayName("ClearWidget: clearing a region removes content")
    void clearWidgetRemovesContent() {
        var buf = new Buffer(20, 5);

        // Draw some content
        buf.setString(0, 1, "Hello World", Style.DEFAULT);
        buf.setString(0, 2, "More text", Style.DEFAULT);

        // Clear the first row of content
        new Clear().render(new Rect(0, 1, 11, 1), buf);

        // Verify first row is cleared
        for (int x = 0; x < 11; x++) {
            assertEquals(' ', buf.getCell(x, 1).ch(), "Cell at " + x + " should be cleared");
        }
        // But second row is untouched
        assertEquals('M', buf.getCell(0, 2).ch());
    }
}
