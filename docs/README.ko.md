# OtterTUI

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-3.28-4B8BBE)](https://github.com/jline/jline3)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-5.11-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue)](./LICENSE)

[ratatui](https://github.com/ratatui/ratatui)에서 영감을 받은 Java용 현대적인 터미널 UI 라이브러리입니다.

[English](../README.md) | [中文](README.zh.md) | [日本語](README.ja.md) | **한국어**

## 기능

- **즉시 모드 렌더링** — 위젯을 `Buffer`에 렌더링하고 더블 버퍼링으로 깜빡임 제거
- **백엔드 추상화** — 플러그 가능한 터미널 백엔드 (JLine3, Lanterna)
- **선언적 레이아웃** — 백분율, 고정, 비례, 최소 제약
- **풍부한 위젯 라이브러리** — Block, Paragraph, List, Table, Sparkline, BarChart, Gauge, Tabs
- **컴포넌트 트리** — 포커스 관리 및 키 바인딩이 포함된 관리형 이벤트 루프
- **유창한 DSL** — 선언적 UI 구성을 위한 툴킷 레이어
- **유니코드 인식** — CJK 문자와 이모지의 올바른 표시 너비 처리
- **JDK 21+**

## 모듈

```
ottertui/
├── ottertui-core          ← Buffer, Style, Widget, Layout, Text 모델, InputEvent
├── ottertui-widgets       ← Block, Paragraph, List, Table, Sparkline, Gauge, ...
├── ottertui-tui           ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit       ← 유창한 DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline ← JLine3 백엔드 (권장)
├── ottertui-backend-lanterna ← Lanterna 백엔드 (JDK 8+ 호환)
└── ottertui-examples      ← 데모 애플리케이션
```

## 빠른 시작

### 빌드 및 테스트

```bash
# 모든 모듈 빌드
./gradlew build

# 모든 테스트 실행
./gradlew test

# 테스트 실행 및 커버리지 리포트 생성
./gradlew test jacocoTestReport
```

커버리지 리포트는 각 모듈의 `build/reports/jacoco/test/html/index.html`에 생성됩니다.

| 모듈 | 라인 커버리지 |
|------|-------------|
| ottertui-core | 97% |
| ottertui-widgets | 99% |
| ottertui-tui | 90% |
| ottertui-toolkit | 97% |

### 데모 실행

```bash
# 실제 터미널에서 빌드 및 실행 (IDE에서 실행하지 마세요)
gradle :ottertui-examples:installDist
ottertui-examples/build/install/ottertui-examples/bin/ottertui-examples
```

`q` 키로 종료, 화살표 키로 탐색합니다.

### 사용법

**레벨 1 — 즉시 모드 (위젯만):**

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

**레벨 2 — 관리형 이벤트 루프:**

```java
var backend = new JLineBackend();
var root = new MyComponent();
var runner = new TuiRunner(backend, root);

runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
runner.run();
```

**레벨 3 — 선언적 DSL:**

```java
var app = Toolkit.build(root -> root
    .vertical(
        b -> b.text("Header").widget(new MyWidget())
    )
);
app.run();
```

### 레이아웃

```java
var layout = Layout.horizontal(List.of(
    Constraint.percentage(30),
    Constraint.fixed(20),
    Constraint.proportional(1)
));
var areas = layout.split(rect);
```

## 백엔드 선택

```java
// 자동 선택 (JLine3 > Lanterna)
var backend = BackendSelector.create();

// 명시적 지정
var backend = new JLineBackend();
var backend = new LanternaBackend();

// 시스템 속성 경유
// -Dottertui.backend=jline
// -Dottertui.backend=lanterna
```

## 라이선스

Apache-2.0
