package com.ottertui.widgets;

import com.ottertui.core.*;

public class Clear implements Widget {
    @Override
    public void render(Rect area, Buffer buffer) {
        for (int y = area.y(); y < area.y() + area.height(); y++) {
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, y, Cell.EMPTY);
            }
        }
    }
}
