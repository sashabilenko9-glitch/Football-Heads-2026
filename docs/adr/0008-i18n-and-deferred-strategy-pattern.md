# 0008 — ResourceBundle i18n now, Strategy pattern for AI deferred

**Status:** Accepted

## Context

Two related asks came out of the same review round: externalize the German UI strings so the game
can be shown in other languages, and make the "Strategy pattern for AI difficulty" mentioned in
ROADMAP.md's engineering-practices section concrete.

## Decision

**i18n:** added `com.footballheads.Messages`, a thin wrapper around `java.util.ResourceBundle`
backed by `src/main/resources/i18n/messages_{de,en}.properties`. `Messages.setLocale(...)` swaps
the active bundle; already-constructed Swing components do **not** update live (Swing has no
built-in "re-skin this tree" mechanism), so `MenuPanel`'s language toggle button rebuilds the menu
(`parentFrame.setContentPane(new MenuPanel(parentFrame))`) after switching. German remains the
default locale, matching the game's original (and still authoritative) language. Team/player names
and the on-screen WASD/arrow-key labels were left untranslated on purpose — they're proper nouns
and icons, not language-dependent copy.

**Strategy pattern for AI: deferred, not implemented.** There is no AI opponent yet (ROADMAP.md
v2.0). Introducing an `AiStrategy` interface today, with no second implementation to swap against
and no caller that needs one, would be exactly the kind of speculative abstraction this codebase's
own conventions warn against — an interface with a single implementation is not a pattern, it's
indirection. The right time to add it is when the first AI difficulty level lands and a second one
is imminent; at that point `entities.Player`'s human-driven `left`/`right`/`jumping`/`kicking`
flags are the natural seam an `AiStrategy.decide(GameState) -> PlayerInput` would plug into,
alongside the `MatchState`/`MatchListener` seams already in place (see
[0007](0007-state-and-observer-patterns.md)).

## Consequences

- Adding a language is "write a new `messages_xx.properties` file, extend `MenuPanel`'s toggle (or
  turn it into a picker) to offer it" — no code changes to any screen that already calls
  `Messages.get(...)`.
- A handful of literals are *not* routed through `Messages` yet: `MobileControlsPanel`'s W/A/S/D
  and arrow glyphs (icons, not words), and window titles ("Kopf-Fußball 2026", a brand name).
  If more UI is added later, route new player-facing copy through `Messages` from the start rather
  than letting hardcoded strings creep back in.
- No `AiStrategy` interface exists in the codebase today — do not add one until there are at least
  two real difficulty implementations to justify it.
