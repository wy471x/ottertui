package com.ottertui.core;

import java.util.Optional;

public interface TerminalBackend {
    void flush(Buffer buffer);
    TerminalSize size();
    void enterRawMode();
    void exitRawMode();
    Optional<InputEvent> readInput();
    void showCursor();
    void hideCursor();
    void clearScreen();

    record TerminalSize(int width, int height) {}
}
