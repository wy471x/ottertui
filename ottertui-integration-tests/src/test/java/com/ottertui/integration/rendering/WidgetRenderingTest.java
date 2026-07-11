package com.ottertui.integration.rendering;

import com.ottertui.core.*;
import com.ottertui.integration.infrastructure.BufferSnapshot;
import com.ottertui.integration.infrastructure.StubBackend;
import com.ottertui.tui.Component;
import com.ottertui.widgets.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests: Widget → Buffer → StubBackend flush output.
 * Verifies that widgets render correctly through the full pipeline.
 */
class WidgetRenderingTest {

    @Nested
    @DisplayName("Paragraph → buffer → flush")
    class Paragraph {

        @Test
        @DisplayName("left-aligned wrapped text renders correctly")
        void leftAlignedWrapped() {
            var backend = new StubBackend().withSize(40, 6);
            var buf = new Buffer(40, 6);
            var widget = new com.ottertui.widgets.Paragraph("Hello world this is a test",
                Style.DEFAULT, Alignment.LEFT, true);
            widget.render(new Rect(2, 1, 36, 4), buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("Hello world"));
            assertTrue(output.contains("this is a"));
        }

        @Test
        @DisplayName("right-aligned text positions correctly")
        void rightAligned() {
            var backend = new StubBackend().withSize(40, 5);
            var buf = new Buffer(40, 5);
            var widget = new com.ottertui.widgets.Paragraph("Right",
                Style.DEFAULT, Alignment.RIGHT, false);
            widget.render(new Rect(2, 2, 30, 1), buf);
            backend.flush(buf);

            String line = backend.lastFlush().split("\n")[2];
            // "Right" should be positioned at the right side of the 30-wide area
            assertTrue(line.trim().endsWith("Right"));
        }

        @Test
        @DisplayName("centered text appears roughly in the middle")
        void centered() {
            var backend = new StubBackend().withSize(40, 5);
            var buf = new Buffer(40, 5);
            var widget = new com.ottertui.widgets.Paragraph("Center",
                Style.DEFAULT, Alignment.CENTER, false);
            widget.render(new Rect(2, 2, 30, 1), buf);
            backend.flush(buf);

            String line = backend.lastFlush().split("\n")[2];
            // After position 2, the 30-wide area should have "Center" roughly centered
            String area = line.substring(2, Math.min(32, line.length()));
            int centerIdx = area.indexOf("Center");
            assertTrue(centerIdx > 8, "Expected text near center, got: " + centerIdx);
        }
    }

    @Nested
    @DisplayName("Block → buffer → flush")
    class Block_ {

        @Test
        @DisplayName("bordered block contains corner characters")
        void borderedBlockHasCorners() {
            var backend = new StubBackend().withSize(30, 8);
            var buf = new Buffer(30, 8);
            Block.bordered().render(new Rect(1, 1, 20, 6), buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("┌"), "Expected top-left corner character");
            assertTrue(output.contains("┐"), "Expected top-right corner character");
            assertTrue(output.contains("└"), "Expected bottom-left corner character");
            assertTrue(output.contains("┘"), "Expected bottom-right corner character");
        }

        @Test
        @DisplayName("block with title shows title text")
        void blockWithTitle() {
            var backend = new StubBackend().withSize(30, 8);
            var buf = new Buffer(30, 8);
            Block.bordered().title("MyTitle").render(new Rect(1, 1, 20, 6), buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("MyTitle"), "Expected title in output");
        }

        @Test
        @DisplayName("round border style uses rounded corner chars")
        void roundedBlock() {
            var backend = new StubBackend().withSize(30, 8);
            var buf = new Buffer(30, 8);
            Block.bordered(BorderStyle.ROUNDED).render(new Rect(1, 1, 20, 6), buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("╭"), "Expected rounded top-left corner");
        }

        @Test
        @DisplayName("innerRect excludes border area")
        void innerRectExcludesBorder() {
            var area = new Rect(0, 0, 20, 10);
            var inner = Block.bordered().innerRect(area);
            assertEquals(1, inner.x());
            assertEquals(1, inner.y());
            assertEquals(18, inner.width());
            assertEquals(8, inner.height());
        }
    }

    @Nested
    @DisplayName("Gauge → buffer → flush")
    class Gauge {

        @Test
        @DisplayName("50% gauge shows half filled blocks")
        void halfGauge() {
            var backend = new StubBackend().withSize(40, 4);
            var buf = new Buffer(40, 4);
            var gauge = new com.ottertui.widgets.Gauge(0.5,
                new Style(Color.CYAN, Color.RESET, Set.of()),
                new Style(Color.DARK_GRAY, Color.RESET, Set.of()));
            gauge.render(new Rect(2, 1, 20, 1), buf);
            backend.flush(buf);

            String line = backend.lastFlush().split("\n")[1];
            // First half should be filled blocks, second half light shade
            String area = line.substring(2, 22);
            long filled = area.chars().filter(c -> c == '█').count();
            long empty = area.chars().filter(c -> c == '░').count();
            assertEquals(10, filled, "Expected 10 filled cells for 50%");
            assertEquals(10, empty, "Expected 10 empty cells for 50%");
        }
    }

    @Nested
    @DisplayName("List → buffer → flush")
    class List_ {

        @Test
        @DisplayName("selected item is rendered with full-width background")
        void listSelectionRenders() {
            var backend = new StubBackend().withSize(40, 10);
            var buf = new Buffer(40, 10);
            var list = new com.ottertui.widgets.List(java.util.List.of("Alpha", "Beta", "Gamma"));
            list.select(1);
            list.render(new Rect(2, 2, 20, 5), buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("Alpha"));
            assertTrue(output.contains("Beta"));
            assertTrue(output.contains("Gamma"));
        }
    }

    @Nested
    @DisplayName("Multiple widgets layered")
    class Layered {

        @Test
        @DisplayName("block then paragraph inside inner region")
        void blockWithParagraphInside() {
            var backend = new StubBackend().withSize(40, 10);
            var buf = new Buffer(40, 10);
            var area = new Rect(1, 1, 30, 8);
            var block = Block.bordered().title("Panel");
            block.render(area, buf);
            var inner = block.innerRect(area);
            new com.ottertui.widgets.Paragraph("Content inside block",
                new Style(Color.GREEN, Color.RESET, Set.of()),
                Alignment.LEFT, true)
                .render(inner, buf);
            backend.flush(buf);

            String output = backend.lastFlush();
            assertTrue(output.contains("Panel"), "Title missing");
            assertTrue(output.contains("Content inside block"), "Content missing");
        }
    }
}
