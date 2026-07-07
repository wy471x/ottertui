package com.ottertui.toolkit;

import com.ottertui.core.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StyleSheet {
    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, String> variables = new HashMap<>();

    public record Rule(Selector selector, Map<String, String> declarations, int specificity) {
        public Style toStyle(Map<String, String> variables) {
            var style = Style.DEFAULT;
            for (var entry : declarations.entrySet()) {
                String value = resolveVariable(entry.getValue(), variables);
                style = applyDeclaration(style, entry.getKey(), value);
            }
            return style;
        }

        private static String resolveVariable(String value, Map<String, String> vars) {
            if (value.startsWith("var(") && value.endsWith(")")) {
                String name = value.substring(4, value.length() - 1).trim();
                if (vars.containsKey(name)) {
                    return vars.get(name);
                }
            }
            return value;
        }
    }

    public void addRule(Selector selector, Map<String, String> declarations) {
        addRule(selector, declarations, computeSpecificity(selector));
    }

    public void addRule(Selector selector, Map<String, String> declarations, int specificity) {
        rules.add(new Rule(selector, declarations, specificity));
    }

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    public Map<String, String> variables() {
        return Collections.unmodifiableMap(variables);
    }

    /** Load a stylesheet from a .tcss file. */
    public static StyleSheet load(Path path) throws IOException {
        String source = Files.readString(path);
        return TcssParser.parse(source);
    }

    /** Parse a stylesheet from a CSS string. */
    public static StyleSheet fromString(String css) {
        return TcssParser.parse(css);
    }

    /** Resolve style including pseudo-classes. */
    public Style resolve(String widgetType, String id,
                         Set<String> classes, Set<String> pseudoClasses) {
        var style = Style.DEFAULT;
        for (var rule : rules) {
            if (rule.selector().matches(widgetType, id, classes, pseudoClasses)) {
                style = mergeStyles(style, rule.toStyle(variables), rule.specificity());
            }
        }
        return style;
    }

    /** Resolve style without pseudo-classes (backward-compatible). */
    public Style resolve(String widgetType, String id, Set<String> classes) {
        return resolve(widgetType, id, classes, Set.of());
    }

    private static Style mergeStyles(Style base, Style over, int specificity) {
        // Higher specificity wins; later rules at the same specificity win.
        // Since rules are processed in order and later rules override,
        // this is a simple merge where 'over' property values take precedence
        // if they are not RESET.
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

    private static Style mergeStyles(Style base, Style over) {
        return mergeStyles(base, over, 0);
    }

    private static Style applyDeclaration(Style style, String property, String value) {
        return switch (property) {
            case "color"        -> style.fg(parseColor(value));
            case "background"   -> style.bg(parseColor(value));
            case "border-color" -> style;
            case "bold"         -> "true".equalsIgnoreCase(value) ? style.bold() : style;
            case "italic"       -> "true".equalsIgnoreCase(value) ? style.italic() : style;
            case "underline"    -> "true".equalsIgnoreCase(value) ? style.underline() : style;
            case "dim"          -> "true".equalsIgnoreCase(value) ? style.dim() : style;
            case "reversed"     -> "true".equalsIgnoreCase(value) ? style.reversed() : style;
            default             -> style;
        };
    }

    static int computeSpecificity(Selector selector) {
        return switch (selector) {
            case Selector.Universal u -> 0;
            case Selector.Type t      -> 1;
            case Selector.ClassSelector c -> 10;
            case Selector.PseudoClass p   -> 10;
            case Selector.Id i            -> 100;
            case Selector.Compound comp -> comp.selectors().stream()
                .mapToInt(StyleSheet::computeSpecificity).sum();
        };
    }

    static Color parseColor(String value) {
        if (value.startsWith("#") && value.length() == 7) {
            try {
                int r = Integer.parseInt(value.substring(1, 3), 16);
                int g = Integer.parseInt(value.substring(3, 5), 16);
                int b = Integer.parseInt(value.substring(5, 7), 16);
                return new Color.Rgb(r, g, b);
            } catch (NumberFormatException e) {
                return Color.RESET;
            }
        }
        if (value.startsWith("#") && value.length() == 4) {
            try {
                int r = Integer.parseInt(value.substring(1, 2), 16) * 17;
                int g = Integer.parseInt(value.substring(2, 3), 16) * 17;
                int b = Integer.parseInt(value.substring(3, 4), 16) * 17;
                return new Color.Rgb(r, g, b);
            } catch (NumberFormatException e) {
                return Color.RESET;
            }
        }
        // rgb(r, g, b) format
        if (value.startsWith("rgb(") && value.endsWith(")")) {
            String[] parts = value.substring(4, value.length() - 1).split(",");
            if (parts.length == 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    return new Color.Rgb(
                        Math.clamp(r, 0, 255),
                        Math.clamp(g, 0, 255),
                        Math.clamp(b, 0, 255));
                } catch (NumberFormatException e) {
                    return Color.RESET;
                }
            }
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
            case "dark-gray", "darkgray" -> Color.DARK_GRAY;
            case "light-red", "lightred"     -> Color.LIGHT_RED;
            case "light-green", "lightgreen" -> Color.LIGHT_GREEN;
            case "light-yellow", "lightyellow" -> Color.LIGHT_YELLOW;
            case "light-blue", "lightblue"     -> Color.LIGHT_BLUE;
            case "light-magenta", "lightmagenta" -> Color.LIGHT_MAGENTA;
            case "light-cyan", "lightcyan"       -> Color.LIGHT_CYAN;
            default        -> Color.RESET;
        };
    }
}
