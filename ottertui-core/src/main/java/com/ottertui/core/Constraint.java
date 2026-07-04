package com.ottertui.core;

import java.util.ArrayList;
import java.util.List;

public sealed interface Constraint {
    record Percentage(int percent) implements Constraint {}
    record Fixed(int size) implements Constraint {}
    record Min(int min) implements Constraint {}
    record Proportional(int weight) implements Constraint {}

    static Constraint percentage(int p) { return new Percentage(p); }
    static Constraint fixed(int s) { return new Fixed(s); }
    static Constraint min(int m) { return new Min(m); }
    static Constraint proportional(int w) { return new Proportional(w); }
}
