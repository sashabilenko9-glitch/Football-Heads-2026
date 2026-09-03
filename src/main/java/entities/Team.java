package entities;

import java.awt.Color;

/** Datencontainer für ein spielbares Team. Speichert Namen, Trikotfarben und Hautfarbe. */
public class Team {

  /** Anzeigename des Teams. */
  public String name;

  /** Primärfarbe des Trikots. */
  public Color primaryColor;

  /** Sekundärfarbe (Hose, Streifen). */
  public Color secondaryColor;

  /** Hautfarbe der Spielerfigur. */
  public Color skinColor;

  /** Standard-Hautton, verwendet, wenn kein eigener Hautton angegeben wird. */
  private static final Color SKIN_DEFAULT = new Color(255, 200, 160);

  /** Heller Hautton. */
  public static final Color SKIN_LIGHT = new Color(255, 200, 160);

  /** Mittlerer Hautton. */
  public static final Color SKIN_MEDIUM = new Color(198, 134, 74);

  /** Dunkler Hautton. */
  public static final Color SKIN_DARK = new Color(107, 67, 38);

  /**
   * Erstellt ein Team mit den angegebenen Farben und dem Standard-Hautton.
   *
   * @param name Anzeigename
   * @param primary Primärfarbe des Trikots
   * @param secondary Sekundärfarbe (Hose, Streifen)
   */
  public Team(String name, Color primary, Color secondary) {
    this(name, primary, secondary, SKIN_DEFAULT);
  }

  /**
   * Erstellt ein Team mit den angegebenen Farben und Hautton.
   *
   * @param name Anzeigename
   * @param primary Primärfarbe des Trikots
   * @param secondary Sekundärfarbe (Hose, Streifen)
   * @param skin Hautfarbe der Spielerfigur
   */
  public Team(String name, Color primary, Color secondary, Color skin) {
    this.name = name;
    this.primaryColor = primary;
    this.secondaryColor = secondary;
    this.skinColor = skin;
  }

  /**
   * Gibt alle verfügbaren Teams zurück.
   *
   * <p>Die Trikotfarben orientieren sich an bekannten Nationalmannschafts- bzw. Vereinstrikots;
   * jedes Team ist nach einem bekannten Spieler benannt, dessen Trikotfarben zur jeweiligen
   * Farbkombination passen.
   *
   * @return Array aller vordefinierten Teams
   */
  public static Team[] getAllTeams() {
    return new Team[] {
      new Team(
          "Tim Cahill",
          new Color(240, 240, 240),
          new Color(218, 165, 32),
          SKIN_MEDIUM), // Australien (Auswärtstrikot)
      new Team(
          "Jack Grealish", new Color(165, 0, 68), new Color(0, 77, 152), SKIN_LIGHT), // Aston Villa
      new Team(
          "Robert Lewandowski",
          new Color(220, 5, 45),
          new Color(255, 255, 255),
          SKIN_LIGHT), // Polen
      new Team(
          "Kylian Mbappé",
          new Color(3, 70, 148),
          new Color(255, 255, 255),
          SKIN_DARK), // Frankreich
      new Team(
          "Alessandro Del Piero",
          new Color(0, 0, 0),
          new Color(255, 255, 255),
          SKIN_LIGHT), // Juventus
      new Team(
          "Erling Haaland",
          new Color(253, 225, 0),
          new Color(0, 0, 0),
          SKIN_LIGHT), // Borussia Dortmund
      new Team(
          "Wilfried Zaha",
          new Color(0, 65, 113),
          new Color(218, 41, 28),
          SKIN_DARK), // Crystal Palace
      new Team("Pelé", new Color(255, 223, 0), new Color(0, 155, 58), SKIN_DARK), // Brasilien
      new Team(
          "Lionel Messi",
          new Color(116, 172, 223),
          new Color(255, 255, 255),
          SKIN_MEDIUM), // Argentinien
      new Team(
          "Paolo Maldini", new Color(0, 100, 170), new Color(255, 255, 255), SKIN_LIGHT), // Italien
    };
  }

  /**
   * @return Anzeigename des Teams (für JComboBox-Darstellung).
   */
  @Override
  public String toString() {
    return name;
  }
}
