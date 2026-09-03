# 0005 — Checkstyle/PMD/SpotBugs/Spotless scope and exceptions

**Status:** Accepted

## Context

Running Checkstyle, PMD and SpotBugs with their default/"kitchen sink" rulesets against this
codebase produced a long list of violations that don't represent real defects here — they mostly
flag conventions this specific project deliberately doesn't follow (wildcard AWT/Swing imports,
public mutable fields on physics structs, Swing components that are never serialized). A gate that
starts red and gets rules silenced one-by-one under time pressure is worse than one that's scoped
correctly from the start.

## Decision

Each tool runs against a custom, checked-in ruleset (`config/checkstyle/checkstyle.xml`,
`config/pmd/ruleset.xml`, `config/spotbugs/exclude.xml`) rather than a stock preset, with each
exception commented at the point of exclusion:

- **Wildcard imports** (`import java.awt.*; import javax.swing.*;`) are allowed. Checkstyle's
  `AvoidStarImport` is simply not enabled — rewriting every import list in every UI file to
  dozens of explicit imports was judged not worth the diff size or risk for a purely stylistic
  win, and many style guides carve out exactly this exception for AWT/Swing.
- **`entities.Ball` / `entities.Player`'s public mutable fields** (`x`, `y`, `vx`, `vy`, ...) are
  intentional — the same "plain physics struct" pattern used by e.g. libGDX's `Vector2`. The
  per-frame collision math reads and writes them dozens of times a frame; wrapping every field in
  a getter/setter would hurt readability for no real safety gain within a single trusted package.
  SpotBugs' `PA_PUBLIC_PRIMITIVE_ATTRIBUTE` is filtered for the `entities` package only.
- **`EI_EXPOSE_REP`/`EI_EXPOSE_REP2`** (exposing/storing a mutable reference) is filtered for
  `GamePanel` and `MobileControlsPanel`: both just wire up their parent
  `GameFrame`/`GamePanel`/`InputManager` collaborators, which are shared-by-design, not
  defensively-copyable values.
- **`NullAssignment`** (PMD) is disabled — assigning `null` to reset a reference field inside a
  `reset()`-style method (see `Ball.reset()`) is the idiomatic pattern here, not a smell.
- Swing components that extend `JPanel`/`JFrame` but are never actually serialized still declare
  `serialVersionUID` (silences the warning honestly) and mark non-serializable collaborator
  fields `transient` (accurate, not just quiet).
- **Formatting** is Spotless's job (`google-java-format`), not Checkstyle's — Checkstyle carries
  only hygiene/naming/documentation checks, avoiding two tools fighting over the same concern.

## Consequences

- `./gradlew check` is green using rules that reflect real intent, so a future contributor can
  trust a red gate means something, instead of learning to ignore known-noisy failures.
- Adopting Spotless meant one large, whitespace-dominated reformatting diff across the whole
  codebase (2-space `google-java-format` style replaces the previous hand-aligned 4-space style).
  That diff landed together with the Gradle migration commit rather than as an isolated
  "reformat only" commit, so `git blame` on that commit mixes formatting and substance for the
  files touched in the same pass.
- Every exception above is a live decision, not a permanent one: if `entities.Ball`/`Player` ever
  grow real invariants worth protecting (e.g. once an AI or netcode consumer needs guarantees
  about valid ranges), revisit the `PA_PUBLIC_PRIMITIVE_ATTRIBUTE` filter rather than assuming it
  still applies.
