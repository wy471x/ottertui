package com.ottertui.core;

public record Cell(char ch, Style style) {
    public static final Cell EMPTY = new Cell(' ', Style.DEFAULT);
}
