package com.ottertui.widgets;

import com.ottertui.core.*;

public class Fill implements Widget {
    private final char ch;
    private final Style style;

    public Fill(char ch) {
        this(ch, Style.DEFAULT);
    }

    public Fill(char ch, Style style) {
        this.ch = ch;
        this.style = style;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        for (int y = area.y(); y < area.y() + area.height(); y++) {
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, y, new Cell(ch, style));
            }
        }
    }
}
