package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;

public class Chart implements Widget {
    private final List<Dataset> datasets;
    private final double xMin, xMax, yMin, yMax;
    private final Style axisStyle;

    private static final int MARGIN_LEFT = 1;
    private static final int MARGIN_BOTTOM = 1;

    public record Point(double x, double y) {}
    public record Dataset(String label, List<Point> points, Style style) {}

    public Chart(List<Dataset> datasets) {
        this(datasets, Style.DEFAULT);
    }

    public Chart(List<Dataset> datasets, Style axisStyle) {
        this.datasets = List.copyOf(datasets);
        var bounds = computeBounds(datasets);
        this.xMin = bounds[0];
        this.xMax = bounds[1];
        this.yMin = bounds[2];
        this.yMax = bounds[3];
        this.axisStyle = axisStyle;
    }

    public Chart(List<Dataset> datasets,
                 double xMin, double xMax, double yMin, double yMax) {
        this(datasets, xMin, xMax, yMin, yMax, Style.DEFAULT);
    }

    public Chart(List<Dataset> datasets,
                 double xMin, double xMax, double yMin, double yMax,
                 Style axisStyle) {
        this.datasets = List.copyOf(datasets);
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.axisStyle = axisStyle;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (datasets.isEmpty() || area.width() < 2 || area.height() < 2) return;

        // Compute plot area
        int plotX = area.x() + MARGIN_LEFT;
        int plotY = area.y();
        int plotW = Math.max(1, area.width() - MARGIN_LEFT);
        int plotH = Math.max(1, area.height() - MARGIN_BOTTOM);

        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (xRange == 0) xRange = 1;
        if (yRange == 0) yRange = 1;

        int dotWidth = BrailleUtils.cellToDotX(plotW);
        int dotHeight = BrailleUtils.cellToDotY(plotH);

        // Per-dataset dot grid
        List<boolean[][]> dotGrids = new ArrayList<>();
        for (var ds : datasets) {
            boolean[][] dots = new boolean[dotHeight][dotWidth];
            var points = ds.points();
            if (points.size() == 1) {
                var p = points.get(0);
                int dx = dotCoord(p.x(), xMin, xRange, dotWidth);
                int dy = dotCoordY(p.y(), yMax, yRange, dotHeight);
                setDot(dots, dx, dy);
            } else if (points.size() > 1) {
                for (int i = 0; i < points.size() - 1; i++) {
                    var p1 = points.get(i);
                    var p2 = points.get(i + 1);
                    int dx1 = dotCoord(p1.x(), xMin, xRange, dotWidth);
                    int dy1 = dotCoordY(p1.y(), yMax, yRange, dotHeight);
                    int dx2 = dotCoord(p2.x(), xMin, xRange, dotWidth);
                    int dy2 = dotCoordY(p2.y(), yMax, yRange, dotHeight);
                    drawLine(dots, dx1, dy1, dx2, dy2);
                }
            }
            dotGrids.add(dots);
        }

        // Render Braille cells
        for (int cy = 0; cy < plotH; cy++) {
            int baseDy = BrailleUtils.cellToDotY(cy);
            for (int cx = 0; cx < plotW; cx++) {
                int baseDx = BrailleUtils.cellToDotX(cx);

                boolean[] braDots = new boolean[8];
                Style cellStyle = Style.DEFAULT;
                boolean anySet = false;

                for (int dsIdx = 0; dsIdx < dotGrids.size(); dsIdx++) {
                    boolean[][] grid = dotGrids.get(dsIdx);
                    for (int dotRow = 0; dotRow < 4; dotRow++) {
                        int dy = baseDy + dotRow;
                        for (int dotCol = 0; dotCol < 2; dotCol++) {
                            int dx = baseDx + dotCol;
                            if (dy < dotHeight && dx < dotWidth && grid[dy][dx]) {
                                int bitIdx = dotRow * 2 + dotCol;
                                braDots[bitIdx] = true;
                                anySet = true;
                                cellStyle = datasets.get(dsIdx).style();
                            }
                        }
                    }
                }

                if (anySet) {
                    char braille = BrailleUtils.toBrailleChar(braDots);
                    buffer.setCell(plotX + cx, plotY + cy,
                        new Cell(braille, cellStyle));
                }
            }
        }
    }

    private static double[] computeBounds(List<Dataset> datasets) {
        double x0 = Double.MAX_VALUE, x1 = Double.MIN_VALUE;
        double y0 = Double.MAX_VALUE, y1 = Double.MIN_VALUE;
        boolean found = false;

        for (var ds : datasets) {
            for (var p : ds.points()) {
                x0 = Math.min(x0, p.x());
                x1 = Math.max(x1, p.x());
                y0 = Math.min(y0, p.y());
                y1 = Math.max(y1, p.y());
                found = true;
            }
        }
        if (!found) return new double[]{0, 1, 0, 1};
        if (x0 == x1) { x0 -= 0.5; x1 += 0.5; }
        if (y0 == y1) { y0 -= 0.5; y1 += 0.5; }
        double xPad = (x1 - x0) * 0.05;
        double yPad = (y1 - y0) * 0.05;
        return new double[]{x0 - xPad, x1 + xPad, y0 - yPad, y1 + yPad};
    }

    private static int dotCoord(double val, double low, double range, int maxDot) {
        int d = (int) ((val - low) / range * (maxDot - 1));
        return Math.clamp(d, 0, maxDot - 1);
    }

    private static int dotCoordY(double val, double high, double range, int maxDot) {
        // Flip y: screen y=0 maps to yMax
        int d = (int) ((high - val) / range * (maxDot - 1));
        return Math.clamp(d, 0, maxDot - 1);
    }

    private static void setDot(boolean[][] dots, int x, int y) {
        if (y >= 0 && y < dots.length && x >= 0 && x < dots[0].length) {
            dots[y][x] = true;
        }
    }

    private static void drawLine(boolean[][] dots, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1, y = y1;
        while (true) {
            setDot(dots, x, y);
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }
}
