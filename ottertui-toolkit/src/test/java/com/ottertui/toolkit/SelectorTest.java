package com.ottertui.toolkit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SelectorTest {

    @Test
    @DisplayName("Universal matches everything")
    void universalMatches() {
        Selector s = Selector.universal();
        assertTrue(s.matches("any", "any", Set.of()));
        assertTrue(s.matches("foo", "bar", Set.of("baz")));
    }

    @Test
    @DisplayName("Type matches correct widget type")
    void typeMatches() {
        Selector s = Selector.type("Button");
        assertTrue(s.matches("Button", "x", Set.of()));
        assertFalse(s.matches("Label", "x", Set.of()));
    }

    @Test
    @DisplayName("Id matches correct id")
    void idMatches() {
        Selector s = Selector.id("submit-btn");
        assertTrue(s.matches("x", "submit-btn", Set.of()));
        assertFalse(s.matches("x", "cancel-btn", Set.of()));
    }

    @Test
    @DisplayName("ClassSelector matches classes")
    void classSelectorMatches() {
        Selector s = Selector.clazz("primary");
        assertTrue(s.matches("x", "y", Set.of("primary", "secondary")));
        assertFalse(s.matches("x", "y", Set.of("secondary")));
        assertFalse(s.matches("x", "y", Set.of()));
    }

    @Test
    @DisplayName("Compound selector matches all")
    void compoundMatchesAll() {
        Selector s = new Selector.Compound(List.of(
            Selector.type("Button"),
            Selector.clazz("primary")
        ));
        assertTrue(s.matches("Button", "x", Set.of("primary")));
        assertFalse(s.matches("Label", "x", Set.of("primary")));
        assertFalse(s.matches("Button", "x", Set.of()));
    }

    @Test
    @DisplayName("static factory methods create correct types")
    void factoryMethods() {
        assertInstanceOf(Selector.Universal.class, Selector.universal());
        assertInstanceOf(Selector.Type.class, Selector.type("test"));
        assertInstanceOf(Selector.Id.class, Selector.id("test"));
        assertInstanceOf(Selector.ClassSelector.class, Selector.clazz("test"));
        assertInstanceOf(Selector.PseudoClass.class, Selector.pseudo("hover"));
    }

    @Test
    @DisplayName("PseudoClass selector matches pseudo classes")
    void pseudoClassMatches() {
        Selector s = Selector.pseudo("hover");
        assertTrue(s.matches("x", "y", Set.of(), Set.of("hover", "focus")));
        assertFalse(s.matches("x", "y", Set.of(), Set.of("focus")));
        assertFalse(s.matches("x", "y", Set.of(), Set.of()));
    }
}
