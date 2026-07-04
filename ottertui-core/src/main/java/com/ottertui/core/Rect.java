package com.ottertui.core;

public record Rect(int x, int y, int width, int height) {
    public static Rect of(int x, int y, int width, int height) {
        return new Rect(x, y, width, height);
    }

    public Rect inner(int margin) {
        return new Rect(
            x + margin, y + margin,
            Math.max(0, width - 2 * margin),
            Math.max(0, height - 2 * margin)
        );
    }

    public Rect inner(int topBottom, int leftRight) {
        return new Rect(
            x + leftRight, y + topBottom,
            Math.max(0, width - 2 * leftRight),
            Math.max(0, height - 2 * topBottom)
        );
    }
}
