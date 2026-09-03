# 0007 — State enum and Observer listener in GamePanel

**Status:** Accepted

## Context

`GamePanel` tracked match flow with three independent booleans: `matchEnded`, `paused`,
`goalCelebration` (plus `goldenGoalPending`, which only makes sense *during* a celebration). They
were never truly independent — e.g. `togglePause()` had to check `matchEnded || goalCelebration`
before touching `paused` — so the actual set of valid combinations was smaller than what three
booleans can represent, and that invariant lived only in scattered `if` conditions rather than
being expressed anywhere.

Separately, goal-scored and match-ended events were handled by directly mutating `matchEvents`
and calling `repaint()` inline in `recordGoal()`/`endMatch()` — correct, but with no seam for
anything else (a future sound effect, a replay recorder) to react to the same events without
editing those methods again.

## Decision

**State:** replaced the three booleans with one `private enum MatchState { PLAYING, PAUSED,
GOAL_CELEBRATION, ENDED }` field. Every place that used to check/set a combination of booleans now
checks/sets the single `state` field, so illegal combinations (e.g. "paused *and* ended") are no
longer representable. A plain enum + `switch`/`if` was used rather than a full GoF State pattern
(one class per state implementing a common interface) — with only four states and mostly shared
transition logic in one class, an interface hierarchy would add indirection without a matching
benefit; see [Simple State Machines](https://en.wikipedia.org/wiki/State_pattern) for when the
fuller pattern earns its cost (it doesn't, here).

**Observer:** added `MatchListener` (`onGoal(scoringTeamName, ownGoal, goldenGoal)`,
`onMatchEnded(team1Name, score1, team2Name, score2)`), registered via
`GamePanel.addMatchListener(...)`. `GamePanel` now registers exactly one internal listener itself
— `LoggingMatchListener`, which logs both events via SLF4J — proving the seam actually works
rather than adding it speculatively with zero consumers.

## Consequences

- `isPaused()`/`isMatchEnded()` are now simple `state == MatchState.X` checks instead of reading
  independent flags that happened to encode the same information.
- Any future subscriber (a `SoundManager` playing a goal horn, a replay recorder logging inputs)
  can call `addMatchListener(...)` from outside `GamePanel` without touching its internals — this
  is the intended hook for the sound-effects and replay items already on ROADMAP.md.
- The Strategy pattern flagged in the same review round (for AI difficulty) was *not* added here —
  see [0008](0008-i18n-and-deferred-strategy-pattern.md) for why.
