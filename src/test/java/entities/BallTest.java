package entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BallTest {

  @Test
  void resetRestoresKickoffState() {
    Ball ball = new Ball();
    ball.x = 12;
    ball.y = 34;
    ball.vx = 5;
    ball.vy = 5;
    ball.spinVelocity = 90;
    ball.lastTouchedPlayer = new Player(0, true, null);

    ball.reset();

    assertEquals(500, ball.x);
    assertEquals(200, ball.y);
    assertEquals(0, ball.vx);
    assertEquals(-5, ball.vy);
    assertEquals(0, ball.spinVelocity);
    assertNull(ball.lastTouchedPlayer);
  }

  @Test
  void speedIsClampedAfterExtremeVelocity() {
    Ball ball = new Ball();
    Stadium stadium = new Stadium();
    Player p1 = new Player(280, true, null);
    Player p2 = new Player(720, false, null);

    ball.vx = 1000;
    ball.vy = 1000;
    ball.update(p1, p2, stadium);

    double speed = Math.hypot(ball.vx, ball.vy);
    assertTrue(speed <= 30.5, "ball speed must stay near the physics engine's speed cap");
  }

  @Test
  void ballBouncesOffLeftWallInsteadOfPassingThrough() {
    Ball ball = new Ball();
    Stadium stadium = new Stadium();
    Player p1 = new Player(280, true, null);
    Player p2 = new Player(720, false, null);

    ball.x = stadium.leftGoal.backX - 5;
    ball.y = 100;
    ball.vx = -5;
    ball.vy = 0;

    ball.update(p1, p2, stadium);

    assertTrue(ball.x >= stadium.leftGoal.backX, "ball must be pushed back out of the wall");
    assertTrue(ball.vx > 0, "horizontal velocity must reverse on wall impact");
  }
}
