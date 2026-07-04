package com.ottertui.toolkit;

import com.ottertui.core.*;
import com.ottertui.tui.*;
import com.ottertui.widgets.*;

import java.util.List;

public class TuiApp {
    private final Element root;
    private final TuiRunner runner;

    TuiApp(Element root) {
        this.root = root;
        var component = compile(root);
        this.runner = new TuiRunner(BackendSelector.create(), component);
    }

    public void run() {
        runner.run();
    }

    public TuiRunner runner() { return runner; }

    private Component compile(Element element) {
        return switch (element) {
            case Element.Container c -> {
                var container = new ContainerComponent(c.direction(), c.gap());
                for (var child : c.children()) {
                    container.addChild(compile(child));
                }
                yield container;
            }
            case Element.WidgetElement w -> new WidgetComponent(w.widget());
            case Element.TextElement t -> new WidgetComponent(
                new ParagraphWidget(t.text(), t.style(),
                    com.ottertui.core.Alignment.LEFT, true));
        };
    }

    static class ContainerComponent extends Component {
        private final Layout.Direction direction;
        private final int gap;
        private final List<Constraint> constraints;

        ContainerComponent(Layout.Direction direction, int gap) {
            this.direction = direction;
            this.gap = gap;
            this.constraints = List.of();
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            var children = children();
            if (children.isEmpty()) return;

            // Equal proportional layout
            var layoutConstraints = new java.util.ArrayList<Constraint>();
            for (int i = 0; i < children.size(); i++) {
                layoutConstraints.add(Constraint.proportional(1));
            }
            var layout = new Layout(direction, layoutConstraints, gap);
            var areas = layout.split(area);

            for (int i = 0; i < children.size() && i < areas.size(); i++) {
                children.get(i).render(areas.get(i), buffer);
            }
        }
    }

    static class WidgetComponent extends Component {
        private final Widget widget;

        WidgetComponent(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void render(Rect area, Buffer buffer) {
            widget.render(area, buffer);
        }
    }
}
