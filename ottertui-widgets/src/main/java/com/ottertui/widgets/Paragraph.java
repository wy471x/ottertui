package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;

public class Paragraph implements Widget {
    private final String text;
    private final Style style;
    private final Alignment alignment;
    private final boolean wrap;

    public Paragraph(String text) {
        this(text, Style.DEFAULT, Alignment.LEFT, true);
    }

    public Paragraph(String text, Style style, Alignment alignment, boolean wrap) {
        this.text = text;
        this.style = style;
        this.alignment = alignment;
        this.wrap = wrap;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (wrap) {
            List<String> lines = wrapText(text, area.width());
            for (int i = 0; i < lines.size() && i < area.height(); i++) {
                String line = lines.get(i);
                int x = switch (alignment) {
                    case LEFT   -> area.x();
                    case CENTER -> area.x() + (area.width() - line.length()) / 2;
                    case RIGHT  -> area.x() + area.width() - line.length();
                };
                buffer.setString(x, area.y() + i, line, style);
            }
        } else {
            String display = text.length() > area.width()
                ? text.substring(0, area.width()) : text;
            int x = switch (alignment) {
                case LEFT   -> area.x();
                case CENTER -> area.x() + (area.width() - display.length()) / 2;
                case RIGHT  -> area.x() + area.width() - display.length();
            };
            buffer.setString(x, area.y(), display, style);
        }
    }

    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (width <= 0) return lines;
        for (String paragraph : text.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            int start = 0;
            while (start < paragraph.length()) {
                int end = Math.min(start + width, paragraph.length());
                if (end < paragraph.length() && paragraph.charAt(end) != ' ') {
                    int space = paragraph.lastIndexOf(' ', end);
                    if (space > start) end = space;
                }
                lines.add(paragraph.substring(start, end).trim());
                start = end;
                while (start < paragraph.length() && paragraph.charAt(start) == ' ') {
                    start++;
                }
            }
        }
        return lines;
    }
}
