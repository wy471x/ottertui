package com.ottertui.backend.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;

final class LibC {
    static final SymbolLookup LOOKUP = load();

    private static SymbolLookup load() {
        try {
            return SymbolLookup.libraryLookup("c", Arena.global());
        } catch (IllegalArgumentException e) {
            // macOS JDK 25+ no longer maps "c" to libSystem; use the real path
            return SymbolLookup.libraryLookup("/usr/lib/libSystem.B.dylib", Arena.global());
        }
    }

    private LibC() {}
}
