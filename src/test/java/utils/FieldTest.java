package utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FieldTest {

  @Test
  void gravityIsPositiveAndSmall() {
    assertTrue(Field.GRAVITY > 0, "gravity must pull the ball/players downward");
    assertTrue(Field.GRAVITY < 5, "gravity should be a small per-frame acceleration");
  }

  @Test
  void groundIsBelowScreenTop() {
    assertTrue(Field.GROUND_Y > 0);
  }
}
