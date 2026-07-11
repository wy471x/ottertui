package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.examples.InteractiveExample;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Clear;
import com.ottertui.widgets.Tabs;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TabsExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new TComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> { });
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> { });
        runner.run();
    }

    public static TComponent createComponent() { return new TComponent(); }

    static class TComponent extends Component implements InteractiveExample {
        private final Tabs tabs = new Tabs(List.of(
            "Overview", "Metrics", "Logs", "Settings", "About"
        ));

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "left"  -> tabs.select(Math.max(0, tabs.selected() - 1));
                case "right" -> tabs.select(Math.min(4, tabs.selected() + 1));
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" Tabs ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            tabs.render(new Rect(inner.x(), inner.y(), inner.width(), 1), buffer);

            for (int x = inner.x(); x < inner.x() + inner.width(); x++) {
                buffer.setCell(x, inner.y() + 1, new Cell('─', Style.DEFAULT));
            }

            int contentY = inner.y() + 3;
            String[] tabContents = {
                "Overview: System status summary and key metrics at a glance.",
                "Metrics: Detailed performance graphs, CPU, memory, and disk usage.",
                "Logs: Real-time application log viewer with filtering and search.",
                "Settings: Configuration options for the app and its plugins.",
                "About: OtterTUI v0.1.0 — A modern Java terminal UI library."
            };
            String[] tabIcons = {"[O]", "[M]", "[L]", "[S]", "[A]"};

            var contentBlock = Block.bordered(BorderStyle.PLAIN)
                .title(" " + tabIcons[tabs.selected()] + " Content ")
                .titleStyle(new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD)));
            Rect contentArea = new Rect(inner.x(), contentY,
                inner.width(), inner.height() - contentY + inner.y());
            contentBlock.render(contentArea, buffer);

            var ci = contentBlock.innerRect(contentArea);
            buffer.setString(ci.x(), ci.y(), tabContents[tabs.selected()], Style.DEFAULT);
            buffer.setString(ci.x(), ci.y() + 2,
                "Use LEFT/RIGHT arrows to switch tabs.", Style.DEFAULT);
            buffer.setString(ci.x(), ci.y() + 3,
                "Active tab: " + tabs.selected(), Style.DEFAULT);
        }
    }
}
