# OtterTUI

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-3.28-4B8BBE)](https://github.com/jline/jline3)
[![Aesh](https://img.shields.io/badge/Aesh-2.6-8A2BE2)](https://github.com/aeshell/aesh)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-5.11-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![codecov](https://codecov.io/gh/ottertui/ottertui/branch/main/graph/badge.svg)](https://codecov.io/gh/ottertui/ottertui)
[![License](https://img.shields.io/badge/License-MIT-blue)](./LICENSE)

A modern terminal user interface library for Java, inspired by [ratatui](https://github.com/ratatui/ratatui).

**English** | [中文](docs/README.zh.md) | [日本語](docs/README.ja.md) | [한국어](docs/README.ko.md)

## Features

- **Immediate-mode rendering** — widgets render to a `Buffer`, double-buffered to eliminate flicker
- **Multiple backends** — JLine3 (default), Aesh (zero-dependency), Lanterna, FFM API (JDK 22+)
- **Terminal image rendering** — Kitty, iTerm2, and Sixel protocol support
- **Declarative layout** — percentage, fixed, proportional, and minimum constraints
- **Rich widget library** — Block, Paragraph, List, Table, Sparkline, BarChart, Gauge, Tabs, Image
- **Component tree** — managed event loop with focus management and key bindings
- **Fluent DSL** — toolkit layer for declarative UI composition
- **Unicode-aware** — correct display width for CJK characters and emoji
- **JDK 21+** (FFM backend requires JDK 22+)

## Modules

```
ottertui/
├── ottertui-core           ← Buffer, Style, Widget, Layout, Text model, InputEvent
├── ottertui-widgets        ← Block, Paragraph, List, Table, Sparkline, Gauge, Image, ...
├── ottertui-tui            ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit        ← Fluent DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline  ← JLine3 backend (recommended, best cross-platform)
├── ottertui-backend-aesh   ← Aesh backend (zero-dependency, SSH/Telnet built-in)
├── ottertui-backend-lanterna ← Lanterna backend (JDK 8+ compatible)
├── ottertui-backend-ffm    ← FFM API backend (JDK 22+, direct POSIX syscalls)
└── ottertui-examples       ← Demo applications
```

## Quick Start

### Build & Test

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport
```

Coverage reports are generated per module at `build/reports/jacoco/test/html/index.html`.

| Module | Line Coverage |
|--------|--------------|
| ottertui-core | 100% |
| ottertui-widgets | 98% |
| ottertui-tui | 91% |
| ottertui-toolkit | 96% |

### Run the demo

```bash
# Build and run from a real terminal (not an IDE)
gradle :ottertui-examples:installDist
ottertui-examples/build/install/ottertui-examples/bin/ottertui-examples
```

Press `q` to quit, arrow keys to navigate.

### Usage

**Level 1 — Immediate mode (widgets only):**

```java
var backend = new JLineBackend();
backend.enterRawMode();

var size = backend.size();
var buffer = new Buffer(size.width(), size.height());

var block = Block.bordered().title("Hello");
block.render(new Rect(0, 0, size.width(), size.height()), buffer);

backend.flush(buffer);
backend.exitRawMode();
```

**Level 2 — Managed event loop:**

```java
var backend = new JLineBackend();
var root = new MyComponent();
var runner = new TuiRunner(backend, root);

runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
runner.run();
```

**Level 3 — Declarative DSL:**

```java
var app = Toolkit.build(root -> root
    .vertical(
        b -> b.text("Header").widget(new MyWidget())
    )
);
app.run();
```

### Layout

```java
var layout = Layout.horizontal(List.of(
    Constraint.percentage(30),
    Constraint.fixed(20),
    Constraint.proportional(1)
));
var areas = layout.split(rect);
```

### Terminal Image Rendering

Render images directly in the terminal using Kitty, iTerm2, or Sixel protocols:

```java
// From file
var img = ImageWidget.fromFile("logo.png", 40, 20, TerminalImage.Protocol.KITTY);

// From classpath resource (auto-detect protocol)
var img = ImageWidget.fromResource("/banner.jpg", 30, 15);

// From raw bytes
var img = ImageWidget.fromBytes(pngBytes, 24, 12, TerminalImage.Protocol.SIXEL);

// From BufferedImage
var img = ImageWidget.fromImage(bufferedImage, 20, 10);

// Render to buffer
img.render(new Rect(0, 0, 40, 20), buffer);
```

Supported protocols (auto-detected from environment):

| Protocol | Format | Quality |
|----------|--------|---------|
| Kitty | APC escape (`\033_G...\033\`) | 24-bit PNG |
| iTerm2 | OSC 1337 (`\033]1337;File=...\007`) | 24-bit PNG |
| Sixel | DCS escape (`\033Pq...\033\`) | 256-color palette |

## Backend Selection

```java
// Auto-select (JLine3 → Aesh → Lanterna)
var backend = BackendSelector.create();

// Explicit
var backend = new JLineBackend();
var backend = new AeshBackend();
var backend = new LanternaBackend();
var backend = new FfmBackend();  // JDK 22+, --enable-native-access=ALL-UNNAMED

// Via system property
// -Dottertui.backend=jline
// -Dottertui.backend=aesh
// -Dottertui.backend=lanterna
// -Dottertui.backend=ffm
```

| Backend | JDK | Dependencies | Highlights |
|---------|-----|-------------|------------|
| JLine3 | 21+ | JLine | Best cross-platform, mature |
| Aesh | 8+ | none | Zero-dependency, SSH/Telnet built-in |
| Lanterna | 8+ | Lanterna | Compatible with older JDKs |
| FFM | 22+ | Jansi | Direct POSIX syscalls, minimal overhead |

## License

MIT
