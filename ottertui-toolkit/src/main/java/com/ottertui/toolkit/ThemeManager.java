package com.ottertui.toolkit;

import com.ottertui.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThemeManager {
    private static final Map<String, StyleSheet> themes = new HashMap<>();
    private static StyleSheet active;

    public static void loadTheme(String name, StyleSheet sheet) {
        themes.put(name, sheet);
    }

    public static void activate(String name) {
        active = themes.get(name);
    }

    public static Style resolve(String widgetType, String id, Set<String> classes) {
        return active != null ? active.resolve(widgetType, id, classes) : Style.DEFAULT;
    }
}
