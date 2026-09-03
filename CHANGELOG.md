# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

Engineering-foundations work from ROADMAP.md's v1.1 milestone. No player-visible gameplay
changes other than the new language toggle.

### Added
- English translation alongside German, with a toggle in the main menu
  (`Messages`/`messages_{de,en}.properties`); German remains the default.
- SLF4J + Logback logging (match start, goals, match end, uncaught exceptions).
- `MatchListener`, an Observer hook for goal/match-end events (currently used
  only for logging; the intended seam for future sound effects/replays).
- JUnit 5 test suite for `entities`/`utils`, including headless rendering
  smoke tests; `./gradlew check` enforces a 60% coverage floor on that layer.
- Gradle build with Checkstyle, PMD, SpotBugs and Spotless wired into
  `./gradlew check`; GitHub Actions CI, a release workflow that attaches a
  runnable jar to tagged releases, a release-please workflow, and a
  Javadoc-to-GitHub-Pages workflow.
- `docs/adr/` — Architecture Decision Records explaining the non-obvious
  choices in this codebase, past and present.

### Changed
- `GamePanel`'s `matchEnded`/`paused`/`goalCelebration` booleans replaced by
  a single `MatchState` enum (State pattern).
- Team roster in `entities/Team.java`: the 10 teams are now named after real
  footballers whose kit colors match each team's palette, instead of course
  staff names.
- Project layout moved to the Gradle-standard `src/main/java`/`src/test/java`.

### Fixed
- Removed dead code (unused `Field` constants, unused `Player.headImage`).
- Missing `serialVersionUID` on Swing components; non-serializable fields
  marked `transient`.
- `TeamSelectPanel` used a default-locale `toUpperCase()` (locale-dependent
  bug risk); now explicit `Locale.ROOT`.
- `Stadium`'s constructor created a second, immediately-discarded `Random`
  instead of reusing the instance field.

## [1.0.0] - 2026-09-03

Initial public release.

### Added
- 2-player local head-soccer gameplay with a fixed-timestep physics loop
  (gravity, crossbar bounces, ball spin, two-circle head/body collision so
  the ball can't tunnel through a player's legs).
- 60-second matches with Golden Goal sudden-death overtime on a draw.
- 10 selectable teams, each styled after a real footballer's shirt colors.
- On-screen touch/mouse control overlay (WASD / arrow-key buttons) alongside
  full keyboard support.
- Resizable window with letterboxed rendering — the field keeps its aspect
  ratio and the ball/heads stay round at any window size.
- Match summary screen with a goal-by-goal log (own goals and golden goals
  marked separately) and a rematch/menu option.
- Pause menu and a dedicated controls/help screen.

### Known limitations
- No sound (no SFX or music yet).
- No AI opponent — both players are human (local hot-seat only).
- No settings persistence — key bindings and window state reset each launch.
- UI text is German only (no localization yet).

See [ROADMAP.md](ROADMAP.md) for what's planned next.
