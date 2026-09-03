# Contributing

This is a personal/portfolio project (see [README.md](README.md#license) for the license terms),
but the workflow below is the one used to develop it and is worth following for consistency if
you're working on a fork or a PR against it.

## Dev workflow

```bash
./gradlew run            # play the game
./gradlew check          # tests + coverage gate + Checkstyle + PMD + SpotBugs + format check
./gradlew spotlessApply  # auto-fix formatting before committing
```

Enable the repo's pre-commit hook once per clone (blocks a commit that isn't Spotless-formatted):

```bash
git config core.hooksPath .githooks
```

`./gradlew check` is the same thing CI runs — get it green locally before opening a PR; see
[.github/workflows/ci.yml](.github/workflows/ci.yml) and
[docs/adr/0005-quality-gate-tooling.md](docs/adr/0005-quality-gate-tooling.md) for what each tool
checks and why the rulesets are scoped the way they are.

- Use a feature branch and a PR even solo — it keeps history readable and gives CI a chance to run
  before something lands on `main`.
- Add or update a test for any change to `entities/*` or `utils/*` (the packages the coverage gate
  actually watches — see [docs/adr/0006](docs/adr/0006-coverage-scope.md)). UI-only changes don't
  need a test but should be run once with `./gradlew run` before committing.
- Record a new [ADR](docs/adr/) for any decision a future reader couldn't reconstruct just by
  reading the diff (why a library, why an exception was carved out of a linter, why an approach
  was rejected). Use the next free number and the same Status/Context/Decision/Consequences shape
  as the existing ones.
- Update [CHANGELOG.md](CHANGELOG.md) under `## [Unreleased]` for anything user-visible; update
  [ROADMAP.md](ROADMAP.md) checkboxes as items land.

## Commit messages

Commits follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<optional scope>): <short summary>

<optional body>
```

Types used in this repo:

| Type | For |
|---|---|
| `feat` | a user-visible capability (new game mode, new team, new setting) |
| `fix` | a bug fix |
| `refactor` | internal restructuring with no behavior change |
| `chore` | build/tooling/config changes (Gradle, CI, linters) |
| `docs` | README/CHANGELOG/ROADMAP/ADR-only changes |
| `test` | test-only changes |

Examples:

```
feat(team-select): add tournament mode
fix(ball): stop crossbar re-triggering shake every frame while resting on it
chore(ci): cache the Gradle wrapper between workflow runs
docs(adr): record why AI strategy pattern is deferred
```

This format is what a `release-please`-style tool would need to auto-draft `CHANGELOG.md` entries
from commit history in the future (see ROADMAP.md's "Conventional Commits + auto-generated
changelog" item) — consistent typing now is what makes that automation possible later.

## Reporting issues / picking up work

Open issues against the milestones in [ROADMAP.md](ROADMAP.md) (v1.1 engineering foundations, v1.2
polish, v2.0 AI, v2.1 modes, v3.0 stretch). `scripts/create-github-issues.sh` can bulk-create a
GitHub issue per unchecked ROADMAP checklist item, grouped into matching milestones, once the repo
has a GitHub remote and the `gh` CLI is authenticated.
