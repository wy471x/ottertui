package com.ottertui.widgets;

public enum BorderStyle {
    PLAIN("─", "│", "┌", "┐", "└", "┘"),
    ROUNDED("─", "│", "╭", "╮", "╰", "╯"),
    DOUBLE("═", "║", "╔", "╗", "╚", "╝"),
    THICK("━", "┃", "┏", "┓", "┗", "┛");

    final String horizontal;
    final String vertical;
    final String topLeft;
    final String topRight;
    final String bottomLeft;
    final String bottomRight;

    BorderStyle(String h, String v, String tl, String tr, String bl, String br) {
        this.horizontal = h;
        this.vertical = v;
        this.topLeft = tl;
        this.topRight = tr;
        this.bottomLeft = bl;
        this.bottomRight = br;
    }
}
