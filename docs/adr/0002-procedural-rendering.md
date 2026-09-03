# 0002 — Procedural (drawn) player/ball art instead of sprite images

**Status:** Accepted

## Context

`Player`, `Ball`, `Goal` and `Stadium` all draw themselves with `Graphics2D` primitives
(`fillOval`, `fillRect`, `fillPolygon`, ...) rather than loading and blitting image sprites. There
are no image assets anywhere in the project.

## Decision

Keep procedural rendering rather than introducing a sprite pipeline.

## Consequences

- No asset pipeline, no license/attribution concerns for art, no loading step, trivially
  resizable/recolorable (team colors are just parameters to the same draw code — see
  `entities/Team.java`).
- Visual richness is capped by what's practical to express as shape math; the animations
  (walk cycle, hair lag, kick pose) are all small trigonometric offsets applied to the same
  shapes rather than distinct animation frames.
- If the project ever wants hand-drawn art, ROADMAP.md lists it as an optional v3.0 item
  ("sprite-based art pipeline") specifically *alongside*, not *replacing*, this renderer — the
  procedural style is a deliberate look, not a placeholder.
