package com.ottertui.core;

public interface StatefulWidget<S> {
    void render(S state, Rect area, Buffer buffer);
}
