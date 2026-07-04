package com.ottertui.toolkit;

import com.ottertui.core.*;

import java.util.*;

public class StyleSheet {
    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, Color> variables = new HashMap<>();

    public record Rule(Selector selector, Map<String, String> declarations) {
        public Style toStyle() {
            var style = Style.DEFAULT;
            for (var entry : declarations.entrySet()) {
                style = applyDeclaration(style, entry.getKey(), entry.getValue());
            }
            return style;
        }
    }

    public void addRule(Selector selector, Map<String, String> declarations) {
        rules.add(new Rule(selector, declarations));
    }

    public Style resolve(String widgetType, String id, Set<String> classes) {
        var style = Style.DEFAULT;
        for (var rule : rules) {
            if (rule.selector().matches(widgetType, id, classes)) {
                style = mergeStyles(style, rule.toStyle());
            }
        }
        return style;
    }

    private static Style mergeStyles(Style base, Style over) {
        var result = base;
        if (!(over.foreground() instanceof Color.Reset)) {
            result = result.fg(over.foreground());
        }
        if (!(over.background() instanceof Color.Reset)) {
            result = result.bg(over.background());
        }
        var mods = new HashSet<>(result.modifiers());
        mods.addAll(over.modifiers());
        return new Style(result.foreground(), result.background(), mods);
    }

    private static Style applyDeclaration(Style style, String property, String value) {
        return switch (property) {
            case "color" -> style.fg(parseColor(value));
            case "background" -> style.bg(parseColor(value));
            case "border-color" -> style; // stored separately
            default -> style;
        };
    }

    private static Color parseColor(String value) {
        if (value.startsWith("#") && value.length() == 7) {
            int r = Integer.parseInt(value.substring(1, 3), 16);
            int g = Integer.parseInt(value.substring(3, 5), 16);
            int b = Integer.parseInt(value.substring(5, 7), 16);
            return new Color.Rgb(r, g, b);
        }
        return switch (value.toLowerCase()) {
            case "black"   -> Color.BLACK;
            case "red"     -> Color.RED;
            case "green"   -> Color.GREEN;
            case "yellow"  -> Color.YELLOW;
            case "blue"    -> Color.BLUE;
            case "magenta" -> Color.MAGENTA;
            case "cyan"    -> Color.CYAN;
            case "white"   -> Color.WHITE;
            case "gray"    -> Color.GRAY;
            default        -> Color.RESET;
        };
    }
}
