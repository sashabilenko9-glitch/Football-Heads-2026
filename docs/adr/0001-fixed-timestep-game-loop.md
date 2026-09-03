# 0001 — Fixed-timestep game loop

**Status:** Accepted

## Context

The game runs its physics (gravity, collisions, movement) on a `javax.swing.Timer` firing every
16 ms. Swing timers are not real-time: the EDT can be delayed by GC pauses, other repaints, or a
slow machine, so the actual gap between ticks varies. If physics simply advanced "one step" per
timer tick, gameplay speed (and therefore fairness between two human players) would depend on how
fast the machine happens to be running at that moment.

## Decision

`GamePanel` measures real elapsed time between ticks and accumulates it (`accumulatorNs`). Physics
advances in fixed `STEP_NS` (1/60s) increments, replayed as many times as the accumulator allows
each tick (`while (accumulatorNs >= STEP_NS) stepGame();`). A single frame's elapsed time is capped
at `MAX_FRAME_NS` so a long stall (e.g. the window being dragged) doesn't cause dozens of physics
steps to fire at once ("spiral of death").

The Swing `Timer` itself is only a wake-up clock; it does not determine simulation speed.

## Consequences

- Gameplay speed is identical regardless of frame rate or timer jitter — a match takes the same
  60 seconds and the ball moves at the same speed whether the machine renders at 200 fps or 40 fps.
- Rendering (`paintComponent`) is not interpolated between physics steps; on a very fast machine
  the same physics state may be drawn more than once per accumulator step. Not visually
  noticeable at the game's speeds, but worth knowing before chasing a "smoother rendering"
  feature request.
- This determinism is also the prerequisite for two roadmap items: match replays (record inputs,
  replay the same simulation) and lockstep netcode (only inputs need to travel over the network).
