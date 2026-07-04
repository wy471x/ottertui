package com.ottertui.core;

import java.util.List;

public sealed interface Text {
    void render(Rect area, Buffer buffer);

    record Span(String content, Style style) implements Text {
        public void render(Rect area, Buffer buffer) {
            buffer.setString(area.x(), area.y(), content, style);
        }
    }

    record Line(List<Span> spans) implements Text {
        public void render(Rect area, Buffer buffer) {
            int x = area.x();
            for (Span span : spans) {
                buffer.setString(x, area.y(), span.content(), span.style());
                x += span.content().length();
            }
        }
    }

    record Paragraph(List<Line> lines, Alignment alignment) implements Text {
        public void render(Rect area, Buffer buffer) {
            int y = area.y();
            for (Line line : lines) {
                int totalWidth = line.spans.stream()
                    .mapToInt(s -> s.content.length())
                    .sum();
                int x = switch (alignment) {
                    case LEFT   -> area.x();
                    case CENTER -> area.x() + (area.width() - totalWidth) / 2;
                    case RIGHT  -> area.x() + area.width() - totalWidth;
                };
                line.render(new Rect(x, y, area.width(), 1), buffer);
                y++;
                if (y >= area.y() + area.height()) break;
            }
        }
    }
}
