package com.ottertui.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Buffer {
    private final Cell[][] cells;
    private final int width;
    private final int height;
    private final List<RawWrite> rawWrites;

    public record RawWrite(int x, int y, String text) {}

    public Buffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
        this.rawWrites = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = Cell.EMPTY;
            }
        }
    }

    public int width() { return width; }
    public int height() { return height; }

    public List<RawWrite> rawContent() {
        return Collections.unmodifiableList(rawWrites);
    }

    public void addRawWrite(int x, int y, String text) {
        rawWrites.add(new RawWrite(x, y, text));
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[y][x];
        }
        return Cell.EMPTY;
    }

    public void setCell(int x, int y, Cell cell) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y][x] = cell;
        }
    }

    public void setString(int x, int y, String text, Style style) {
        int displayX = x;
        int[] codePoints = text.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {
            int cp = codePoints[i];
            int charWidth = CharWidth.codePointWidth(cp);

            if (charWidth == 0) continue;

            char[] chars = Character.toChars(cp);
            char displayChar = chars.length == 1 ? chars[0] : '?';

            if (displayX >= 0 && displayX < width) {
                cells[y][displayX] = new Cell(displayChar, style);
            }

            if (charWidth == 2) {
                displayX++;
            }

            displayX++;
            if (displayX >= width) break;
        }
    }

    /**
     * Write a string including control characters (ESC, etc.) without
     * width filtering. Used for terminal escape sequences.
     */
    public void writeRaw(int x, int y, String text, Style style) {
        int displayX = x;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isHighSurrogate(ch) && i + 1 < text.length()) {
                // Supplementary character — store as '?'
                if (displayX >= 0 && displayX < width) {
                    cells[y][displayX] = new Cell('?', style);
                }
                i++; // skip low surrogate
                displayX++;
            } else {
                if (displayX >= 0 && displayX < width) {
                    cells[y][displayX] = new Cell(ch, style);
                }
                displayX++;
            }
            if (displayX >= width) break;
        }
    }

    public Buffer region(Rect area) {
        return new BufferView(this, area);
    }

    public void fill(Rect area, Cell cell) {
        for (int y = area.y(); y < area.y() + area.height() && y < height; y++) {
            for (int x = area.x(); x < area.x() + area.width() && x < width; x++) {
                if (x >= 0 && y >= 0) {
                    cells[y][x] = cell;
                }
            }
        }
    }

    private static class BufferView extends Buffer {
        private final Buffer parent;
        private final int offsetX;
        private final int offsetY;

        BufferView(Buffer parent, Rect area) {
            super(area.width(), area.height());
            this.parent = parent;
            this.offsetX = area.x();
            this.offsetY = area.y();
        }

        @Override
        public Cell getCell(int x, int y) {
            return parent.getCell(offsetX + x, offsetY + y);
        }

        @Override
        public void setCell(int x, int y, Cell cell) {
            parent.setCell(offsetX + x, offsetY + y, cell);
        }

        @Override
        public List<RawWrite> rawContent() {
            return parent.rawContent();
        }

        @Override
        public void addRawWrite(int x, int y, String text) {
            parent.addRawWrite(offsetX + x, offsetY + y, text);
        }
    }
}
