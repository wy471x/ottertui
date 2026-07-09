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

[ratatui](https://github.com/ratatui/ratatui) にインスパイアされた、Java 向けのモダンなターミナル UI ライブラリです。

[English](../README.md) | [中文](README.zh.md) | **日本語** | [한국어](README.ko.md)

## 特徴

- **イミディエイトモードレンダリング** — ウィジェットを `Buffer` にレンダリングし、ダブルバッファでちらつきを防止
- **複数バックエンド** — JLine3（デフォルト）、Aesh（ゼロ依存）、Lanterna、FFM API（JDK 22+）
- **端末画像レンダリング** — Kitty、iTerm2、Sixel プロトコルをサポート
- **宣言的レイアウト** — パーセント、固定、比例、最小制約
- **豊富なウィジェットライブラリ** — Block、Paragraph、List、Table、Sparkline、BarChart、Gauge、Tabs、Image
- **コンポーネントツリー** — フォーカス管理とキーバインドを備えた管理イベントループ
- **流暢な DSL** — 宣言的 UI 構成のためのツールキット層
- **Unicode 対応** — CJK 文字と絵文字の表示幅を正しく処理
- **JDK 21+**（FFM バックエンドは JDK 22+ が必要）

## モジュール

```
ottertui/
├── ottertui-core           ← Buffer, Style, Widget, Layout, Text モデル, InputEvent
├── ottertui-widgets        ← Block, Paragraph, List, Table, Sparkline, Gauge, Image, ...
├── ottertui-tui            ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit        ← 流暢な DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline  ← JLine3 バックエンド（推奨、最高のクロスプラットフォーム）
├── ottertui-backend-aesh   ← Aesh バックエンド（ゼロ依存、SSH/Telnet 内蔵）
├── ottertui-backend-lanterna ← Lanterna バックエンド（JDK 8+ 互換）
├── ottertui-backend-ffm    ← FFM API バックエンド（JDK 22+、直接 POSIX システムコール）
└── ottertui-examples       ← デモアプリケーション
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
| ottertui-core | 100% |
| ottertui-widgets | 98% |
| ottertui-tui | 91% |
| ottertui-toolkit | 96% |

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

### 端末画像レンダリング

Kitty、iTerm2、または Sixel プロトコルを使用して端末内に直接画像を表示：

```java
// ファイルから
var img = ImageWidget.fromFile("logo.png", 40, 20, TerminalImage.Protocol.KITTY);

// クラスパスリソースから（プロトコル自動検出）
var img = ImageWidget.fromResource("/banner.jpg", 30, 15);

// 生のバイト配列から
var img = ImageWidget.fromBytes(pngBytes, 24, 12, TerminalImage.Protocol.SIXEL);

// BufferedImage から
var img = ImageWidget.fromImage(bufferedImage, 20, 10);

// バッファにレンダリング
img.render(new Rect(0, 0, 40, 20), buffer);
```

対応プロトコル（環境変数から自動検出）：

| プロトコル | 形式 | 品質 |
|-----------|------|------|
| Kitty | APC エスケープ (`\033_G...\033\`) | 24-bit PNG |
| iTerm2 | OSC 1337 (`\033]1337;File=...\007`) | 24-bit PNG |
| Sixel | DCS エスケープ (`\033Pq...\033\`) | 256 色パレット |

## バックエンド選択

```java
// 自動選択（JLine3 → Aesh → Lanterna）
var backend = BackendSelector.create();

// 明示的に指定
var backend = new JLineBackend();
var backend = new AeshBackend();
var backend = new LanternaBackend();
var backend = new FfmBackend();  // JDK 22+, --enable-native-access=ALL-UNNAMED

// システムプロパティ経由
// -Dottertui.backend=jline
// -Dottertui.backend=aesh
// -Dottertui.backend=lanterna
// -Dottertui.backend=ffm
```

| バックエンド | JDK | 依存 | 特徴 |
|-------------|-----|------|------|
| JLine3 | 21+ | JLine | 最高のクロスプラットフォーム、成熟 |
| Aesh | 8+ | なし | ゼロ依存、SSH/Telnet 内蔵 |
| Lanterna | 8+ | Lanterna | 古い JDK と互換 |
| FFM | 22+ | Jansi | 直接 POSIX システムコール、最小オーバーヘッド |

## ライセンス

MIT
