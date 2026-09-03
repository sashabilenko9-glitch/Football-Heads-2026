# 0006 — JaCoCo coverage gate scoped to entities/utils only

**Status:** Accepted

## Context

Most of this codebase is Swing UI: menus, HUD, overlays, and `paintComponent`/`draw*` methods
built from dozens of `Graphics2D` calls with no return value and no meaningful assertion beyond
"didn't throw". Measuring coverage across the whole project would make the number mostly reflect
how much rendering code happens to get exercised by a `BufferedImage` smoke test, not how well the
actual game *logic* (physics, collisions, scoring, team selection) is tested.

## Decision

`build.gradle` restricts both the coverage report and the `jacocoTestCoverageVerification` gate to
`entities/**` and `utils/**` class files only (`ui.*` and `com.footballheads.*` are excluded from
the coverage *measurement*, not from testing — nothing stops adding UI tests later, they just
wouldn't move this particular gate). The gate is a `BUNDLE`-level instruction-coverage minimum
(currently 60%, measured at ~75% when this was set), not a per-class minimum — a few classes
being draw-method-heavy shouldn't fail the build if the bundle as a whole is well covered.

Rendering methods within the covered packages (`Ball.draw`, `Player.draw`, `Goal.drawBack/Front`,
`Stadium.drawBackground`) are still exercised, via `RenderingSmokeTest`: each is called against an
off-screen `BufferedImage`'s `Graphics2D`, which works headlessly (no display/window needed) and
is a real regression test, not just a coverage trick — a `NullPointerException` or malformed
polygon in draw code fails exactly the same way there as on screen.

## Consequences

- The coverage number in CI answers "is the physics/scoring/team logic tested", which is the
  question worth gating on. A contributor cannot silently regress collision or scoring logic
  without either adding a test or consciously lowering the gate.
- The gate says nothing about `ui/*` or `GamePanel`'s own game-loop wiring — those still need
  manual verification (`./gradlew run`) before every release, same as before this migration.
- If `GamePanel`'s game-loop logic is ever extracted into a Swing-free `MatchEngine` (ROADMAP.md
  v1.1), that extraction is exactly the point to widen this gate to include it.
