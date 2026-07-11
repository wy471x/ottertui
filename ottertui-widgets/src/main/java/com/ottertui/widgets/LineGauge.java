package com.ottertui.widgets;

import com.ottertui.core.*;

public class LineGauge implements Widget {
    private static final char FILLED_CHAR = '\u2501';   // ━ heavy horizontal
    private static final char UNFILLED_CHAR = '\u2500'; // ─ light horizontal

    private final double ratio;
    private final Style filledStyle;
    private final Style unfilledStyle;

    public LineGauge(double ratio) {
        this(ratio, Style.DEFAULT, Style.DEFAULT);
    }

    public LineGauge(double ratio, Style filledStyle, Style unfilledStyle) {
        if (ratio < 0.0 || ratio > 1.0) {
            System.getLogger("com.ottertui.widgets.LineGauge")
                .log(System.Logger.Level.WARNING,
                    "Ratio {0} out of [0.0, 1.0], clamped", ratio);
        }
        this.ratio = Math.clamp(ratio, 0.0, 1.0);
        this.filledStyle = filledStyle;
        this.unfilledStyle = unfilledStyle;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        int filled = (int) (ratio * area.width());

        for (int x = 0; x < area.width(); x++) {
            char ch = x < filled ? FILLED_CHAR : UNFILLED_CHAR;
            Style style = x < filled ? filledStyle : unfilledStyle;
            buffer.setCell(area.x() + x, area.y(), new Cell(ch, style));
        }
    }
}
