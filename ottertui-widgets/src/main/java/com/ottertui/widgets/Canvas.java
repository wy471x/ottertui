package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.function.Consumer;

public class Canvas implements Widget {
    private final int width;
    private final int height;
    private final Painter painter;

    public Canvas(int width, int height, Consumer<Painter> painter) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.painter = new Painter(this.width, this.height);
        painter.accept(this.painter);
    }

    public static class Painter {
        private final int width;
        private final int height;
        private final Color[][] dots;

        Painter(int width, int height) {
            this.width = width;
            this.height = height;
            this.dots = new Color[height][width];
        }

        public Painter set(int x, int y, Color color) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                dots[y][x] = color;
            }
            return this;
        }

        public Painter line(int x1, int y1, int x2, int y2, Color color) {
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int sx = x1 < x2 ? 1 : -1;
            int sy = y1 < y2 ? 1 : -1;
            int err = dx - dy;

            int x = x1, y = y1;
            while (true) {
                set(x, y, color);
                if (x == x2 && y == y2) break;
                int e2 = 2 * err;
                if (e2 > -dy) { err -= dy; x += sx; }
                if (e2 < dx) { err += dx; y += sy; }
            }
            return this;
        }

        public Painter rect(int x, int y, int w, int h, Color color) {
            line(x, y, x + w - 1, y, color);
            line(x, y + h - 1, x + w - 1, y + h - 1, color);
            line(x, y, x, y + h - 1, color);
            line(x + w - 1, y, x + w - 1, y + h - 1, color);
            return this;
        }

        public Painter circle(int cx, int cy, int r, Color color) {
            int x = r, y = 0;
            int p = 1 - r;
            while (x >= y) {
                plotCirclePoints(cx, cy, x, y, color);
                y++;
                if (p <= 0) {
                    p += 2 * y + 1;
                } else {
                    x--;
                    p += 2 * (y - x) + 1;
                }
            }
            return this;
        }

        private void plotCirclePoints(int cx, int cy, int x, int y, Color c) {
            set(cx + x, cy + y, c);
            set(cx - x, cy + y, c);
            set(cx + x, cy - y, c);
            set(cx - x, cy - y, c);
            set(cx + y, cy + x, c);
            set(cx - y, cy + x, c);
            set(cx + y, cy - x, c);
            set(cx - y, cy - x, c);
        }

        Color getDot(int x, int y) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                return dots[y][x];
            }
            return null;
        }
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        int cellsWide = (width + 1) / 2;
        int cellsHigh = (height + 3) / 4;

        for (int cy = 0; cy < cellsHigh && cy < area.height(); cy++) {
            int baseDy = BrailleUtils.cellToDotY(cy);
            for (int cx = 0; cx < cellsWide && cx < area.width(); cx++) {
                int baseDx = BrailleUtils.cellToDotX(cx);

                boolean[] braDots = new boolean[8];
                Color cellColor = null;

                for (int dotRow = 0; dotRow < 4; dotRow++) {
                    int dy = baseDy + dotRow;
                    for (int dotCol = 0; dotCol < 2; dotCol++) {
                        int dx = baseDx + dotCol;
                        Color dot = painter.getDot(dx, dy);
                        if (dot != null) {
                            int bitIdx = dotRow * 2 + dotCol;
                            braDots[bitIdx] = true;
                            if (cellColor == null) {
                                cellColor = dot;
                            }
                        }
                    }
                }

                if (cellColor != null) {
                    char braille = BrailleUtils.toBrailleChar(braDots);
                    buffer.setCell(area.x() + cx, area.y() + cy,
                        new Cell(braille, new Style(cellColor, Color.RESET,
                            java.util.Set.of())));
                }
            }
        }
    }
}
