package entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Exercises the procedural rendering code against an off-screen {@link BufferedImage}.
 *
 * <p>This needs no display/window (safe in headless CI) and is a real smoke test, not just a
 * coverage trick: a {@code NullPointerException} or {@code ArrayIndexOutOfBoundsException} inside
 * one of these draw methods (e.g. a malformed polygon, a font metrics call on a null font) would
 * fail here exactly as it would on screen.
 */
class RenderingSmokeTest {

  private static Graphics2D newOffscreenGraphics() {
    BufferedImage image = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_ARGB);
    return image.createGraphics();
  }

  @Test
  void ballDrawsWithoutThrowing() {
    Graphics2D g = newOffscreenGraphics();
    Ball ball = new Ball();
    assertDoesNotThrow(
        () -> {
          ball.drawShadow(g);
          ball.draw(g);
        });
    g.dispose();
  }

  @Test
  void playerDrawsForBothFacingDirectionsWithoutThrowing() {
    Graphics2D g = newOffscreenGraphics();
    Ball ball = new Ball();
    Player facingRight = new Player(280, true, null);
    Player facingLeft = new Player(720, false, null);
    assertDoesNotThrow(
        () -> {
          facingRight.draw(g, ball);
          facingLeft.draw(g, ball);
          facingRight.draw(g, null);
        });
    g.dispose();
  }

  @Test
  void goalDrawsBothLayersInBothShakeStatesWithoutThrowing() {
    Graphics2D g = newOffscreenGraphics();
    Goal goal = new Goal(true);
    assertDoesNotThrow(
        () -> {
          goal.drawBack(g);
          goal.drawFront(g);
          goal.startShake();
          goal.update();
          goal.drawBack(g);
          goal.drawFront(g);
        });
    g.dispose();
  }

  @Test
  void stadiumDrawsBackgroundInCelebrationAndIdleStateWithoutThrowing() {
    Graphics2D g = newOffscreenGraphics();
    Stadium stadium = new Stadium();
    assertDoesNotThrow(
        () -> {
          stadium.drawBackground(g, false);
          stadium.drawBackground(g, true);
          stadium.drawBackgroundOnly(g);
        });
    g.dispose();
  }
}
