# 0003 — Fixed 1000×700 logical resolution with letterboxing

**Status:** Accepted

## Context

The game window is resizable and typically maximized (`JFrame.MAXIMIZED_BOTH`), so its pixel size
varies with the player's monitor. All gameplay math (goal positions, player physics, collision
bounds) is written against fixed numbers like `500`, `580`, `1000`.

## Decision

`GamePanel` always simulates and lays out the scene in a fixed 1000×700 logical coordinate space.
In `paintComponent`, it computes a single uniform `scale = min(width/1000, height/700)`, centers
the result with `offsetX`/`offsetY`, and clips to the logical bounds — leaving black bars on
whichever axis has leftover space, rather than stretching.

Mouse input (`mouseClicked`) reverses the same transform (`(e.getX() - offsetX) / scale`) to map
screen coordinates back to logical ones before hit-testing the end-screen buttons.

## Consequences

- The ball and player heads stay perfectly circular at any window size/aspect ratio — a
  non-uniform (stretch-to-fill) scale would ellipse them.
- Gameplay code never has to be aspect-ratio-aware; every constant in `entities/*` and
  `GamePanel`'s drawing methods is in the same fixed 1000×700 space.
- Very wide or very tall windows waste screen space as letterbox bars instead of using it — an
  intentional trade for keeping physics and hit-testing simple. An alternative (extend the
  playable area instead of letterboxing) was not pursued, since it would make goal distance and
  field size vary with the player's window, changing the balance of the game itself.
