package com.ottertui.widgets;

public class TableState {
    private int selectedIndex = 0;

    public int selectedIndex() { return selectedIndex; }

    public void select(int index) { this.selectedIndex = index; }

    public void moveUp() { if (selectedIndex > 0) selectedIndex--; }

    public void moveDown() { selectedIndex++; }
}
