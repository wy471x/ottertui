package com.ottertui.toolkit;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

class ToolkitTest {

    static boolean isTtyAvailable() {
        return System.console() != null;
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("build runs config with ElementBuilder")
    void buildRunsConfig() {
        TuiApp app = Toolkit.build(builder -> {
            builder.text("Hello");
        });
        assertNotNull(app);
    }

    @Test
    @DisplayName("text adds TextElement")
    void textElement() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.text("Hello");
        Element e = builder.build();
        assertInstanceOf(Element.Container.class, e);
        Element.Container c = (Element.Container) e;
        assertEquals(1, c.children().size());
        assertInstanceOf(Element.TextElement.class, c.children().get(0));
    }

    @Test
    @DisplayName("text with style")
    void textWithStyle() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        Style style = new Style(Color.RED, Color.RESET, java.util.Set.of());
        builder.text("Hello", style);
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        Element.TextElement te = (Element.TextElement) c.children().get(0);
        assertEquals("Hello", te.text());
        assertEquals(Color.RED, te.style().foreground());
    }

    @Test
    @DisplayName("widget adds WidgetElement")
    void widgetElement() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.widget((area, buffer) -> {});
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        assertInstanceOf(Element.WidgetElement.class, c.children().get(0));
    }

    @Test
    @DisplayName("widget with style")
    void widgetWithStyle() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        Style style = new Style(Color.BLUE, Color.RESET, java.util.Set.of());
        builder.widget((area, buffer) -> {}, style);
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        Element.WidgetElement we = (Element.WidgetElement) c.children().get(0);
        assertEquals(Color.BLUE, we.style().foreground());
    }

    @Test
    @DisplayName("vertical adds children")
    void verticalAddsChildren() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.vertical(child -> child.text("A"), child -> child.text("B"));
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        assertEquals(2, c.children().size());
    }

    @Test
    @DisplayName("horizontal adds children")
    void horizontalAddsChildren() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.horizontal(child -> child.text("A"), child -> child.text("B"));
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        assertEquals(2, c.children().size());
    }

    @Test
    @DisplayName("build returns single container child directly")
    void buildSingleContainer() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.vertical(child -> child.text("A"));
        Element e = builder.build();
        assertInstanceOf(Element.Container.class, e);
    }

    @Test
    @DisplayName("build wraps multiple children in container")
    void buildWrapsMultiple() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.text("A");
        builder.text("B");
        Element e = builder.build();
        Element.Container c = (Element.Container) e;
        assertEquals(Layout.Direction.VERTICAL, c.direction());
        assertEquals(1, c.gap());
    }

    @Test
    @DisplayName("build returns container when nested")
    void buildReturnsNestedContainer() {
        Toolkit.ElementBuilder builder = new Toolkit.ElementBuilder();
        builder.vertical(child -> {
            child.text("A");
            child.text("B");
        });
        Element e = builder.build();
        assertInstanceOf(Element.Container.class, e);
        Element.Container c = (Element.Container) e;
        assertEquals(2, c.children().size());
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("nesting horizontal inside vertical")
    void nestingHorizontalInVertical() {
        TuiApp app = Toolkit.build(root -> {
            root.vertical(column -> {
                column.text("Header");
                column.horizontal(row -> {
                    row.text("A");
                    row.text("B");
                });
            });
        });
        assertNotNull(app);
    }
}
