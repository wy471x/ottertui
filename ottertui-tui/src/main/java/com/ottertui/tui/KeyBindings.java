package com.ottertui.tui;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

public class KeyBindings {
    private final List<Binding> bindings = new ArrayList<>();

    public record Binding(KeyCode code, Set<Modifier> mods, int ch, Runnable action) {}

    public void bind(KeyCode code, Set<Modifier> mods, int ch, Runnable action) {
        bindings.add(new Binding(code, mods, ch, action));
    }

    public void bind(KeyCode code, Runnable action) {
        bindings.add(new Binding(code, Set.of(), 0, action));
    }

    public boolean handle(InputEvent event) {
        if (!(event instanceof InputEvent.KeyEvent k)) return false;

        for (var b : bindings) {
            if (b.code() == k.code()
                    && b.mods().equals(k.mods())
                    && (b.ch() == 0 || b.ch() == k.ch())) {
                b.action().run();
                return true;
            }
        }
        return false;
    }
}
