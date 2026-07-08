# OtterTUI

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-3.28-4B8BBE)](https://github.com/jline/jline3)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-5.11-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![codecov](https://codecov.io/gh/ottertui/ottertui/branch/main/graph/badge.svg)](https://codecov.io/gh/ottertui/ottertui)
[![License](https://img.shields.io/badge/License-MIT-blue)](./LICENSE)

[ratatui](https://github.com/ratatui/ratatui) にインスパイアされた、Java 向けのモダンなターミナル UI ライブラリです。

[English](../README.md) | [中文](README.zh.md) | **日本語** | [한국어](README.ko.md)

## 特徴

- **イミディエイトモードレンダリング** — ウィジェットを `Buffer` にレンダリングし、ダブルバッファでちらつきを防止
- **バックエンド抽象化** — プラグ可能なターミナルバックエンド（JLine3、Lanterna）
- **宣言的レイアウト** — パーセント、固定、比例、最小制約
- **豊富なウィジェットライブラリ** — Block、Paragraph、List、Table、Sparkline、BarChart、Gauge、Tabs
- **コンポーネントツリー** — フォーカス管理とキーバインドを備えた管理イベントループ
- **流暢な DSL** — 宣言的 UI 構成のためのツールキット層
- **Unicode 対応** — CJK 文字と絵文字の表示幅を正しく処理
- **JDK 21+**

## モジュール

```
ottertui/
├── ottertui-core          ← Buffer, Style, Widget, Layout, Text モデル, InputEvent
├── ottertui-widgets       ← Block, Paragraph, List, Table, Sparkline, Gauge, ...
├── ottertui-tui           ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit       ← 流暢な DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline ← JLine3 バックエンド（推奨）
├── ottertui-backend-lanterna ← Lanterna バックエンド（JDK 8+ 互換）
└── ottertui-examples      ← デモアプリケーション
```

## クイックスタート

### ビルドとテスト

```bash
# 全モジュールをビルド
./gradlew build

# 全テストを実行
./gradlew test

# テストを実行しカバレッジレポートを生成
./gradlew test jacocoTestReport
```

カバレッジレポートは各モジュールの `build/reports/jacoco/test/html/index.html` に生成されます。

| モジュール | 行カバレッジ |
|-----------|------------|
| ottertui-core | 97% |
| ottertui-widgets | 99% |
| ottertui-tui | 90% |
| ottertui-toolkit | 97% |

### デモの実行

```bash
# 実際のターミナルからビルドして実行（IDE 内では実行しないでください）
gradle :ottertui-examples:installDist
ottertui-examples/build/install/ottertui-examples/bin/ottertui-examples
```

`q` キーで終了、矢印キーでナビゲーションします。

### 使用方法

**レベル 1 — イミディエイトモード（ウィジェットのみ）：**

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

**レベル 2 — 管理イベントループ：**

```java
var backend = new JLineBackend();
var root = new MyComponent();
var runner = new TuiRunner(backend, root);

runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
runner.run();
```

**レベル 3 — 宣言的 DSL：**

```java
var app = Toolkit.build(root -> root
    .vertical(
        b -> b.text("Header").widget(new MyWidget())
    )
);
app.run();
```

### レイアウト

```java
var layout = Layout.horizontal(List.of(
    Constraint.percentage(30),
    Constraint.fixed(20),
    Constraint.proportional(1)
));
var areas = layout.split(rect);
```

## バックエンド選択

```java
// 自動選択（JLine3 > Lanterna）
var backend = BackendSelector.create();

// 明示的に指定
var backend = new JLineBackend();
var backend = new LanternaBackend();

// システムプロパティ経由
// -Dottertui.backend=jline
// -Dottertui.backend=lanterna
```

## ライセンス

Apache-2.0
