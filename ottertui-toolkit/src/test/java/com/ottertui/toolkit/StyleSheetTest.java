package com.ottertui.toolkit;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StyleSheetTest {

    @Test
    @DisplayName("empty stylesheet resolves to DEFAULT")
    void emptyResolves() {
        StyleSheet sheet = new StyleSheet();
        Style result = sheet.resolve("Button", "x", Set.of());
        assertEquals(Style.DEFAULT.foreground(), result.foreground());
    }

    @Test
    @DisplayName("addRule and resolve matches by type")
    void addRuleResolveByType() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.type("Button"), Map.of("color", "red"));
        Style result = sheet.resolve("Button", "x", Set.of());
        assertEquals(Color.RED, result.foreground());
    }

    @Test
    @DisplayName("resolve ignores non-matching rules")
    void resolveIgnoresNonMatching() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.type("Button"), Map.of("color", "red"));
        Style result = sheet.resolve("Label", "x", Set.of());
        assertEquals(Color.RESET, result.foreground());
    }

    @Test
    @DisplayName("resolve merges multiple matching rules")
    void resolveMergeMultiple() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.type("Button"), Map.of("color", "red"));
        sheet.addRule(Selector.universal(), Map.of("background", "blue"));
        Style result = sheet.resolve("Button", "x", Set.of());
        assertEquals(Color.RED, result.foreground());
        assertEquals(Color.BLUE, result.background());
    }

    @Test
    @DisplayName("resolve handles named colors")
    void resolveNamedColors() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "green"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.GREEN, result.foreground());
    }

    @Test
    @DisplayName("resolve handles hex colors")
    void resolveHexColors() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "#FF00FF"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(255, 0, 255), result.foreground());
    }

    @Test
    @DisplayName("resolve handles background hex colors")
    void resolveBackgroundHex() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("background", "#AABBCC"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(0xAA, 0xBB, 0xCC), result.background());
    }

    @Test
    @DisplayName("parseColor returns RESET for unknown names")
    void parseColorUnknown() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "unknowncolor"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.RESET, result.foreground());
    }

    @Test
    @DisplayName("border-color property is stored but doesn't affect style")
    void borderColorProperty() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("border-color", "red"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(Style.DEFAULT, result);
    }

    @Test
    @DisplayName("Rule toStyle converts declarations")
    void ruleToStyle() {
        StyleSheet.Rule rule = new StyleSheet.Rule(
            Selector.universal(),
            Map.of("color", "cyan", "background", "black"),
            0
        );
        Style s = rule.toStyle(Map.of());
        assertEquals(Color.CYAN, s.foreground());
        assertEquals(Color.BLACK, s.background());
    }

    @Test
    @DisplayName("resolve with id selector")
    void resolveById() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.id("my-btn"), Map.of("color", "yellow"));
        Style result = sheet.resolve("x", "my-btn", Set.of());
        assertEquals(Color.YELLOW, result.foreground());
    }

    @Test
    @DisplayName("resolve with class selector")
    void resolveByClass() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.clazz("danger"), Map.of("color", "red"));
        Style result = sheet.resolve("x", "y", Set.of("danger"));
        assertEquals(Color.RED, result.foreground());
    }

    @Test
    @DisplayName("resolve all named colors")
    void resolveAllNamedColors() {
        String[] names = {"black", "red", "green", "yellow", "blue", "magenta", "cyan", "white", "gray"};
        Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW,
            Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE, Color.GRAY};

        for (int i = 0; i < names.length; i++) {
            StyleSheet sheet = new StyleSheet();
            sheet.addRule(Selector.universal(), Map.of("color", names[i]));
            Style result = sheet.resolve("x", "y", Set.of());
            assertEquals(colors[i], result.foreground());
        }
    }

    @Test
    @DisplayName("mergeStyles preserves non-Reset foreground from override")
    void mergeStylesPreservesOverride() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "blue"));
        sheet.addRule(Selector.clazz("highlight"), Map.of("color", "red"));
        Style result = sheet.resolve("x", "y", Set.of("highlight"));
        assertEquals(Color.RED, result.foreground());
    }

    @Test
    @DisplayName("resolve merges foreground from one rule and background from another")
    void mergeForegroundAndBackground() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.type("Button"), Map.of("color", "green"));
        sheet.addRule(Selector.clazz("panel"), Map.of("background", "blue"));
        Style result = sheet.resolve("Button", "x", Set.of("panel"));
        assertEquals(Color.GREEN, result.foreground());
        assertEquals(Color.BLUE, result.background());
    }

    @Test
    @DisplayName("parseColor short hex string")
    void parseColorShortHex() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("color", "#123"));
        Style result = sheet.resolve("x", "y", Set.of());
        // #123 → #112233 (each digit doubled)
        assertEquals(new Color.Rgb(0x11, 0x22, 0x33), result.foreground());
    }

    @Test
    @DisplayName("resolve with unknown property ignored")
    void resolveUnknownProperty() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("font-size", "12"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(Style.DEFAULT, result);
    }

    @Test
    @DisplayName("parseColor hex with lowercase letters")
    void parseColorHexLowercase() {
        StyleSheet sheet = new StyleSheet();
        sheet.addRule(Selector.universal(), Map.of("background", "#abcdef"));
        Style result = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(0xAB, 0xCD, 0xEF), result.background());
    }
}
