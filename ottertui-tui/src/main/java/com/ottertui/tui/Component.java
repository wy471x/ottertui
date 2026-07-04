package com.ottertui.tui;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Component implements Widget {
    private Component parent;
    private final List<Component> children = new ArrayList<>();
    private boolean focused = false;
    private boolean focusable = false;

    public Component parent() { return parent; }

    public void setParent(Component parent) { this.parent = parent; }

    public List<Component> children() { return children; }

    public void addChild(Component child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Component child) {
        children.remove(child);
        child.setParent(null);
    }

    public boolean isFocused() { return focused; }

    public boolean isFocusable() { return focusable; }

    public void setFocusable(boolean focusable) { this.focusable = focusable; }

    public void requestFocus() {
        if (parent != null) {
            parent.focusChild(this);
        }
    }

    protected void focusChild(Component child) {
        for (var c : children) c.focused = false;
        child.focused = true;
    }

    public Component focusedChild() {
        for (var c : children) {
            if (c.focused) return c;
        }
        return null;
    }

    public void focusNext() {
        if (children.isEmpty()) return;
        int idx = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).focused) {
                idx = i;
                break;
            }
        }
        for (var c : children) c.focused = false;
        // Find next focusable child
        for (int i = 1; i <= children.size(); i++) {
            int next = (idx + i) % children.size();
            if (children.get(next).focusable) {
                children.get(next).focused = true;
                return;
            }
        }
    }

    public boolean onEvent(InputEvent event) {
        for (var child : children) {
            if (child.isFocused() && child.onEvent(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        for (var child : children) {
            child.render(area, buffer);
        }
    }
}
