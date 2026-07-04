package com.ottertui.core;

public final class CharWidth {

    private CharWidth() {}

    public static int displayWidth(String s) {
        int width = 0;
        int[] codePoints = s.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {
            int cp = codePoints[i];

            if (isEmojiZWJSequence(codePoints, i)) {
                width += 2;
                while (i + 1 < codePoints.length
                        && isEmojiModifier(codePoints[i + 1])) {
                    i++;
                }
                continue;
            }

            width += codePointWidth(cp);
        }
        return width;
    }

    static int codePointWidth(int cp) {
        if (cp <= 0x1F || (cp >= 0x7F && cp <= 0xA0)) {
            return cp == '\t' ? 4 : 0;
        }
        if (cp == 0x200B) return 0;

        if (isEastAsianWide(cp)) return 2;

        return 1;
    }

    private static boolean isEastAsianWide(int cp) {
        return (cp >= 0x1100 && cp <= 0x115F)
            || (cp >= 0x2E80 && cp <= 0xA4CF)
            || (cp >= 0xAC00 && cp <= 0xD7A3)
            || (cp >= 0xF900 && cp <= 0xFAFF)
            || (cp >= 0xFE10 && cp <= 0xFE19)
            || (cp >= 0xFE30 && cp <= 0xFE6F)
            || (cp >= 0xFF01 && cp <= 0xFF60)
            || (cp >= 0xFFE0 && cp <= 0xFFE6)
            || (cp >= 0x1F004 && cp <= 0x1F9FF)
            || (cp >= 0x20000 && cp <= 0x2FFFD)
            || (cp >= 0x30000 && cp <= 0x3FFFD);
    }

    private static boolean isEmojiZWJSequence(int[] cps, int start) {
        if (start + 1 >= cps.length) return false;
        int cp = cps[start];
        return (cp >= 0x1F300 && cp <= 0x1FAFF)
            || cp == 0x2764;
    }

    private static boolean isEmojiModifier(int cp) {
        return cp == 0x200D
            || cp == 0xFE0F
            || (cp >= 0x1F3FB && cp <= 0x1F3FF)
            || (cp >= 0x1F1E6 && cp <= 0x1F1FF);
    }
}
