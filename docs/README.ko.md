# OtterTUI

<p align="center">
  <img src="../assets/logo.png" alt="OtterTUI logo" width="320">
</p>

[![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.1-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![JLine](https://img.shields.io/badge/JLine-4.3.1-4B8BBE)](https://github.com/jline/jline3)
[![Aesh](https://img.shields.io/badge/Aesh-3.16.2-8A2BE2)](https://github.com/aeshell/aesh)
[![Lanterna](https://img.shields.io/badge/Lanterna-3.1.5-555555)](https://github.com/mabe02/lanterna)
[![JUnit](https://img.shields.io/badge/JUnit-6.1.1-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![codecov](https://codecov.io/gh/ottertui/ottertui/branch/main/graph/badge.svg)](https://codecov.io/gh/ottertui/ottertui)
[![License](https://img.shields.io/badge/License-MIT-blue)](./LICENSE)

[ratatui](https://github.com/ratatui/ratatui)에서 영감을 받은 Java용 현대적인 터미널 UI 라이브러리입니다.

[English](../README.md) | [中文](README.zh.md) | [日本語](README.ja.md) | **한국어**

## 기능

- **즉시 모드 렌더링** — 위젯을 `Buffer`에 렌더링하고 더블 버퍼링으로 깜빡임 제거
- **다중 백엔드** — JLine3 (기본), Aesh (무의존성), Lanterna, FFM API (JDK 22+)
- **터미널 이미지 렌더링** — Kitty, iTerm2, Sixel 프로토콜 지원
- **선언적 레이아웃** — 백분율, 고정, 비례, 최소 제약
- **풍부한 위젯 라이브러리** — Block, Paragraph, List, Table, Sparkline, BarChart, Gauge, Tabs, Image
- **컴포넌트 트리** — 포커스 관리 및 키 바인딩이 포함된 관리형 이벤트 루프
- **유창한 DSL** — 선언적 UI 구성을 위한 툴킷 레이어
- **유니코드 인식** — CJK 문자와 이모지의 올바른 표시 너비 처리
- **JDK 21+** (FFM 백엔드는 JDK 22+ 필요)

## 모듈

```
ottertui/
├── ottertui-core           ← Buffer, Style, Widget, Layout, Text 모델, InputEvent
├── ottertui-widgets        ← Block, Paragraph, List, Table, Sparkline, Gauge, Image, ...
├── ottertui-tui            ← Component, TuiRunner, KeyBindings, BackendSelector
├── ottertui-toolkit        ← 유창한 DSL, StyleSheet, ThemeManager
├── ottertui-backend-jline  ← JLine3 백엔드 (권장, 최고의 크로스 플랫폼)
├── ottertui-backend-aesh   ← Aesh 백엔드 (무의존성, SSH/Telnet 내장)
├── ottertui-backend-lanterna ← Lanterna 백엔드 (JDK 8+ 호환)
├── ottertui-backend-ffm    ← FFM API 백엔드 (JDK 22+, 직접 POSIX 시스템 콜)
└── ottertui-examples       ← 데모 애플리케이션
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
| ottertui-core | 100% |
| ottertui-widgets | 98% |
| ottertui-tui | 91% |
| ottertui-toolkit | 96% |

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

### 터미널 이미지 렌더링

Kitty, iTerm2 또는 Sixel 프로토콜을 사용하여 터미널에서 직접 이미지 렌더링:

```java
// 파일에서
var img = ImageWidget.fromFile("logo.png", 40, 20, TerminalImage.Protocol.KITTY);

// 클래스패스 리소스에서 (프로토콜 자동 감지)
var img = ImageWidget.fromResource("/banner.jpg", 30, 15);

// 원시 바이트 배열에서
var img = ImageWidget.fromBytes(pngBytes, 24, 12, TerminalImage.Protocol.SIXEL);

// BufferedImage에서
var img = ImageWidget.fromImage(bufferedImage, 20, 10);

// 버퍼에 렌더링
img.render(new Rect(0, 0, 40, 20), buffer);
```

지원 프로토콜 (환경 변수에서 자동 감지):

| 프로토콜 | 형식 | 품질 |
|---------|------|------|
| Kitty | APC 이스케이프 (`\033_G...\033\`) | 24-bit PNG |
| iTerm2 | OSC 1337 (`\033]1337;File=...\007`) | 24-bit PNG |
| Sixel | DCS 이스케이프 (`\033Pq...\033\`) | 256색 팔레트 |

## 백엔드 선택

```java
// 자동 선택 (JLine3 → Aesh → Lanterna)
var backend = BackendSelector.create();

// 명시적 지정
var backend = new JLineBackend();
var backend = new AeshBackend();
var backend = new LanternaBackend();
var backend = new FfmBackend();  // JDK 22+, --enable-native-access=ALL-UNNAMED

// 시스템 속성 경유
// -Dottertui.backend=jline
// -Dottertui.backend=aesh
// -Dottertui.backend=lanterna
// -Dottertui.backend=ffm
```

| 백엔드 | JDK | 의존성 | 특징 |
|--------|-----|--------|------|
| JLine3 | 21+ | JLine | 최고의 크로스 플랫폼, 성숙함 |
| Aesh | 8+ | 없음 | 무의존성, SSH/Telnet 내장 |
| Lanterna | 8+ | Lanterna | 구버전 JDK와 호환 |
| FFM | 22+ | Jansi | 직접 POSIX 시스템 콜, 최소 오버헤드 |

## 라이선스

MIT
