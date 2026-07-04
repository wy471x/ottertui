package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TableWidget<T> implements StatefulWidget<TableState> {
    private final List<Column<T>> columns;
    private final List<T> rows;
    private final Style headerStyle;
    private final Style selectedStyle;

    public record Column<T>(String header, Function<T, String> extractor, int width) {}

    public TableWidget(List<Column<T>> columns, List<T> rows) {
        this(columns, rows,
            new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)),
            new Style(Color.BLACK, Color.WHITE, Set.of()));
    }

    public TableWidget(List<Column<T>> columns, List<T> rows,
                       Style headerStyle, Style selectedStyle) {
        this.columns = columns;
        this.rows = rows;
        this.headerStyle = headerStyle;
        this.selectedStyle = selectedStyle;
    }

    @Override
    public void render(TableState state, Rect area, Buffer buffer) {
        int y = area.y();

        // Header
        int x = area.x();
        for (int c = 0; c < columns.size(); c++) {
            Column<T> col = columns.get(c);
            int w = Math.min(col.width(), area.width() - (x - area.x()));
            String header = col.header().length() > w
                ? col.header().substring(0, w) : col.header();
            buffer.setString(x, y, header, headerStyle);
            x += w + 1;
            if (x >= area.x() + area.width()) break;
        }
        y++;

        // Separator line
        if (y < area.y() + area.height()) {
            for (int sx = area.x(); sx < area.x() + area.width(); sx++) {
                buffer.setCell(sx, y, new Cell('─', Style.DEFAULT));
            }
            y++;
        }

        // Data rows
        int startRow = Math.max(0, state.selectedIndex() - (area.height() - y + area.y()) + 1);
        for (int i = startRow; i < rows.size() && y < area.y() + area.height(); i++) {
            T row = rows.get(i);
            Style rowStyle = (i == state.selectedIndex()) ? selectedStyle : Style.DEFAULT;

            x = area.x();
            for (int c = 0; c < columns.size(); c++) {
                Column<T> col = columns.get(c);
                int w = Math.min(col.width(), area.width() - (x - area.x()));
                String value = col.extractor().apply(row);
                String display = value.length() > w ? value.substring(0, w) : value;
                buffer.setString(x, y, display, rowStyle);
                // Pad
                for (int px = x + display.length(); px < x + w; px++) {
                    buffer.setCell(px, y, new Cell(' ', rowStyle));
                }
                x += w + 1;
                if (x >= area.x() + area.width()) break;
            }
            y++;
        }
    }
}
