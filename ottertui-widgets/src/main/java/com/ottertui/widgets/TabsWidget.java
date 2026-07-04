package com.ottertui.widgets;

import com.ottertui.core.*;

import java.util.List;
import java.util.Set;

public class TabsWidget implements Widget {
    private final List<String> titles;
    private int selected = 0;
    private final Style selectedStyle;
    private final Style normalStyle;

    public TabsWidget(List<String> titles) {
        this(titles,
            new Style(Color.BLACK, Color.WHITE, Set.of(Modifier.BOLD)),
            Style.DEFAULT);
    }

    public TabsWidget(List<String> titles, Style selectedStyle, Style normalStyle) {
        this.titles = titles;
        this.selectedStyle = selectedStyle;
        this.normalStyle = normalStyle;
    }

    public void select(int index) {
        if (index >= 0 && index < titles.size()) this.selected = index;
    }

    public int selected() { return selected; }

    @Override
    public void render(Rect area, Buffer buffer) {
        int x = area.x();
        for (int i = 0; i < titles.size(); i++) {
            String title = " " + titles.get(i) + " ";
            if (x + title.length() > area.x() + area.width()) break;

            Style s = (i == selected) ? selectedStyle : normalStyle;
            buffer.setString(x, area.y(), title, s);
            x += title.length();
        }
    }
}
