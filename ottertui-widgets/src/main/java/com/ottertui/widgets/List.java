package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.Set;

public class List implements Widget {
    private final java.util.List<String> items;
    private int selectedIndex = 0;
    private final Style selectedStyle;
    private final Style normalStyle;

    public List(java.util.List<String> items) {
        this(items, new Style(Color.BLACK, Color.WHITE, Set.of()),
            Style.DEFAULT);
    }

    public List(java.util.List<String> items, Style selectedStyle, Style normalStyle) {
        this.items = items;
        this.selectedStyle = selectedStyle;
        this.normalStyle = normalStyle;
    }

    public void select(int index) {
        if (index >= 0 && index < items.size()) {
            this.selectedIndex = index;
        }
    }

    public void moveUp() {
        if (selectedIndex > 0) selectedIndex--;
    }

    public void moveDown() {
        if (selectedIndex < items.size() - 1) selectedIndex++;
    }

    public int selectedIndex() { return selectedIndex; }
    public String selectedItem() { return items.get(selectedIndex); }

    @Override
    public void render(Rect area, Buffer buffer) {
        for (int i = 0; i < items.size() && i < area.height(); i++) {
            String item = items.get(i);
            String display = item.length() > area.width()
                ? item.substring(0, area.width()) : item;
            Style s = (i == selectedIndex) ? selectedStyle : normalStyle;
            buffer.setString(area.x(), area.y() + i, display, s);
            // Pad remaining width
            for (int x = area.x() + display.length(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, area.y() + i, new Cell(' ', s));
            }
        }
    }
}
