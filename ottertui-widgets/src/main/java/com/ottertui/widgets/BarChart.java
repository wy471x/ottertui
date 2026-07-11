package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.List;

public class BarChart implements Widget {
    private final List<Bar> bars;
    private final int barWidth;
    private final int barGap;

    public record Bar(String label, double value, Style style) {}

    public BarChart(List<Bar> bars) {
        this(bars, 3, 1);
    }

    public BarChart(List<Bar> bars, int barWidth, int barGap) {
        this.bars = bars;
        this.barWidth = barWidth;
        this.barGap = barGap;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (bars.isEmpty()) return;

        double max = bars.stream().mapToDouble(Bar::value).max().orElse(1);
        int chartHeight = Math.max(1, area.height() - 1);

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            int barH = (int) ((bar.value() / max) * chartHeight);

            int x = area.x() + i * (barWidth + barGap);
            int yBase = area.y() + chartHeight;

            // Draw bar
            for (int row = 0; row < barH && (yBase - row - 1) >= area.y(); row++) {
                for (int col = 0; col < barWidth && (x + col) < area.x() + area.width(); col++) {
                    buffer.setCell(x + col, yBase - row - 1,
                        new Cell('█', bar.style()));
                }
            }

            // Label
            String label = bar.label();
            if (label.length() > barWidth) label = label.substring(0, barWidth);
            buffer.setString(x, yBase, label, Style.DEFAULT);
        }
    }
}
