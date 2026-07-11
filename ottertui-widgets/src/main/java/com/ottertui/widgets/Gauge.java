package com.ottertui.widgets;

import com.ottertui.core.*;
import java.util.Set;

public class Gauge implements Widget {
    private final double ratio;
    private final Style gaugeStyle;
    private final Style backgroundStyle;

    public Gauge(double ratio) {
        this(ratio, new Style(Color.CYAN, Color.RESET, Set.of()), Style.DEFAULT);
    }

    public Gauge(double ratio, Style gaugeStyle, Style backgroundStyle) {
        if (ratio < 0.0 || ratio > 1.0) {
            System.getLogger("com.ottertui.widgets.Gauge")
                .log(System.Logger.Level.WARNING,
                    "Ratio {0} out of [0.0, 1.0], clamped", ratio);
        }
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
                ch = '█';
            } else {
                ch = '░';
            }
            buffer.setCell(area.x() + x, area.y(), new Cell(ch, style));
        }
    }
}
