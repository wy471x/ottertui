package com.ottertui.toolkit;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Toolkit {

    private Toolkit() {}

    public static TuiApp build(Consumer<ElementBuilder> config) {
        var builder = new ElementBuilder();
        config.accept(builder);
        return new TuiApp(builder.build());
    }

    public static class ElementBuilder {
        private final List<Element> children = new ArrayList<>();

        public ElementBuilder vertical(Consumer<ElementBuilder>... blocks) {
            for (var block : blocks) {
                var child = new ElementBuilder();
                block.accept(child);
                children.addAll(child.children);
            }
            return this;
        }

        public ElementBuilder horizontal(Consumer<ElementBuilder>... blocks) {
            for (var block : blocks) {
                var child = new ElementBuilder();
                block.accept(child);
                children.addAll(child.children);
            }
            return this;
        }

        public ElementBuilder text(String text) {
            children.add(new Element.TextElement(text, Style.DEFAULT));
            return this;
        }

        public ElementBuilder text(String text, Style style) {
            children.add(new Element.TextElement(text, style));
            return this;
        }

        public ElementBuilder widget(Widget widget) {
            children.add(new Element.WidgetElement(widget, Style.DEFAULT, ""));
            return this;
        }

        public ElementBuilder widget(Widget widget, Style style) {
            children.add(new Element.WidgetElement(widget, style, ""));
            return this;
        }

        Element build() {
            if (children.size() == 1 && children.get(0) instanceof Element.Container) {
                return children.get(0);
            }
            return new Element.Container(Layout.Direction.VERTICAL, List.copyOf(children), 1, Style.DEFAULT);
        }
    }
}
