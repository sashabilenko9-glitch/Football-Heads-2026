package com.footballheads;

/**
 * Zentraler Eingabe-Manager.
 *
 * <p>Sammelt die Eingabezustände von Tastatur und mobiler Overlay-Steuerung getrennt und führt sie
 * per ODER-Verknüpfung zu virtuellen Eingaben zusammen, die im {@link GamePanel} an die
 * Spieler-Update-Methoden übergeben werden. Dadurch können beide Eingabequellen gleichzeitig und
 * konfliktfrei genutzt werden.
 */
public class InputManager {

  /** Erstellt einen Eingabe-Manager mit allen Eingabezuständen auf {@code false}. */
  public InputManager() {}

  // --- Spieler 1: Eingaben von der Tastatur ---
  private boolean p1MoveLeftKeyboard = false;
  private boolean p1MoveRightKeyboard = false;
  private boolean p1JumpKeyboard = false;
  private boolean p1KickKeyboard = false;

  // --- Spieler 1: Eingaben von der mobilen Steuerung ---
  private boolean p1MoveLeftMobile = false;
  private boolean p1MoveRightMobile = false;
  private boolean p1JumpMobile = false;
  private boolean p1KickMobile = false;

  // --- Spieler 2: Eingaben von der Tastatur ---
  private boolean p2MoveLeftKeyboard = false;
  private boolean p2MoveRightKeyboard = false;
  private boolean p2JumpKeyboard = false;
  private boolean p2KickKeyboard = false;

  // --- Spieler 2: Eingaben von der mobilen Steuerung ---
  private boolean p2MoveLeftMobile = false;
  private boolean p2MoveRightMobile = false;
  private boolean p2JumpMobile = false;
  private boolean p2KickMobile = false;

  // ------------------------------------------------------------------
  // Zusammengeführte (virtuelle) Eingabezustände – Spieler 1
  // ------------------------------------------------------------------

  /**
   * Fragt ab, ob Spieler 1 nach links laufen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 1 nach links laufen soll (Tastatur ODER mobil)
   */
  public boolean getP1Left() {
    return p1MoveLeftKeyboard || p1MoveLeftMobile;
  }

  /**
   * Fragt ab, ob Spieler 1 nach rechts laufen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 1 nach rechts laufen soll (Tastatur ODER mobil)
   */
  public boolean getP1Right() {
    return p1MoveRightKeyboard || p1MoveRightMobile;
  }

  /**
   * Fragt ab, ob Spieler 1 springen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 1 springen soll (Tastatur ODER mobil)
   */
  public boolean getP1Jump() {
    return p1JumpKeyboard || p1JumpMobile;
  }

  /**
   * Fragt ab, ob Spieler 1 schießen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 1 schießen soll (Tastatur ODER mobil)
   */
  public boolean getP1Kick() {
    return p1KickKeyboard || p1KickMobile;
  }

  // ------------------------------------------------------------------
  // Zusammengeführte (virtuelle) Eingabezustände – Spieler 2
  // ------------------------------------------------------------------

  /**
   * Fragt ab, ob Spieler 2 nach links laufen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 2 nach links laufen soll (Tastatur ODER mobil)
   */
  public boolean getP2Left() {
    return p2MoveLeftKeyboard || p2MoveLeftMobile;
  }

  /**
   * Fragt ab, ob Spieler 2 nach rechts laufen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 2 nach rechts laufen soll (Tastatur ODER mobil)
   */
  public boolean getP2Right() {
    return p2MoveRightKeyboard || p2MoveRightMobile;
  }

  /**
   * Fragt ab, ob Spieler 2 springen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 2 springen soll (Tastatur ODER mobil)
   */
  public boolean getP2Jump() {
    return p2JumpKeyboard || p2JumpMobile;
  }

  /**
   * Fragt ab, ob Spieler 2 schießen soll (Tastatur ODER mobil).
   *
   * @return {@code true}, wenn Spieler 2 schießen soll (Tastatur ODER mobil)
   */
  public boolean getP2Kick() {
    return p2KickKeyboard || p2KickMobile;
  }

  // ------------------------------------------------------------------
  // Setter: Tastatur (Spieler 1)
  // ------------------------------------------------------------------

  /**
   * Setzt den Tastatur-Zustand „links“ für Spieler 1.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP1LeftFromKeyboard(boolean on) {
    p1MoveLeftKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „rechts“ für Spieler 1.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP1RightFromKeyboard(boolean on) {
    p1MoveRightKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „springen“ für Spieler 1.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP1JumpFromKeyboard(boolean on) {
    p1JumpKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „schießen“ für Spieler 1.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP1KickFromKeyboard(boolean on) {
    p1KickKeyboard = on;
  }

  // ------------------------------------------------------------------
  // Setter: Tastatur (Spieler 2)
  // ------------------------------------------------------------------

  /**
   * Setzt den Tastatur-Zustand „links“ für Spieler 2.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP2LeftFromKeyboard(boolean on) {
    p2MoveLeftKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „rechts“ für Spieler 2.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP2RightFromKeyboard(boolean on) {
    p2MoveRightKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „springen“ für Spieler 2.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP2JumpFromKeyboard(boolean on) {
    p2JumpKeyboard = on;
  }

  /**
   * Setzt den Tastatur-Zustand „schießen“ für Spieler 2.
   *
   * @param on {@code true}, solange die Taste gedrückt ist
   */
  public void setP2KickFromKeyboard(boolean on) {
    p2KickKeyboard = on;
  }

  // ------------------------------------------------------------------
  // Setter: mobile Steuerung (Spieler 1)
  // ------------------------------------------------------------------

  /**
   * Setzt den Mobil-Zustand „links“ für Spieler 1.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP1MoveLeftFromMobile(boolean on) {
    p1MoveLeftMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „rechts“ für Spieler 1.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP1MoveRightFromMobile(boolean on) {
    p1MoveRightMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „springen“ für Spieler 1.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP1JumpFromMobile(boolean on) {
    p1JumpMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „schießen“ für Spieler 1.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP1KickFromMobile(boolean on) {
    p1KickMobile = on;
  }

  // ------------------------------------------------------------------
  // Setter: mobile Steuerung (Spieler 2)
  // ------------------------------------------------------------------

  /**
   * Setzt den Mobil-Zustand „links“ für Spieler 2.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP2MoveLeftFromMobile(boolean on) {
    p2MoveLeftMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „rechts“ für Spieler 2.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP2MoveRightFromMobile(boolean on) {
    p2MoveRightMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „springen“ für Spieler 2.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP2JumpFromMobile(boolean on) {
    p2JumpMobile = on;
  }

  /**
   * Setzt den Mobil-Zustand „schießen“ für Spieler 2.
   *
   * @param on {@code true}, solange die Schaltfläche gehalten wird
   */
  public void setP2KickFromMobile(boolean on) {
    p2KickMobile = on;
  }
}
