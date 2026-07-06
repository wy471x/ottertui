package com.ottertui.core;

import java.util.ArrayList;
import java.util.List;

public record Layout(Direction direction, List<Constraint> constraints, int gap) {

    public enum Direction { HORIZONTAL, VERTICAL }

    public static Layout horizontal(List<Constraint> constraints) {
        return new Layout(Direction.HORIZONTAL, constraints, 1);
    }

    public static Layout vertical(List<Constraint> constraints) {
        return new Layout(Direction.VERTICAL, constraints, 1);
    }

    public Layout gap(int gap) {
        return new Layout(direction, constraints, gap);
    }

    public List<Rect> split(Rect area) {
        int total = direction == Direction.HORIZONTAL ? area.width() : area.height();
        total -= gap * (constraints.size() - 1);

        List<Integer> sizes = resolveSizes(constraints, total);

        List<Rect> result = new ArrayList<>();
        int offset = direction == Direction.HORIZONTAL ? area.x() : area.y();
        for (int size : sizes) {
            Rect rect = direction == Direction.HORIZONTAL
                ? new Rect(offset, area.y(), size, area.height())
                : new Rect(area.x(), offset, area.width(), size);
            result.add(rect);
            offset += size + gap;
        }
        return result;
    }

    private List<Integer> resolveSizes(List<Constraint> constraints, int total) {
        List<Integer> sizes = new ArrayList<>();
        int remaining = total;
        int proportionalTotal = 0;

        for (var c : constraints) {
            switch (c) {
                case Constraint.Fixed f -> {
                    int size = Math.min(f.size(), remaining);
                    sizes.add(size);
                    remaining -= size;
                }
                case Constraint.Min m -> {
                    sizes.add(m.min());
                    remaining -= m.min();
                }
                case Constraint.Percentage p -> {
                    int size = total * p.percent() / 100;
                    sizes.add(size);
                    remaining -= size;
                }
                case Constraint.Proportional p -> {
                    proportionalTotal += p.weight();
                    sizes.add(-p.weight()); // placeholder
                    // remaining unchanged until we resolve
                }
            }
        }

        // Resolve proportional constraints
        if (proportionalTotal > 0 && remaining > 0) {
            int propRemaining = remaining;
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i) < 0) {
                    int weight = -sizes.get(i);
                    int size = propRemaining * weight / proportionalTotal;
                    sizes.set(i, size);
                    remaining -= size;
                }
            }
        } else {
            // Zero out unresolved proportional placeholders
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i) < 0) {
                    sizes.set(i, 0);
                }
            }
        }

        // Distribute any leftover space (from integer division) to last non-zero size
        if (remaining > 0) {
            for (int i = sizes.size() - 1; i >= 0; i--) {
                if (sizes.get(i) > 0) {
                    sizes.set(i, sizes.get(i) + remaining);
                    break;
                }
            }
        }

        return sizes;
    }
}
