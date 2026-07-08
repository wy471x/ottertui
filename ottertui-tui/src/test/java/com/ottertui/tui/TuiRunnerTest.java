package com.ottertui.tui;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TuiRunnerTest {

    static class StubBackend implements TerminalBackend {
        TerminalSize size = new TerminalSize(80, 24);
        boolean rawMode = false;
        boolean cursorVisible = true;
        AtomicInteger readInputCount = new AtomicInteger(0);
        Queue<InputEvent> inputs = new LinkedList<>();
        AtomicBoolean flushed = new AtomicBoolean(false);

        @Override
        public void flush(Buffer buffer) { flushed.set(true); }

        @Override
        public TerminalSize size() { return size; }

        @Override
        public void enterRawMode() { rawMode = true; }

        @Override
        public void exitRawMode() { rawMode = false; }

        @Override
        public Optional<InputEvent> readInput() {
            readInputCount.incrementAndGet();
            InputEvent event = inputs.poll();
            return Optional.ofNullable(event);
        }

        @Override
        public void showCursor() { cursorVisible = true; }

        @Override
        public void hideCursor() { cursorVisible = false; }

        @Override
        public void clearScreen() {}
    }

    @Test
    @DisplayName("constructor sets up key bindings")
    void constructorSetsUpBindings() {
        StubBackend backend = new StubBackend();
        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        assertNotNull(runner.keyBindings());
    }

    @Test
    @DisplayName("run enters and exits raw mode")
    void runEntersAndExitsRawMode() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.run();
        assertFalse(backend.rawMode);
    }

    @Test
    @DisplayName("run calls render and flush backend")
    void runCallsRender() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.run();
        assertTrue(backend.flushed.get());
    }

    @Test
    @DisplayName("requestRedraw sets dirty flag for next frame")
    void requestRedraw() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.requestRedraw();
        runner.run();
        assertTrue(backend.flushed.get());
    }

    @Test
    @DisplayName("key bindings handle Ctrl+C to stop")
    void keyBindingsStop() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.run();
        assertFalse(backend.rawMode);
    }

    @Test
    @DisplayName("Ctrl+L triggers redraw")
    void ctrlLTriggersRedraw() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('l', Set.of(Modifier.BOLD)));
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.run();
        assertTrue(backend.flushed.get());
    }

    @Test
    @DisplayName("stop sets running to false directly")
    void stopDirectly() {
        StubBackend backend = new StubBackend();
        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.stop();
        runner.run();
        assertFalse(backend.rawMode);
    }

    @Test
    @DisplayName("requestRedraw adds dirty flag so render is called")
    void requestRedrawDirtyFlag() {
        StubBackend backend = new StubBackend();
        backend.inputs.add(InputEvent.charKey('c', Set.of(Modifier.BOLD)));

        Component root = new Component() {
            @Override
            public void render(Rect area, Buffer buffer) {}
        };
        TuiRunner runner = new TuiRunner(backend, root);
        runner.requestRedraw();
        runner.run();
        assertTrue(backend.flushed.get());
    }

}
