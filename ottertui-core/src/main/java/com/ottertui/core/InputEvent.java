package com.ottertui.core;

import java.util.List;
import java.util.Set;

public sealed interface InputEvent {
    record KeyEvent(KeyCode code, Set<Modifier> mods, int ch) implements InputEvent {}
    record MouseEvent(MouseButton button, int row, int col, boolean pressed) implements InputEvent {}
    record Resize(int rows, int cols) implements InputEvent {}
    record Unknown() implements InputEvent {}

    static InputEvent key(KeyCode code) {
        return new KeyEvent(code, Set.of(), 0);
    }

    static InputEvent key(KeyCode code, Set<Modifier> mods) {
        return new KeyEvent(code, mods, 0);
    }

    static InputEvent charKey(char c) {
        return new KeyEvent(KeyCode.CHAR, Set.of(), c);
    }

    static InputEvent charKey(char c, Set<Modifier> mods) {
        return new KeyEvent(KeyCode.CHAR, mods, c);
    }

    enum MouseButton { LEFT, MIDDLE, RIGHT, NONE }
}
