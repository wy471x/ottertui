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
import com.ottertui.widgets.Clear;
import com.ottertui.widgets.TableState;
import com.ottertui.widgets.Table;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TableExample {

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var runner = new TuiRunner(backend, new TblComponent());
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.run();
    }

    public static TblComponent createComponent() { return new TblComponent(); }

    static class TblComponent extends Component implements InteractiveExample {
        private final TableState tableState = new TableState();

        record Person(String name, String role, String email) {}

        private final List<Person> people = List.of(
            new Person("Alice Johnson", "Engineer", "alice@example.com"),
            new Person("Bob Smith", "Designer", "bob@example.com"),
            new Person("Carol White", "Manager", "carol@example.com"),
            new Person("Dave Brown", "DevOps", "dave@example.com"),
            new Person("Eve Davis", "QA Lead", "eve@example.com"),
            new Person("Frank Miller", "Architect", "frank@example.com"),
            new Person("Grace Wilson", "Intern", "grace@example.com"),
            new Person("Hank Taylor", "Director", "hank@example.com"),
            new Person("Ivy Anderson", "Analyst", "ivy@example.com"),
            new Person("Jack Thomas", "Support", "jack@example.com")
        );

        private final Table<Person> table = new Table<>(
            List.of(
                new Table.Column<>("Name", p -> p.name(), 18),
                new Table.Column<>("Role", p -> p.role(), 12),
                new Table.Column<>("Email", p -> p.email(), 22)
            ),
            people
        );

        @Override
        public void handleKey(String key) {
            switch (key) {
                case "up"   -> tableState.moveUp();
                case "down" -> tableState.moveDown();
                default -> { }
            }
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);

            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" Table ")
                .titleStyle(new Style(Color.CYAN, Color.RESET, Set.of(Modifier.BOLD)));
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            table.render(tableState, new Rect(inner.x(), inner.y(),
                inner.width(), inner.height() - 2), buffer);

            int infoY = inner.y() + inner.height() - 2;
            buffer.setString(inner.x(), infoY,
                "Selected row: " + tableState.selectedIndex()
                + " | UP/DOWN to navigate", Style.DEFAULT);
        }
    }
}
