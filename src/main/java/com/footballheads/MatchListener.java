package com.footballheads;

/**
 * Beobachter für Tor- und Spielende-Ereignisse eines {@link GamePanel} (Observer-Pattern).
 *
 * <p>Entkoppelt die Spiellogik davon, was auf diese Ereignisse reagiert. Aktuell registriert {@link
 * GamePanel} selbst nur einen Logging-Listener; künftige Abonnenten könnten z. B. Soundeffekte
 * auslösen oder Eingaben für einen Replay-Modus aufzeichnen (siehe ROADMAP.md).
 */
public interface MatchListener {

  /**
   * Wird aufgerufen, sobald ein Tor fällt.
   *
   * @param scoringTeamName Name des Teams, dem das Tor gutgeschrieben wird
   * @param ownGoal {@code true}, wenn es sich um ein Eigentor handelt
   * @param goldenGoal {@code true}, wenn das Tor in der Golden-Goal-Verlängerung fiel
   */
  default void onGoal(String scoringTeamName, boolean ownGoal, boolean goldenGoal) {}

  /**
   * Wird aufgerufen, sobald das Spiel endet.
   *
   * @param team1Name Name des ersten Teams
   * @param score1 Tore des ersten Teams
   * @param team2Name Name des zweiten Teams
   * @param score2 Tore des zweiten Teams
   */
  default void onMatchEnded(String team1Name, int score1, String team2Name, int score2) {}
}
