package entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import utils.Field;

class PlayerTest {

  @Test
  void nullTeamFallsBackToDefaultTeam() {
    Player player = new Player(280, true, null);
    assertEquals("Default", player.team.name);
  }

  @Test
  void resetReturnsPlayerToStartPosition() {
    Player player = new Player(280, true, null);
    player.x = 999;
    player.y = 5;
    player.vx = 7;
    player.vy = -7;

    player.reset();

    assertEquals(280, player.x);
    assertEquals(Field.GROUND_Y, player.y);
    assertEquals(0, player.vx);
    assertEquals(0, player.vy);
  }

  @Test
  void movingLeftAcceleratesTowardsNegativeVelocity() {
    Player player = new Player(500, true, null);
    Player opponent = new Player(700, false, null);
    Ball ball = new Ball();
    Stadium stadium = new Stadium();

    player.left = true;
    for (int i = 0; i < 30; i++) {
      player.update(ball, stadium, opponent);
    }

    assertTrue(player.vx < 0, "player should be moving left");
    assertTrue(player.vx >= -8.6, "horizontal speed must respect the movement cap");
  }

  @Test
  void jumpingFromGroundGivesUpwardVelocity() {
    Player player = new Player(500, true, null);
    Player opponent = new Player(700, false, null);
    Ball ball = new Ball();
    Stadium stadium = new Stadium();

    assertEquals(Field.GROUND_Y, player.y, "player must start on the ground to be able to jump");
    player.jumping = true;
    player.update(ball, stadium, opponent);

    assertTrue(player.vy < 0, "jumping must launch the player upward (negative vy)");
  }
}
