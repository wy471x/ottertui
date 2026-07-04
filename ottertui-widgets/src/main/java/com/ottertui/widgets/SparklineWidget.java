package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.List;

public class SparklineWidget implements Widget {
    private final List<Double> data;
    private final int maxDataPoints;
    private final Style style;

    private static final char[] BLOCKS = {'▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

    public SparklineWidget(List<Double> data) {
        this(data, 80, Style.DEFAULT);
    }

    public SparklineWidget(List<Double> data, int maxDataPoints, Style style) {
        this.data = data;
        this.maxDataPoints = maxDataPoints;
        this.style = style;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (data.isEmpty()) return;

        var window = data.subList(
            Math.max(0, data.size() - Math.min(maxDataPoints, area.width())),
            data.size()
        );

        double min = window.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = window.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double range = max - min;

        for (int i = 0; i < window.size() && i < area.width(); i++) {
            double normalized = range == 0 ? 0.5 : (window.get(i) - min) / range;
            int idx = (int) (normalized * (BLOCKS.length - 1));
            idx = Math.clamp(idx, 0, BLOCKS.length - 1);
            buffer.setCell(area.x() + i, area.y(),
                new Cell(BLOCKS[idx], style));
        }
    }
}
