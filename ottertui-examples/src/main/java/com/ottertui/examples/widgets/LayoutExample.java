package com.ottertui.examples.widgets;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Color;
import com.ottertui.core.Constraint;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Layout;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.examples.InteractiveExample;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LayoutExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new LyComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static LyComponent createComponent() { return new LyComponent(); }

    static class LyComponent extends Component implements InteractiveExample {
        @Override
        public void handleKey(String key) { }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.DOUBLE)
                .title(" Layout System ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            var hLayout = Layout.horizontal(List.of(
                Constraint.percentage(25),
                Constraint.percentage(75)
            ));
            var hAreas = hLayout.split(new Rect(inner.x(), inner.y(),
                inner.width(), inner.height() / 2));

            var sidebar = Block.bordered().title(" Sidebar (25%) ");
            sidebar.render(hAreas.get(0), buffer);
            var si = sidebar.innerRect(hAreas.get(0));
            buffer.setString(si.x(), si.y(), "Fixed-width", Style.DEFAULT);
            buffer.setString(si.x(), si.y() + 1, "sidebar using", Style.DEFAULT);
            buffer.setString(si.x(), si.y() + 2, "Constraint.", Style.DEFAULT);
            buffer.setString(si.x(), si.y() + 3, "percentage(25)", Style.DEFAULT);

            var content = Block.bordered().title(" Content (75%) ");
            content.render(hAreas.get(1), buffer);
            var ci = content.innerRect(hAreas.get(1));
            buffer.setString(ci.x(), ci.y(),
                "Main content area with proportional width.", Style.DEFAULT);

            var vLayout = Layout.vertical(List.of(
                Constraint.fixed(3),
                Constraint.proportional(1),
                Constraint.proportional(1)
            ));
            var vAreas = vLayout.split(new Rect(inner.x(),
                inner.y() + inner.height() / 2 + 1,
                inner.width(), inner.height() / 2 - 1));

            var header = Block.bordered().title(" Header (fixed=3) ");
            header.render(vAreas.get(0), buffer);
            var hi = header.innerRect(vAreas.get(0));
            buffer.setString(hi.x(), hi.y(), "Fixed height header", Style.DEFAULT);

            var panel1 = Block.bordered().title(" Panel A (prop=1) ");
            panel1.render(vAreas.get(1), buffer);
            var pi1 = panel1.innerRect(vAreas.get(1));
            buffer.setString(pi1.x(), pi1.y(), "Proportional split.", Style.DEFAULT);
            buffer.setString(pi1.x(), pi1.y() + 1, "Shares remaining space.", Style.DEFAULT);

            var panel2 = Block.bordered().title(" Panel B (prop=1) ");
            panel2.render(vAreas.get(2), buffer);
            var pi2 = panel2.innerRect(vAreas.get(2));
            buffer.setString(pi2.x(), pi2.y(), "Equal proportion.", Style.DEFAULT);
            buffer.setString(pi2.x(), pi2.y() + 1,
                "Constraint types: %, fixed, min, proportional.", Style.DEFAULT);
        }
    }
}
