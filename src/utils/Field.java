package utils;

/**
 * Globale Spielfeld-Konstanten: Grenzen, Physik und Bodenhöhe.
 */
public class Field {

    /**
     * Privater Konstruktor – reine Konstantenklasse, wird nicht instanziiert.
     */
    private Field() {
    }

    /** Linker Bildschirmrand in Spielkoordinaten. */
    public static final int LEFT_WALL = 0;

    /** Rechter Bildschirmrand in Spielkoordinaten. */
    public static final int RIGHT_WALL = 1000;

    /** Oberer Bildschirmrand in Spielkoordinaten. */
    public static final int CEILING = 0;

    /** Schwerkraftbeschleunigung pro Frame (Pixel/Frame²). */
    public static final double GRAVITY = 0.5;

    /** Y-Koordinate des Spielfeldbodens. */
    public static final int GROUND_Y = 580;

    /** Linke Bewegungsgrenze für Spieler. */
    public static final int PLAYER_LEFT_LIMIT = 40;

    /** Rechte Bewegungsgrenze für Spieler. */
    public static final int PLAYER_RIGHT_LIMIT = 960;
}
