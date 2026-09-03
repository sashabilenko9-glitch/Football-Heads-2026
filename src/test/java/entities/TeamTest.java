package entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TeamTest {

  @Test
  void getAllTeamsReturnsTenDistinctTeams() {
    Team[] teams = Team.getAllTeams();
    assertEquals(10, teams.length);

    Set<String> names = new HashSet<>();
    for (Team team : teams) {
      assertNotNull(team.name);
      assertTrue(!team.name.isBlank(), "team name must not be blank");
      assertNotNull(team.primaryColor);
      assertNotNull(team.secondaryColor);
      assertNotNull(team.skinColor);
      names.add(team.name);
    }
    assertEquals(10, names.size(), "all team names must be unique so players can tell them apart");
  }

  @Test
  void threeArgConstructorUsesDefaultLightSkin() {
    Team team = new Team("Test", Color.RED, Color.BLUE);
    assertEquals(Team.SKIN_LIGHT, team.skinColor);
  }

  @Test
  void fourArgConstructorHonorsGivenSkinTone() {
    Team team = new Team("Test", Color.RED, Color.BLUE, Team.SKIN_DARK);
    assertEquals(Team.SKIN_DARK, team.skinColor);
  }

  @Test
  void toStringReturnsName() {
    Team team = new Team("Rocket Rioters", Color.GREEN, Color.WHITE);
    assertEquals("Rocket Rioters", team.toString());
  }
}
