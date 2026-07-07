package com.ottertui.toolkit;

import java.util.List;
import java.util.Set;

public sealed interface Selector {
    record Universal() implements Selector {
        public boolean matches(String type, String id, Set<String> classes,
                               Set<String> pseudoClasses) {
            return true;
        }
    }

    record Type(String widgetType) implements Selector {
        public boolean matches(String type, String id, Set<String> classes,
                               Set<String> pseudoClasses) {
            return widgetType.equals(type);
        }
    }

    record Id(String id) implements Selector {
        public boolean matches(String type, String wid, Set<String> classes,
                               Set<String> pseudoClasses) {
            return id.equals(wid);
        }
    }

    record ClassSelector(String className) implements Selector {
        public boolean matches(String type, String id, Set<String> classes,
                               Set<String> pseudoClasses) {
            return classes.contains(className);
        }
    }

    record PseudoClass(String name) implements Selector {
        public boolean matches(String type, String id, Set<String> classes,
                               Set<String> pseudoClasses) {
            return pseudoClasses.contains(name);
        }
    }

    record Compound(List<Selector> selectors) implements Selector {
        public boolean matches(String type, String id, Set<String> classes,
                               Set<String> pseudoClasses) {
            return selectors.stream()
                .allMatch(s -> s.matches(type, id, classes, pseudoClasses));
        }
    }

    boolean matches(String widgetType, String id, Set<String> classes,
                    Set<String> pseudoClasses);

    default boolean matches(String widgetType, String id, Set<String> classes) {
        return matches(widgetType, id, classes, Set.of());
    }

    static Selector universal() { return new Universal(); }
    static Selector type(String t) { return new Type(t); }
    static Selector id(String i) { return new Id(i); }
    static Selector clazz(String c) { return new ClassSelector(c); }
    static Selector pseudo(String p) { return new PseudoClass(p); }
}
