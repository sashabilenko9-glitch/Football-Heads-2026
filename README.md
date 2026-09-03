# Kopf-Fußball 2026 ⚽

A local 2-player "Head Soccer" style game built in Java Swing — fast, chaotic couch matches with exaggerated physics, golden goal overtime, and a match log at full time.

![Java](https://img.shields.io/badge/Java-21%2B-orange)
![Build](https://img.shields.io/badge/build-Gradle-02303A?logo=gradle)
![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey)
![Version](https://img.shields.io/badge/version-1.0.0-brightgreen)

<!-- Once pushed to GitHub, replace <owner>/<repo> below to show the live CI badge:
![CI](https://github.com/<owner>/<repo>/actions/workflows/ci.yml/badge.svg) -->

> Note: the in-game UI text is in German (this was originally a university programming assignment). This document is in English for a wider audience.

See [CHANGELOG.md](CHANGELOG.md) for release notes, [ROADMAP.md](ROADMAP.md) for what's planned next, [CONTRIBUTING.md](CONTRIBUTING.md) for the dev workflow, and [docs/adr/](docs/adr/) for the reasoning behind non-obvious decisions.

## Screenshots

<!-- Add screenshots here, e.g.: -->
<!-- ![Menu](screenshots/menu.png) -->
<!-- ![Match](screenshots/match.png) -->
<!-- ![Match end](screenshots/end.png) -->

## Features

- **2 players, one keyboard** — local hot-seat multiplayer, no network needed.
- **Physics-driven gameplay** — gravity, crossbar bounces, ball spin, player-vs-player collisions, all running on a fixed-timestep loop so speed stays consistent regardless of frame rate.
- **60-second matches with Golden Goal** — a draw at full time triggers sudden-death overtime.
- **10 selectable teams**, each modeled after a real footballer's shirt colors (Messi, Mbappé, Pelé, Haaland, Lewandowski, Maldini, and more).
- **On-screen touch controls** — an overlay with WASD / arrow-key buttons for mouse or touch input, alongside full keyboard support.
- **Resizable window with letterboxing** — the field keeps its aspect ratio and stays perfectly round at any window size.
- **Match summary screen** — goal-by-goal log (including own goals and golden goals) with a rematch/menu option.

## Controls

| Action     | Player 1 (left) | Player 2 (right) |
|------------|:----------------:|:-----------------:|
| Move       | `A` / `D`         | `←` / `→`          |
| Jump       | `W`               | `↑`                |
| Kick       | `S`               | `↓`                |
| Pause      | `Esc`             | `Esc`              |

On-screen buttons for both players are also available via the **TASTEN** toggle during a match (mouse/touch only supports one button at a time per cursor).

## Getting started

### Requirements

- JDK 21 or newer to build (a JDK 21 toolchain is auto-provisioned by Gradle if you don't have one)
- Nothing else — the Gradle Wrapper (`gradlew`/`gradlew.bat`) downloads Gradle itself on first run

### Run from the command line

```bash
./gradlew run          # Linux/macOS
gradlew.bat run        # Windows
```

### Build a runnable jar

```bash
./gradlew build
java -jar build/libs/football-heads-2026-1.0.0-all.jar   # bundles slf4j/logback, runs standalone
```

### Run the quality gates locally

```bash
./gradlew check         # tests + coverage gate + checkstyle + PMD + SpotBugs + format check
./gradlew spotlessApply # auto-fix formatting
```

### Run from Eclipse

Eclipse's Buildship plugin reads `build.gradle` directly:
`File → Import → Gradle → Existing Gradle Project`, select this folder, run `com.footballheads.Main`.

### Generate the Javadoc

```bash
./gradlew javadoc
# output: build/docs/javadoc/index.html
```

## Project structure

```
src/main/java/
├── com/footballheads/   # Application entry point, game window, game loop, input handling
│   ├── Main.java
│   ├── GameFrame.java
│   ├── GamePanel.java
│   └── InputManager.java
├── entities/             # Game objects and their physics
│   ├── Ball.java
│   ├── Player.java
│   ├── Goal.java
│   ├── Stadium.java
│   └── Team.java
├── ui/                   # Menus, HUD, overlays
│   ├── MenuPanel.java
│   ├── TeamSelectPanel.java
│   ├── ControlsPanel.java
│   ├── InGameUI.java
│   ├── PauseMenu.java
│   └── MobileControlsPanel.java
└── utils/
    └── Field.java        # Shared field constants

src/test/java/            # JUnit 5 tests for entities/utils (see docs/adr/0006-coverage-scope.md)
```

## How it works

- **Fixed-timestep game loop** — a Swing `Timer` acts only as a clock; physics advance in fixed `1/60s` steps via an accumulator, so gameplay speed doesn't depend on rendering jitter or lag spikes.
- **Two-circle player collision** — the ball is checked against a head circle and a separate body/legs circle, so it can't tunnel through a player's legs.
- **Letterboxed rendering** — the scene is drawn at a fixed 1000×700 logical resolution and uniformly scaled/centered to fit any window size, keeping the ball and heads perfectly round.
- **Layered goal rendering** — each goal is drawn in two passes (net + back post, then crossbar + front post) so players and the ball can visually appear *behind* the net, creating a simple depth illusion.

## License

All rights reserved. This repository is shared for portfolio and educational purposes; no license is granted to use, copy, modify, or redistribute the code without permission from the author.

---

Built as a university programming assignment (Hochschule Mittweida) and continued as a personal project.
