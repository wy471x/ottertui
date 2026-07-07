package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
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
import com.ottertui.widgets.ClearWidget;
import com.ottertui.widgets.ListWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new LComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.UP, Set.of(), 0, () -> { });
        runner.keyBindings().bind(KeyCode.DOWN, Set.of(), 0, () -> { });
        // Note: key forwarding delegated to MenuComponent in AllExamplesApp;
        // standalone use works via TuiRunner keyBindings directly
        runner.run();
    }

    public static LComponent createComponent() { return new LComponent(); }

    static class LComponent extends Component implements InteractiveExample {
        private final ListWidget list = new ListWidget(new ArrayList<>(List.of(
            "Dashboard", "Metrics & Monitoring", "Application Logs",
            "System Settings", "User Management", "API Keys & Tokens",
            "Billing & Invoices", "Security Audit", "Help & Support",
            "About OtterTUI"
        )));

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "up"   -> list.moveUp();
                case "down" -> list.moveDown();
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new ClearWidget().render(area, buffer);

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" ListWidget ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            var sidebar = Block.bordered().title(" Menu ");
            Rect sidebarArea = new Rect(inner.x(), inner.y(), 28, inner.height());
            sidebar.render(sidebarArea, buffer);
            var listInner = sidebar.innerRect(sidebarArea);
            list.render(listInner, buffer);

            var info = Block.bordered(BorderStyle.ROUNDED)
                .title(" Details ")
                .titleStyle(new Style(Color.GREEN, Color.RESET, Set.of(Modifier.BOLD)));
            Rect infoArea = new Rect(inner.x() + 29, inner.y(),
                inner.width() - 29, inner.height());
            info.render(infoArea, buffer);
            var infoInner = info.innerRect(infoArea);

            buffer.setString(infoInner.x(), infoInner.y(),
                "Selected item #" + list.selectedIndex() + ":",
                new Style(Color.YELLOW, Color.RESET, Set.of(Modifier.BOLD)));
            buffer.setString(infoInner.x(), infoInner.y() + 2,
                list.selectedItem(), new Style(Color.WHITE, Color.RESET, Set.of(Modifier.BOLD)));
            buffer.setString(infoInner.x(), infoInner.y() + 4,
                "Use UP/DOWN arrows to navigate.", Style.DEFAULT);
            buffer.setString(infoInner.x(), infoInner.y() + 5,
                "The selected item is highlighted.", Style.DEFAULT);
            buffer.setString(infoInner.x(), infoInner.y() + 6,
                "Items beyond visible area scroll.", Style.DEFAULT);
        }
    }
}
