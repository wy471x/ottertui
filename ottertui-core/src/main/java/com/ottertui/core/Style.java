package com.ottertui.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record Style(
    Color foreground,
    Color background,
    Set<Modifier> modifiers
) {
    public static final Style DEFAULT = new Style(
        Color.RESET, Color.RESET, Set.of()
    );

    public Style {
        modifiers = Collections.unmodifiableSet(new HashSet<>(modifiers));
    }

    public Style fg(Color c) { return new Style(c, background, modifiers); }
    public Style bg(Color c) { return new Style(foreground, c, modifiers); }

    public Style bold() { return addModifier(Modifier.BOLD); }
    public Style dim() { return addModifier(Modifier.DIM); }
    public Style italic() { return addModifier(Modifier.ITALIC); }
    public Style underline() { return addModifier(Modifier.UNDERLINE); }
    public Style reversed() { return addModifier(Modifier.REVERSED); }
    public Style crossedOut() { return addModifier(Modifier.CROSSED_OUT); }

    private Style addModifier(Modifier m) {
        var mods = new HashSet<>(modifiers);
        mods.add(m);
        return new Style(foreground, background, mods);
    }

    public Style removeModifier(Modifier m) {
        var mods = new HashSet<>(modifiers);
        mods.remove(m);
        return new Style(foreground, background, mods);
    }
}
