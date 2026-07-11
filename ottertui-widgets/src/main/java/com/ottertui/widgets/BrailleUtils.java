package com.ottertui.widgets;

final class BrailleUtils {
    private static final char BRAILLE_BASE = '\u2800';

    private BrailleUtils() {}

    static int cellToDotX(int cellX) {
        return cellX * 2;
    }

    static int cellToDotY(int cellY) {
        return cellY * 4;
    }

    static char toBrailleChar(boolean[] dots) {
        int pattern = 0;
        for (int i = 0; i < 8 && i < dots.length; i++) {
            if (dots[i]) {
                pattern |= (1 << i);
            }
        }
        return (char) (BRAILLE_BASE | pattern);
    }
}
