# 0004 — Migrate the build from a raw Eclipse project to Gradle

**Status:** Accepted

## Context

Through v1.0.0 the project was a bare Eclipse Java project: `.classpath`/`.project`/`.settings`,
compiled by Eclipse's builder into `bin/`, no dependency manager, no way to run linters, tests, or
coverage from the command line or CI. Publishing to GitHub as a portfolio piece and wiring up
CI/quality gates (ROADMAP.md "engineering foundations") both need a real build tool.

## Decision

Adopt Gradle (Groovy DSL) with the standard `src/main/java` / `src/test/java` layout, the
`application` plugin for `./gradlew run`, and the Gradle Wrapper committed so CI and other
machines need only a JDK — not a pre-installed Gradle. Not Maven: Gradle's plugin ecosystem for
Checkstyle/PMD/SpotBugs/Spotless/JaCoCo is equally mature, and a Gradle install happened to already
be available in the environment this migration was done in, making it possible to actually run and
verify every quality-gate change locally rather than writing XML blind.

## Consequences

- `gradlew.bat` / `gradlew` are the only required tooling; `./gradlew build` produces a runnable
  jar (`build/libs/football-heads-2026-<version>.jar`).
- The raw `.classpath`/`.project`/`.settings` files were deleted — Eclipse's Buildship plugin
  reads `build.gradle` directly (`File → Import → Gradle → Existing Gradle Project`) and generates
  its own project files locally, which are `.gitignore`d rather than committed.
- Every subsequent quality-gate ADR (0005, 0006) builds on this: none of Checkstyle, PMD,
  SpotBugs, Spotless or JaCoCo could be wired into a one-command `./gradlew check` (or into CI)
  without a build tool actually managing them.
