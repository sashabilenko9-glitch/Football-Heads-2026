# Architecture Decision Records

Short records of the non-obvious decisions in this codebase and why they were made — not
documentation of what the code does (the code and its Javadoc already say that), but of the
reasoning a future reader can't recover just by reading the diff.

| # | Decision |
|---|---|
| [0001](0001-fixed-timestep-game-loop.md) | Fixed-timestep game loop over a naive per-frame update |
| [0002](0002-procedural-rendering.md) | Procedural (drawn) player/ball art instead of sprite images |
| [0003](0003-letterboxed-fixed-resolution.md) | Fixed 1000×700 logical resolution with letterboxing |
| [0004](0004-gradle-migration.md) | Migrate the build from a raw Eclipse project to Gradle |
| [0005](0005-quality-gate-tooling.md) | Checkstyle/PMD/SpotBugs/Spotless scope and exceptions |
| [0006](0006-coverage-scope.md) | JaCoCo coverage gate scoped to entities/utils only |
| [0007](0007-state-and-observer-patterns.md) | State enum and Observer listener in GamePanel |
| [0008](0008-i18n-and-deferred-strategy-pattern.md) | ResourceBundle i18n now, Strategy pattern for AI deferred |

New ADRs go at the next number, in this same format: Status, Context, Decision, Consequences.
Never renumber or delete a superseded one — add a new one and mark the old `Status: Superseded by
000X`.
