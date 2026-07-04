package com.ottertui.toolkit;

import com.ottertui.core.*;

import java.util.List;

public sealed interface Element {
    record Container(Layout.Direction direction, List<Element> children,
                     int gap, Style style) implements Element {}

    record WidgetElement(Widget widget, Style style, String id) implements Element {}

    record TextElement(String text, Style style) implements Element {}
}
