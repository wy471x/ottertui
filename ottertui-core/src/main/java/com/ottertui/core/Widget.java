package com.ottertui.core;

@FunctionalInterface
public interface Widget {
    void render(Rect area, Buffer buffer);
}
