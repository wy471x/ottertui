package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.Optional;

public class Block implements Widget {
    private final Optional<String> title;
    private final BorderStyle borderStyle;
    private final Style borderStyleCustom;
    private final Style titleStyle;

    private Block(Optional<String> title, BorderStyle borderStyle,
                  Style borderStyleCustom, Style titleStyle) {
        this.title = title;
        this.borderStyle = borderStyle;
        this.borderStyleCustom = borderStyleCustom;
        this.titleStyle = titleStyle;
    }

    public static Block bordered() {
        return new Block(Optional.empty(), BorderStyle.PLAIN,
            Style.DEFAULT, Style.DEFAULT);
    }

    public static Block bordered(BorderStyle style) {
        return new Block(Optional.empty(), style, Style.DEFAULT, Style.DEFAULT);
    }

    public Block title(String title) {
        return new Block(Optional.of(title), borderStyle, borderStyleCustom, titleStyle);
    }

    public Block borderStyle(Style style) {
        return new Block(title, borderStyle, style, titleStyle);
    }

    public Block titleStyle(Style style) {
        return new Block(title, borderStyle, borderStyleCustom, style);
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (area.width() < 2 || area.height() < 2) return;

        Style bs = borderStyleCustom == Style.DEFAULT ? Style.DEFAULT : borderStyleCustom;
        Style ts = titleStyle == Style.DEFAULT ? bs : titleStyle;

        // Top border
        for (int x = area.x(); x < area.x() + area.width(); x++) {
            String ch;
            if (x == area.x()) {
                ch = borderStyle.topLeft;
            } else if (x == area.x() + area.width() - 1) {
                ch = borderStyle.topRight;
            } else {
                ch = borderStyle.horizontal;
            }
            buffer.setCell(x, area.y(), new Cell(ch.charAt(0), bs));
        }

        // Bottom border
        int bottomY = area.y() + area.height() - 1;
        for (int x = area.x(); x < area.x() + area.width(); x++) {
            String ch;
            if (x == area.x()) {
                ch = borderStyle.bottomLeft;
            } else if (x == area.x() + area.width() - 1) {
                ch = borderStyle.bottomRight;
            } else {
                ch = borderStyle.horizontal;
            }
            buffer.setCell(x, bottomY, new Cell(ch.charAt(0), bs));
        }

        // Side borders
        for (int y = area.y() + 1; y < area.y() + area.height() - 1; y++) {
            buffer.setCell(area.x(), y,
                new Cell(borderStyle.vertical.charAt(0), bs));
            buffer.setCell(area.x() + area.width() - 1, y,
                new Cell(borderStyle.vertical.charAt(0), bs));
        }

        // Title
        title.ifPresent(t -> {
            int maxWidth = Math.max(0, area.width() - 4);
            if (maxWidth <= 0) return;
            String display = t.length() > maxWidth ? t.substring(0, maxWidth) : t;
            int startX = area.x() + 2;
            buffer.setString(startX, area.y(), display, ts);
        });
    }

    public Rect innerRect(Rect area) {
        return area.inner(1);
    }
}
