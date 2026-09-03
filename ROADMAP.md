# Roadmap

Checklist-style plan from the current v1.0.0 baseline toward a portfolio-grade
project: solid engineering foundations first, then AI, then game modes, then
stretch goals. Check items off as they land; open a matching GitHub Issue per
item when you start it.

## v1.1 — Engineering foundations

Everything below unlocks testing, AI, and CI — do this before adding features.

- [ ] Extract a Swing-free `MatchEngine` / `MatchState` from `GamePanel`: pure
      state + a `step()` method, no rendering, no input. `GamePanel` becomes a
      thin view/controller on top of it.
- [ ] Add JUnit 5 and cover the pure logic first: ball/crossbar/player
      collisions, goal detection, golden goal timer.
- [ ] Migrate from a raw Eclipse project to Maven or Gradle.
- [ ] Add GitHub Actions CI: build + run tests on every push/PR, badge in
      README.
- [ ] Wire in static analysis (Checkstyle/PMD/SpotBugs) as a CI check.
- [ ] Add a formatter (Spotless + google-java-format) and a pre-commit hook.
- [ ] Cleanup pass: remove the dead code found in review (unused `Field`
      wall/limit constants, unused `Player.headImage`).
- [ ] Replace ad-hoc `System.out`/nothing with SLF4J logging.
- [ ] Add `module-info.java` (JPMS) — `.classpath` already targets
      `module=true`.

## v1.2 — Polish & persistence

- [ ] Sound effects: kick, goal, crowd cheer.
- [ ] Background music with a volume control.
- [ ] Settings screen: key rebinding, volume, fullscreen toggle.
- [ ] Persist settings to disk (`Properties` or JSON under a user config dir).
- [ ] Persist local match stats/high scores.
- [ ] Externalize UI strings into a `ResourceBundle` (German + English
      toggle).
- [ ] Colorblind-friendly palette option.

## v2.0 — AI opponent

- [ ] AI v1: rule-based state machine (track ball, move toward it, kick when
      close).
- [ ] AI v2: utility AI using `MatchEngine` lookahead to predict ball
      trajectory.
- [ ] Difficulty levels: tune reaction time, aim error, aggression.
- [ ] "vs AI" mode in the menu (1 player).
- [ ] Stretch: self-play trained agent (genetic algorithm / NEAT) via a
      headless `MatchEngine` simulation running thousands of matches without
      rendering.

## v2.1 — Game modes

- [ ] Tournament mode across the 10 teams (round robin or knockout).
- [ ] Power-ups: giant head, low gravity, second ball.
- [ ] Multiple stadiums/arenas with gimmicks (wind, moving platforms,
      different goal heights).
- [ ] Gamepad support (JInput or a Java HID API).

## v3.0 — Stretch goals

- [ ] LAN multiplayer: lockstep netcode built on the existing deterministic
      fixed-timestep simulation (only inputs need to be exchanged).
- [ ] Online multiplayer.
- [ ] Match replays: store the RNG seed + input log, replay through
      `MatchEngine` (no video needed — determinism makes this cheap).
- [ ] `jpackage` native installers (.exe / .msi / .dmg).
- [ ] Optional sprite-based art pipeline as an alternative to the procedural
      renderer.

## Engineering practices (ongoing, not tied to a version)

- [ ] Conventional Commits + auto-generated changelog (e.g. release-please).
- [ ] Architecture Decision Records under `docs/adr/` for major design
      choices.
- [ ] Issue tracker with labels/milestones mirroring this roadmap.
- [ ] Branch-per-feature with self-review PRs, even solo — keeps history
      readable.
- [ ] Code coverage tracking (JaCoCo) with a minimum threshold enforced in
      CI.
- [ ] Javadoc published via GitHub Pages.
- [ ] CI builds and attaches a runnable jar/installer to each tagged
      release.
- [ ] Performance profiling pass (JFR/VisualVM) once AI/netcode add real
      load.
- [ ] Make existing implicit patterns explicit: State (game states),
      Strategy (AI difficulty), Observer (goal/match events).
