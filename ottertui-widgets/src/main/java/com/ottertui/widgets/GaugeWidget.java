package com.ottertui.widgets;

import com.ottertui.core.*;
import java.util.Set;

public class GaugeWidget implements Widget {
    private final double ratio;
    private final Style gaugeStyle;
    private final Style backgroundStyle;

    public GaugeWidget(double ratio) {
        this(ratio, new Style(Color.CYAN, Color.RESET, Set.of()), Style.DEFAULT);
    }

    public GaugeWidget(double ratio, Style gaugeStyle, Style backgroundStyle) {
        this.ratio = Math.clamp(ratio, 0.0, 1.0);
        this.gaugeStyle = gaugeStyle;
        this.backgroundStyle = backgroundStyle;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        int filled = (int) (ratio * area.width());

        for (int x = 0; x < area.width(); x++) {
            char ch = ' ';
            Style style = backgroundStyle;
            if (x < filled) {
                style = gaugeStyle;
                if (x == filled - 1 && filled > 0) {
                    ch = '█';
                } else {
                    ch = '█';
                }
            } else {
                ch = '░';
            }
            buffer.setCell(area.x() + x, area.y(), new Cell(ch, style));
        }
    }
}
