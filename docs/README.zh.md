# OtterTUI

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-3.28-4B8BBE)](https://github.com/jline/jline3)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-5.11-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![codecov](https://codecov.io/gh/ottertui/ottertui/branch/main/graph/badge.svg)](https://codecov.io/gh/ottertui/ottertui)
[![License](https://img.shields.io/badge/License-MIT-blue)](./LICENSE)

一个现代化的 Java 终端 UI 库，灵感来源于 [ratatui](https://github.com/ratatui/ratatui)。

[English](../README.md) | **中文** | [日本語](README.ja.md) | [한국어](README.ko.md)

## 特性

- **即时模式渲染** — widget 渲染到 `Buffer`，双缓冲消除闪烁
- **后端抽象** — 可插拔的终端后端（JLine3、Lanterna）
- **声明式布局** — 百分比、固定、比例和最小约束
- **丰富的 widget 库** — Block、Paragraph、List、Table、Sparkline、BarChart、Gauge、Tabs
- **组件树** — 托管事件循环，支持焦点管理和按键绑定
- **流式 DSL** — toolkit 层用于声明式 UI 组合
- **Unicode 感知** — 正确处理 CJK 字符和 emoji 的显示宽度
- **JDK 21+**

## 模块

```
ottertui/
├── ottertui-core          ← Buffer, Style, Widget, Layout, Text 模型, InputEvent
├── ottertui-widgets       ← Block, Paragraph, List, Table, Sparkline, Gauge, ...
├── ottertui-tui           ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit       ← 流式 DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline ← JLine3 后端（推荐）
├── ottertui-backend-lanterna ← Lanterna 后端（兼容 JDK 8+）
└── ottertui-examples      ← 示例应用
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
| ottertui-core | 97% |
| ottertui-widgets | 99% |
| ottertui-tui | 90% |
| ottertui-toolkit | 97% |

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

## 后端选择

```java
// 自动选择（JLine3 > Lanterna）
var backend = BackendSelector.create();

// 显式指定
var backend = new JLineBackend();
var backend = new LanternaBackend();

// 通过系统属性
// -Dottertui.backend=jline
// -Dottertui.backend=lanterna
```

## 许可证

Apache-2.0
