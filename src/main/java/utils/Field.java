package utils;

/** Globale Spielfeld-Konstanten: Grenzen, Physik und Bodenhöhe. */
public class Field {

  /** Privater Konstruktor – reine Konstantenklasse, wird nicht instanziiert. */
  private Field() {}

  /** Schwerkraftbeschleunigung pro Frame (Pixel/Frame²). */
  public static final double GRAVITY = 0.5;

  /** Y-Koordinate des Spielfeldbodens. */
  public static final int GROUND_Y = 580;
}
