package entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

class GoalTest {

  @Test
  void leftGoalFrontPostIsRightOfBackPost() {
    Goal goal = new Goal(true);
    assertTrue(goal.isLeft);
    assertTrue(goal.frontX > goal.backX, "left goal's field-facing post must be to the right");
  }

  @Test
  void rightGoalFrontPostIsLeftOfBackPost() {
    Goal goal = new Goal(false);
    assertFalse(goal.isLeft);
    assertTrue(goal.frontX < goal.backX, "right goal's field-facing post must be to the left");
  }

  @Test
  void crossbarBoundsSpanBothPosts() {
    Goal left = new Goal(true);
    Rectangle bounds = left.getCrossbarBounds();
    assertEquals(Math.min(left.frontX, left.backX), bounds.x);
    assertEquals(Math.abs(left.frontX - left.backX), bounds.width);
    assertEquals(left.topY, bounds.y);
  }

  @Test
  void shakeLifecycleDoesNotThrow() {
    Goal goal = new Goal(true);
    goal.startShake();
    assertDoesNotThrow(
        () -> {
          for (int i = 0; i < 20; i++) {
            goal.update();
          }
        });
    assertDoesNotThrow(goal::reset);
  }
}
