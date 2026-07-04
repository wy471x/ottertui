package com.ottertui.core;

public enum KeyCode {
    UP, DOWN, LEFT, RIGHT,
    HOME, END, PAGE_UP, PAGE_DOWN,
    ENTER, TAB, BACKSPACE, ESC, DELETE, INSERT,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
    CHAR;

    public static KeyCode fromChar(char c) { return CHAR; }
}
