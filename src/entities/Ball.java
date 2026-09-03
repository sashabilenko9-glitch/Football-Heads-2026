package entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import utils.Field;

/**
 * Repräsentiert den Spielball und berechnet seine Physik.
 * <p>
 * Physikalisches Modell pro Frame:
 * <ol>
 *   <li>Schwerkraft und Luftreibung auf den Geschwindigkeitsvektor anwenden</li>
 *   <li>Geschwindigkeit auf {@code MAX_SPEED} begrenzen</li>
 *   <li>Position aktualisieren</li>
 *   <li>Wand-, Latten- und Spielerkollisionen auflösen</li>
 * </ol>
 * Die Spieler-Kollision verwendet zwei Kreise (Kopf und Körper/Beine),
 * damit der Ball nicht durch die Beine hindurchfliegt.
 */
public class Ball {

    /** Aktuelle X-Position in Spielkoordinaten. */
    public double x = 500;

    /** Aktuelle Y-Position in Spielkoordinaten. */
    public double y = 200;

    /** Horizontale Geschwindigkeitskomponente (Pixel/Frame). */
    public double vx = 0;

    /** Vertikale Geschwindigkeitskomponente (Pixel/Frame). */
    public double vy = 0;

    /** Drehgeschwindigkeit (Grad/Frame). */
    public double spinVelocity = 0.0;

    /**
     * Letzter Spieler, der den Ball berührt hat.
     * Wird für die Eigentor-Erkennung ausgewertet.
     */
    public Player lastTouchedPlayer = null;

    private double spinAngle = 0.0;

    private static final double FRICTION         = 0.970;
    private static final double MAX_SPEED        = 30.0;
    private static final int    SIZE             = 40;

    /** Radius des Kopf-Kollisionskreises des Spielers. */
    private static final double HEAD_RADIUS = 38.0;

    /** Radius des Körper-/Bein-Kollisionskreises des Spielers. */
    private static final double BODY_RADIUS = 34.0;

    /** Vertikaler Versatz des Kopf-Mittelpunkts relativ zum Fußpunkt des Spielers. */
    private static final double HEAD_OFFSET_Y = 112.0;

    /** Vertikaler Versatz des Körper-Mittelpunkts relativ zum Fußpunkt des Spielers. */
    private static final double BODY_OFFSET_Y = 45.0;

    /** Unterdrückt die Spieler-Kollision für den Frame, in dem geschossen wurde. */
    public boolean justKicked = false;

    private final int r = SIZE / 2;

    /**
     * Erstellt den Ball an der Startposition (Spielfeldmitte, leicht erhöht).
     */
    public Ball() {
    }

    /**
     * Aktualisiert Physik und Kollisionen für einen Frame.
     *
     * @param p1      erster Spieler
     * @param p2      zweiter Spieler
     * @param stadium Stadion mit Tor-Referenzen
     */
    public void update(Player p1, Player p2, Stadium stadium) {
        vy += utils.Field.GRAVITY;
        vx *= FRICTION;
        clampSpeed();

        x += vx;
        y += vy;

        spinAngle    += spinVelocity;
        spinVelocity *= 0.97;

        checkWallCollisions(stadium);
        checkCrossbar(stadium);
        checkPlayerCollision(p1);
        checkPlayerCollision(p2);

        justKicked = false;
    }

    /**
     * Setzt den Ball in die Startposition zurück.
     */
    public void reset() {
        x  = 500; y  = 200;
        vx = 0;   vy = -5;
        spinVelocity      = 0;
        lastTouchedPlayer = null;
    }

    /**
     * Zeichnet den Bodenschatten des Balls.
     * Muss <em>vor</em> den Spielern aufgerufen werden, damit der Schatten unter ihnen liegt.
     *
     * @param g Grafik-Kontext
     */
    public void drawShadow(Graphics2D g) {
        int    ix     = (int) x;
        double height = Math.max(0, Field.GROUND_Y - y);
        int    sw     = (int) Math.max(10, 40 - height * 0.035);
        int    sh     = (int) Math.max(3,  13 - height * 0.012);
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(ix - sw / 2, Field.GROUND_Y - sh, sw, sh);
    }

    /**
     * Zeichnet den Ball mit Rotation.
     * Der Schatten wird separat über {@link #drawShadow} gerendert.
     *
     * @param g Grafik-Kontext
     */
    public void draw(Graphics2D g) {
        int ix = (int) x;
        int iy = (int) y;

        AffineTransform old = g.getTransform();
        g.translate(ix, iy);
        g.rotate(Math.toRadians(spinAngle));

        g.setColor(Color.WHITE);
        g.fillOval(-r, -r, SIZE, SIZE);

        g.setColor(Color.BLACK);
        g.fillPolygon(new int[]{-5, 5, 10, 0, -10}, new int[]{-10, -10, 0, 10, 0}, 5);

        g.setStroke(new BasicStroke(2));
        g.drawOval(-r, -r, SIZE, SIZE);

        g.setTransform(old);
    }

    /**
     * Begrenzt die Gesamtgeschwindigkeit auf {@code MAX_SPEED}.
     * Verhindert exponentielles Anwachsen bei Mehrfachkollisionen (Einklemm-Situationen).
     */
    private void clampSpeed() {
        double speed = Math.hypot(vx, vy);
        if (speed > MAX_SPEED) {
            double scale = MAX_SPEED / speed;
            vx *= scale;
            vy *= scale;
        }
    }

    /**
     * Berechnet die Kollision zwischen Ball und Spieler.
     * Geprüft werden zwei Kreise: zuerst der Kopf, dann Körper/Beine –
     * so kann der Ball nicht mehr durch die Beine hindurchfliegen.
     * Pro Frame wird höchstens eine der beiden Kollisionen aufgelöst.
     *
     * @param p Spieler, gegen den geprüft wird
     */
    private void checkPlayerCollision(Player p) {
        if (justKicked) return;
        if (resolveCircleCollision(p, p.x, p.y - HEAD_OFFSET_Y, HEAD_RADIUS)) return;
        resolveCircleCollision(p, p.x, p.y - BODY_OFFSET_Y, BODY_RADIUS);
    }

    /**
     * Löst eine Kreis-Kollision mit einem Körperteil des Spielers auf.
     * Verwendet distanzbasierte Überlappungskorrektur, um den Tunneling-Effekt zu verhindern.
     *
     * @param p     Spieler (für Geschwindigkeitsübertragung und Eigentor-Referenz)
     * @param cx    X-Mittelpunkt des Körperteil-Kreises
     * @param cy    Y-Mittelpunkt des Körperteil-Kreises
     * @param partR Radius des Körperteil-Kreises
     * @return {@code true}, wenn eine Kollision aufgelöst wurde
     */
    private boolean resolveCircleCollision(Player p, double cx, double cy, double partR) {
        double dx      = x - cx;
        double dy      = y - cy;
        double dist    = Math.hypot(dx, dy);
        double minDist = partR + r;

        if (dist >= minDist || dist == 0) return false;

        double angle   = Math.atan2(dy, dx);
        double cos     = Math.cos(angle);
        double sin     = Math.sin(angle);
        double overlap = minDist - dist;

        x += cos * (overlap + 0.5);
        y += sin * (overlap + 0.5);

        double bounceForce = (y < p.y - 120) ? 20.0 : 14.0;

        double oldVx = vx;
        vx += (cos * bounceForce) + p.vx * 0.6;
        vy += (sin * bounceForce) + p.vy * 0.6 - 2;

        spinVelocity      += (p.vx - oldVx) * 0.1;
        lastTouchedPlayer  = p;

        clampSpeed();
        return true;
    }

    /**
     * Prüft Kollisionen mit Wänden, Boden und Decke und löst sie auf.
     * Bei Bodenkontakt wird die Drehgeschwindigkeit aus {@code vx} abgeleitet,
     * damit der Ball sichtbar rollt statt zu gleiten.
     *
     * @param stadium Stadion mit Tor-Koordinaten (definiert die seitlichen Grenzen)
     */
    private void checkWallCollisions(Stadium stadium) {
        int limitL = stadium.leftGoal.backX  + r;
        int limitR = stadium.rightGoal.backX - r;

        if      (x < limitL) { x = limitL; vx = -vx * 0.6; }
        else if (x > limitR) { x = limitR; vx = -vx * 0.6; }

        if (y < r) {
            y  = r;
            vy = Math.abs(vy) * 0.55;
        }

        int groundY = stadium.leftGoal.bottomY;
        if (y > groundY - r) {
            y   = groundY - r;
            vy  = -vy * 0.55;
            vx *= 0.82;
            if (Math.abs(vy) < 1.0) vy = 0;

            // Rollen: Drehung an die Horizontalgeschwindigkeit koppeln
            spinVelocity = Math.toDegrees(vx / (double) r);
        }
    }

    /**
     * Prüft, ob der Ball die Torlatte berührt, und löst den Aufprall auf.
     *
     * @param stadium Stadion mit Tor-Koordinaten
     */
    private void checkCrossbar(Stadium stadium) {
        int crossY = stadium.leftGoal.topY;

        if (y > crossY - r && y < crossY + r + 15) {
            if (x > stadium.leftGoal.backX && x < stadium.leftGoal.frontX + r) {
                resolveCrossbarHit(stadium.leftGoal, crossY);
            } else if (x > stadium.rightGoal.frontX - r && x < stadium.rightGoal.backX) {
                resolveCrossbarHit(stadium.rightGoal, crossY);
            }
        }
    }

    /**
     * Löst einen Lattentreffer auf: kehrt die Vertikalgeschwindigkeit um,
     * korrigiert die Position und löst den Wackeleffekt aus.
     *
     * @param goal   getroffenes Tor
     * @param crossY Y-Koordinate der Latte
     */
    private void resolveCrossbarHit(Goal goal, int crossY) {
        boolean hitFromTop = vy > 0 && y < crossY;
        vy = -vy * 0.7;
        y  = hitFromTop ? crossY - r - 2 : crossY + r + 12;
        vx += (Math.random() - 0.5) * 4;
        goal.startShake();
    }
}
