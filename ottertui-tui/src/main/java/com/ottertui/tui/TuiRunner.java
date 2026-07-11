package com.ottertui.tui;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

public class TuiRunner {
    private final TerminalBackend backend;
    private final Component root;
    private final KeyBindings keyBindings;
    private volatile boolean running = true;
    private volatile boolean dirty = true;
    private long lastRenderTime = 0;
    private static final long FRAME_INTERVAL = 16; // ~60fps

    public TuiRunner(TerminalBackend backend, Component root) {
        this.backend = backend;
        this.root = root;
        this.keyBindings = new KeyBindings();

        keyBindings.bind(KeyCode.CHAR, Set.of(Modifier.BOLD), 'c', this::stop);
        keyBindings.bind(KeyCode.CHAR, Set.of(Modifier.BOLD), 'l', () -> dirty = true);
    }

    public KeyBindings keyBindings() { return keyBindings; }

    public void run() {
        backend.enterRawMode();
        backend.hideCursor();
        backend.clearScreen();
        try {
            while (running) {
                long now = System.currentTimeMillis();

                List<InputEvent> events = drainInput();
                for (var event : events) {
                    if (keyBindings.handle(event)) continue;
                    root.onEvent(event);
                }

                if (dirty || (now - lastRenderTime >= FRAME_INTERVAL)) {
                    render();
                    dirty = false;
                    lastRenderTime = now;
                }

                long elapsed = System.currentTimeMillis() - now;
                if (elapsed < FRAME_INTERVAL) {
                    LockSupport.parkNanos((FRAME_INTERVAL - elapsed) * 1_000_000);
                }
            }
        } finally {
            backend.showCursor();
            backend.exitRawMode();
        }
    }

    public void requestRedraw() {
        this.dirty = true;
    }

    public void stop() {
        this.running = false;
    }

    private void render() {
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) return;
        var buffer = new Buffer(size.width(), size.height());
        root.render(new Rect(0, 0, size.width(), size.height()), buffer);
        backend.flush(buffer);
    }

    private List<InputEvent> drainInput() {
        var events = new ArrayList<InputEvent>();
        while (true) {
            var event = backend.readInput();
            if (event.isEmpty()) break;
            events.add(event.get());
        }
        return events;
    }
}
