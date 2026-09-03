# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

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
