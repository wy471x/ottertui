# OtterTUI

<p align="center">
  <img src="../assets/logo.png" alt="OtterTUI logo" width="320">
</p>

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-3.28-4B8BBE)](https://github.com/jline/jline3)
[![Aesh](https://img.shields.io/badge/Aesh-2.6-8A2BE2)](https://github.com/aeshell/aesh)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-5.11-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![codecov](https://codecov.io/gh/ottertui/ottertui/branch/main/graph/badge.svg)](https://codecov.io/gh/ottertui/ottertui)
[![License](https://img.shields.io/badge/License-MIT-blue)](./LICENSE)

一个现代化的 Java 终端 UI 库，灵感来源于 [ratatui](https://github.com/ratatui/ratatui)。

[English](../README.md) | **中文** | [日本語](README.ja.md) | [한국어](README.ko.md)

## 特性

- **即时模式渲染** — widget 渲染到 `Buffer`，双缓冲消除闪烁
- **多后端支持** — JLine3（默认）、Aesh（零依赖）、Lanterna、FFM API（JDK 22+）
- **终端图片渲染** — 支持 Kitty、iTerm2 和 Sixel 协议
- **声明式布局** — 百分比、固定、比例和最小约束
- **丰富的 widget 库** — Block、Paragraph、List、Table、Sparkline、BarChart、Gauge、Tabs、Image
- **组件树** — 托管事件循环，支持焦点管理和按键绑定
- **流式 DSL** — toolkit 层用于声明式 UI 组合
- **Unicode 感知** — 正确处理 CJK 字符和 emoji 的显示宽度
- **JDK 21+**（FFM 后端需要 JDK 22+）

## 模块

```
ottertui/
├── ottertui-core           ← Buffer, Style, Widget, Layout, Text 模型, InputEvent
├── ottertui-widgets        ← Block, Paragraph, List, Table, Sparkline, Gauge, Image, ...
├── ottertui-tui            ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit        ← 流式 DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline  ← JLine3 后端（推荐，最佳跨平台支持）
├── ottertui-backend-aesh   ← Aesh 后端（零依赖，内置 SSH/Telnet）
├── ottertui-backend-lanterna ← Lanterna 后端（兼容 JDK 8+）
├── ottertui-backend-ffm    ← FFM API 后端（JDK 22+，直接 POSIX 系统调用）
└── ottertui-examples       ← 示例应用
```

## 快速开始

### 构建与测试

```bash
# 构建所有模块
./gradlew build

# 运行所有测试
./gradlew test

# 运行测试并生成覆盖率报告
./gradlew test jacocoTestReport
```

覆盖率报告生成在各模块的 `build/reports/jacoco/test/html/index.html`。

| 模块 | 行覆盖率 |
|------|---------|
| ottertui-core | 100% |
| ottertui-widgets | 98% |
| ottertui-tui | 91% |
| ottertui-toolkit | 96% |

### 运行示例

```bash
# 从真实终端构建并运行（不要在 IDE 中运行）
gradle :ottertui-examples:installDist
ottertui-examples/build/install/ottertui-examples/bin/ottertui-examples
```

按 `q` 退出，方向键导航。

### 使用方式

**级别 1 — 即时模式（仅 widget）：**

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

**级别 2 — 托管事件循环：**

```java
var backend = new JLineBackend();
var root = new MyComponent();
var runner = new TuiRunner(backend, root);

runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
runner.run();
```

**级别 3 — 声明式 DSL：**

```java
var app = Toolkit.build(root -> root
    .vertical(
        b -> b.text("Header").widget(new MyWidget())
    )
);
app.run();
```

### 布局

```java
var layout = Layout.horizontal(List.of(
    Constraint.percentage(30),
    Constraint.fixed(20),
    Constraint.proportional(1)
));
var areas = layout.split(rect);
```

### 终端图片渲染

使用 Kitty、iTerm2 或 Sixel 协议直接在终端中渲染图片：

```java
// 从文件加载
var img = ImageWidget.fromFile("logo.png", 40, 20, TerminalImage.Protocol.KITTY);

// 从 classpath 资源加载（自动检测协议）
var img = ImageWidget.fromResource("/banner.jpg", 30, 15);

// 从原始字节数组加载
var img = ImageWidget.fromBytes(pngBytes, 24, 12, TerminalImage.Protocol.SIXEL);

// 从 BufferedImage 加载
var img = ImageWidget.fromImage(bufferedImage, 20, 10);

// 渲染到 buffer
img.render(new Rect(0, 0, 40, 20), buffer);
```

支持的协议（自动从环境变量检测）：

| 协议 | 格式 | 质量 |
|------|------|------|
| Kitty | APC 转义 (`\033_G...\033\`) | 24-bit PNG |
| iTerm2 | OSC 1337 (`\033]1337;File=...\007`) | 24-bit PNG |
| Sixel | DCS 转义 (`\033Pq...\033\`) | 256 色调色板 |

## 后端选择

```java
// 自动选择（JLine3 → Aesh → Lanterna）
var backend = BackendSelector.create();

// 显式指定
var backend = new JLineBackend();
var backend = new AeshBackend();
var backend = new LanternaBackend();
var backend = new FfmBackend();  // JDK 22+, --enable-native-access=ALL-UNNAMED

// 通过系统属性
// -Dottertui.backend=jline
// -Dottertui.backend=aesh
// -Dottertui.backend=lanterna
// -Dottertui.backend=ffm
```

| 后端 | JDK | 依赖 | 亮点 |
|------|-----|------|------|
| JLine3 | 21+ | JLine | 最佳跨平台，成熟稳定 |
| Aesh | 8+ | 无 | 零依赖，内置 SSH/Telnet |
| Lanterna | 8+ | Lanterna | 兼容旧版本 JDK |
| FFM | 22+ | Jansi | 直接 POSIX 系统调用，开销极小 |

## 许可证

MIT
