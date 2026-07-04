package com.ottertui.toolkit;

import java.util.List;
import java.util.Set;

public sealed interface Selector {
    record Universal() implements Selector {
        public boolean matches(String type, String id, Set<String> classes) {
            return true;
        }
    }

    record Type(String widgetType) implements Selector {
        public boolean matches(String type, String id, Set<String> classes) {
            return widgetType.equals(type);
        }
    }

    record Id(String id) implements Selector {
        public boolean matches(String type, String wid, Set<String> classes) {
            return id.equals(wid);
        }
    }

    record ClassSelector(String className) implements Selector {
        public boolean matches(String type, String id, Set<String> classes) {
            return classes.contains(className);
        }
    }

    record Compound(List<Selector> selectors) implements Selector {
        public boolean matches(String type, String id, Set<String> classes) {
            return selectors.stream().allMatch(s -> s.matches(type, id, classes));
        }
    }

    boolean matches(String widgetType, String id, Set<String> classes);

    static Selector universal() { return new Universal(); }
    static Selector type(String t) { return new Type(t); }
    static Selector id(String i) { return new Id(i); }
    static Selector clazz(String c) { return new ClassSelector(c); }
}
