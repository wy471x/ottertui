package com.ottertui.toolkit;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TcssParserTest {

    // --- Simple selectors ---

    @Test
    @DisplayName("parse universal selector")
    void universalSelector() {
        StyleSheet sheet = StyleSheet.fromString("* { color: red; }");
        Style s = sheet.resolve("anything", "x", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("parse type selector")
    void typeSelector() {
        StyleSheet sheet = StyleSheet.fromString("Button { color: blue; }");
        Style s = sheet.resolve("Button", "x", Set.of());
        assertEquals(Color.BLUE, s.foreground());
    }

    @Test
    @DisplayName("parse id selector")
    void idSelector() {
        StyleSheet sheet = StyleSheet.fromString("#submit { color: green; }");
        Style s = sheet.resolve("x", "submit", Set.of());
        assertEquals(Color.GREEN, s.foreground());
    }

    @Test
    @DisplayName("parse class selector")
    void classSelector() {
        StyleSheet sheet = StyleSheet.fromString(".primary { color: yellow; }");
        Style s = sheet.resolve("x", "y", Set.of("primary"));
        assertEquals(Color.YELLOW, s.foreground());
    }

    @Test
    @DisplayName("parse pseudo-class selector")
    void pseudoSelector() {
        StyleSheet sheet = StyleSheet.fromString("Button:hover { color: magenta; }");
        Style s = sheet.resolve("Button", "x", Set.of(), Set.of("hover"));
        assertEquals(Color.MAGENTA, s.foreground());
    }

    // --- Compound selectors ---

    @Test
    @DisplayName("parse compound selector type#id")
    void compoundTypeAndId() {
        StyleSheet sheet = StyleSheet.fromString("Button#ok { color: red; }");
        Style s = sheet.resolve("Button", "ok", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("parse compound selector type.class")
    void compoundTypeAndClass() {
        StyleSheet sheet = StyleSheet.fromString("Button.primary { color: blue; }");
        Style s = sheet.resolve("Button", "x", Set.of("primary"));
        assertEquals(Color.BLUE, s.foreground());
    }

    @Test
    @DisplayName("parse compound selector type#id.class:pseudo")
    void compoundFull() {
        StyleSheet sheet = StyleSheet.fromString("Button#ok.primary:hover { color: cyan; }");
        Style s = sheet.resolve("Button", "ok", Set.of("primary"), Set.of("hover"));
        assertEquals(Color.CYAN, s.foreground());
    }

    @Test
    @DisplayName("compound selector with only id and class")
    void compoundIdAndClass() {
        StyleSheet sheet = StyleSheet.fromString("#main.dark { background: black; }");
        Style s = sheet.resolve("x", "main", Set.of("dark"));
        assertEquals(Color.BLACK, s.background());
    }

    // --- Selector lists (comma-separated) ---

    @Test
    @DisplayName("parse comma-separated selectors")
    void commaSelectors() {
        StyleSheet sheet = StyleSheet.fromString("Button, Label { color: red; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
        assertEquals(Color.RED, sheet.resolve("Label", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("parse three comma-separated selectors")
    void threeCommaSelectors() {
        StyleSheet sheet = StyleSheet.fromString("A, B, C { color: green; }");
        assertEquals(Color.GREEN, sheet.resolve("A", "x", Set.of()).foreground());
        assertEquals(Color.GREEN, sheet.resolve("B", "x", Set.of()).foreground());
        assertEquals(Color.GREEN, sheet.resolve("C", "x", Set.of()).foreground());
    }

    // --- Multiple rules ---

    @Test
    @DisplayName("parse multiple rules")
    void multipleRules() {
        StyleSheet sheet = StyleSheet.fromString(
            "Button { color: red; }\nLabel { color: blue; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
        assertEquals(Color.BLUE, sheet.resolve("Label", "x", Set.of()).foreground());
    }

    // --- Variables ---

    @Test
    @DisplayName("parse :root variable block")
    void rootVariables() {
        StyleSheet sheet = StyleSheet.fromString(
            ":root { --primary: red; }\nButton { color: var(--primary); }");
        Style s = sheet.resolve("Button", "x", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("parse multiple variables in :root")
    void multipleRootVariables() {
        StyleSheet sheet = StyleSheet.fromString(
            ":root {\n  --fg: green;\n  --bg: black;\n}\n* { color: var(--fg); background: var(--bg); }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.GREEN, s.foreground());
        assertEquals(Color.BLACK, s.background());
    }

    @Test
    @DisplayName("variables not found fall back to raw value")
    void varNotFound() {
        StyleSheet sheet = StyleSheet.fromString("* { color: var(--missing); }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.RESET, s.foreground());
    }

    // --- Colors ---

    @Test
    @DisplayName("parse 6-digit hex color")
    void hexColor6() {
        StyleSheet sheet = StyleSheet.fromString("* { color: #FF8844; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(0xFF, 0x88, 0x44), s.foreground());
    }

    @Test
    @DisplayName("parse 3-digit hex color")
    void hexColor3() {
        StyleSheet sheet = StyleSheet.fromString("* { color: #F80; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(0xFF, 0x88, 0x00), s.foreground());
    }

    @Test
    @DisplayName("parse rgb() color")
    void rgbColor() {
        StyleSheet sheet = StyleSheet.fromString("* { color: rgb(100, 200, 50); }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(100, 200, 50), s.foreground());
    }

    @Test
    @DisplayName("parse rgb() color with clamping")
    void rgbColorClamp() {
        StyleSheet sheet = StyleSheet.fromString("* { color: rgb(300, -10, 128); }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(255, 0, 128), s.foreground());
    }

    @Test
    @DisplayName("parse all named CSS colors")
    void allNamedColors() {
        var tests = new java.util.LinkedHashMap<String, Color>();
        tests.put("black", Color.BLACK);
        tests.put("red", Color.RED);
        tests.put("green", Color.GREEN);
        tests.put("yellow", Color.YELLOW);
        tests.put("blue", Color.BLUE);
        tests.put("magenta", Color.MAGENTA);
        tests.put("cyan", Color.CYAN);
        tests.put("white", Color.WHITE);
        tests.put("gray", Color.GRAY);
        tests.put("dark-gray", Color.DARK_GRAY);
        tests.put("darkgray", Color.DARK_GRAY);
        tests.put("light-red", Color.LIGHT_RED);
        tests.put("lightred", Color.LIGHT_RED);
        tests.put("light-green", Color.LIGHT_GREEN);
        tests.put("lightgreen", Color.LIGHT_GREEN);
        tests.put("light-yellow", Color.LIGHT_YELLOW);
        tests.put("lightyellow", Color.LIGHT_YELLOW);
        tests.put("light-blue", Color.LIGHT_BLUE);
        tests.put("lightblue", Color.LIGHT_BLUE);
        tests.put("light-magenta", Color.LIGHT_MAGENTA);
        tests.put("lightmagenta", Color.LIGHT_MAGENTA);
        tests.put("light-cyan", Color.LIGHT_CYAN);
        tests.put("lightcyan", Color.LIGHT_CYAN);
        for (var e : tests.entrySet()) {
            StyleSheet sheet = StyleSheet.fromString("* { color: " + e.getKey() + "; }");
            assertEquals(e.getValue(), sheet.resolve("x", "y", Set.of()).foreground(),
                "Failed for color: " + e.getKey());
        }
    }

    // --- Modifier properties ---

    @Test
    @DisplayName("parse bold property")
    void boldProperty() {
        StyleSheet sheet = StyleSheet.fromString("* { bold: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.BOLD));
    }

    @Test
    @DisplayName("parse italic property")
    void italicProperty() {
        StyleSheet sheet = StyleSheet.fromString("* { italic: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.ITALIC));
    }

    @Test
    @DisplayName("parse underline property")
    void underlineProperty() {
        StyleSheet sheet = StyleSheet.fromString("* { underline: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.UNDERLINE));
    }

    @Test
    @DisplayName("parse dim property")
    void dimProperty() {
        StyleSheet sheet = StyleSheet.fromString("* { dim: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.DIM));
    }

    @Test
    @DisplayName("parse reversed property")
    void reversedProperty() {
        StyleSheet sheet = StyleSheet.fromString("* { reversed: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.REVERSED));
    }

    @Test
    @DisplayName("bold false does not add modifier")
    void boldFalse() {
        StyleSheet sheet = StyleSheet.fromString("* { bold: false; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertFalse(s.modifiers().contains(Modifier.BOLD));
    }

    @Test
    @DisplayName("modifiers are additive across rules")
    void modifiersAdditive() {
        StyleSheet sheet = StyleSheet.fromString(
            "* { bold: true; }\n* { italic: true; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertTrue(s.modifiers().contains(Modifier.BOLD));
        assertTrue(s.modifiers().contains(Modifier.ITALIC));
    }

    // --- Comments ---

    @Test
    @DisplayName("skip block comments")
    void blockComments() {
        StyleSheet sheet = StyleSheet.fromString(
            "/* This is a comment */\nButton { color: red; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("skip inline comments")
    void inlineComments() {
        StyleSheet sheet = StyleSheet.fromString(
            "Button { /* color: blue; */ color: red; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("skip comments between selectors")
    void commentBetweenSelectors() {
        StyleSheet sheet = StyleSheet.fromString(
            "/* header */\nButton { color: red; }\n/* footer */\nLabel { color: blue; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
        assertEquals(Color.BLUE, sheet.resolve("Label", "x", Set.of()).foreground());
    }

    // --- Edge cases ---

    @Test
    @DisplayName("empty string produces empty stylesheet")
    void emptyString() {
        StyleSheet sheet = StyleSheet.fromString("");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("whitespace only")
    void whitespaceOnly() {
        StyleSheet sheet = StyleSheet.fromString("   \n  \t  ");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName("missing semicolons still work")
    void missingSemicolon() {
        StyleSheet sheet = StyleSheet.fromString("Button { color: red background: blue; }");
        Style s = sheet.resolve("Button", "x", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("declaration without value")
    void declarationWithoutValue() {
        StyleSheet sheet = StyleSheet.fromString("* { bold; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertNotNull(s);
    }

    @Test
    @DisplayName("empty :root block")
    void emptyRoot() {
        StyleSheet sheet = StyleSheet.fromString(":root { } Button { color: red; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("var() reference without dashes")
    void varWithoutDashes() {
        StyleSheet sheet = StyleSheet.fromString(
            ":root { --primary: blue; }\n* { color: var(primary); }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.BLUE, s.foreground());
    }

    @Test
    @DisplayName("selector with underscore")
    void selectorWithUnderscore() {
        StyleSheet sheet = StyleSheet.fromString("my_button { color: red; }");
        assertEquals(Color.RED, sheet.resolve("my_button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("selector with hyphen")
    void selectorWithHyphen() {
        StyleSheet sheet = StyleSheet.fromString("my-button { color: blue; }");
        assertEquals(Color.BLUE, sheet.resolve("my-button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("rule with no declarations")
    void ruleNoDeclarations() {
        StyleSheet sheet = StyleSheet.fromString("Button { }");
        Style s = sheet.resolve("Button", "x", Set.of());
        assertEquals(Style.DEFAULT, s);
    }

    @Test
    @DisplayName(":root with nested comment")
    void rootWithComment() {
        StyleSheet sheet = StyleSheet.fromString(
            ":root { /* vars */ --c: red; }\n* { color: var(--c); }");
        assertEquals(Color.RED, sheet.resolve("x", "y", Set.of()).foreground());
    }

    @Test
    @DisplayName("multiple :root blocks accumulate variables")
    void multipleRootBlocks() {
        StyleSheet sheet = StyleSheet.fromString(
            ":root { --a: red; }\n:root { --b: blue; }\nButton { color: var(--a); }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("upper case hex color")
    void upperCaseHex() {
        StyleSheet sheet = StyleSheet.fromString("* { color: #ABCDEF; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(new Color.Rgb(0xAB, 0xCD, 0xEF), s.foreground());
    }

    @Test
    @DisplayName("mixed case named color")
    void mixedCaseNamed() {
        StyleSheet sheet = StyleSheet.fromString("* { color: ReD; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.RED, s.foreground());
    }

    @Test
    @DisplayName("trailing semicolons in block")
    void trailingSemicolons() {
        StyleSheet sheet = StyleSheet.fromString("* { color: red;;; background: blue; }");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Color.RED, s.foreground());
        assertEquals(Color.BLUE, s.background());
    }

    @Test
    @DisplayName("specificity: id overrides type")
    void specificityIdOverType() {
        StyleSheet sheet = StyleSheet.fromString(
            "Button { color: red; }\n#ok { color: blue; }");
        Style s = sheet.resolve("Button", "ok", Set.of());
        assertEquals(Color.BLUE, s.foreground());
    }

    @Test
    @DisplayName("specificity: class overrides type")
    void specificityClassOverType() {
        StyleSheet sheet = StyleSheet.fromString(
            "* { color: red; }\n.primary { color: green; }");
        Style s = sheet.resolve("Button", "x", Set.of("primary"));
        assertEquals(Color.GREEN, s.foreground());
    }

    @Test
    @DisplayName("selector parse with leading whitespace")
    void selectorLeadingWhitespace() {
        StyleSheet sheet = StyleSheet.fromString("  Button  { color: red; }");
        assertEquals(Color.RED, sheet.resolve("Button", "x", Set.of()).foreground());
    }

    @Test
    @DisplayName("only-comment stylesheet returns empty")
    void onlyComments() {
        StyleSheet sheet = StyleSheet.fromString("/* nothing here */");
        Style s = sheet.resolve("x", "y", Set.of());
        assertEquals(Style.DEFAULT, s);
    }
}
