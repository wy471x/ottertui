package com.ottertui.widgets;

import com.ottertui.core.*;

public class Scrollbar implements Widget {
    public enum Orientation { VERTICAL, HORIZONTAL }

    private final Orientation orientation;
    private final Style trackStyle;
    private final Style thumbStyle;

    private int position;
    private int contentLength;
    private int viewportLength;

    public Scrollbar(Orientation orientation) {
        this(orientation, Style.DEFAULT, Style.DEFAULT);
    }

    public Scrollbar(Orientation orientation, Style trackStyle, Style thumbStyle) {
        this.orientation = orientation;
        this.trackStyle = trackStyle;
        this.thumbStyle = thumbStyle;
        this.position = 0;
        this.contentLength = 1;
        this.viewportLength = 1;
    }

    public void setPosition(int pos) {
        this.position = Math.max(0, pos);
    }

    public int position() {
        return position;
    }

    public void setContentLength(int len) {
        this.contentLength = Math.max(1, len);
    }

    public int contentLength() {
        return contentLength;
    }

    public void setViewportLength(int len) {
        this.viewportLength = Math.max(1, len);
    }

    public int viewportLength() {
        return viewportLength;
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        int barLength = orientation == Orientation.VERTICAL ? area.height() : area.width();
        if (barLength <= 0) return;

        int thumbSize = (viewportLength * barLength) / contentLength;
        if (thumbSize < 1) thumbSize = 1;
        if (thumbSize > barLength) thumbSize = barLength;

        int thumbPos;
        if (contentLength <= viewportLength) {
            thumbPos = 0;
        } else {
            thumbPos = (position * (barLength - thumbSize)) / (contentLength - viewportLength);
        }
        if (thumbPos < 0) thumbPos = 0;
        if (thumbPos + thumbSize > barLength) thumbPos = barLength - thumbSize;

        char trackChar = orientation == Orientation.VERTICAL ? '│' : '─';
        char thumbChar = '█';

        for (int i = 0; i < barLength; i++) {
            boolean isThumb = i >= thumbPos && i < thumbPos + thumbSize;
            char ch = isThumb ? thumbChar : trackChar;
            Style style = isThumb ? thumbStyle : trackStyle;

            if (orientation == Orientation.VERTICAL) {
                buffer.setCell(area.x(), area.y() + i, new Cell(ch, style));
            } else {
                buffer.setCell(area.x() + i, area.y(), new Cell(ch, style));
            }
        }
    }
}
