package entities;

import java.awt.*;
import utils.Field;

/**
 * Repräsentiert einen Spieler: Bewegung, Physik, Schussmechanik und Darstellung.
 *
 * <p>Steuerung über boolesche Flags ({@link #left}, {@link #right}, {@link #jumping}, {@link
 * #kicking}), die vom {@code GamePanel} aus dem {@code InputManager} gesetzt werden. Animationen
 * (Arme, Beine, Haare) werden mathematisch aus der aktuellen Geschwindigkeit abgeleitet.
 *
 * <p>Koordinatenkonvention: {@link #y} ist der Fußpunkt. Der Sprite erstreckt sich von {@code y -
 * 150} (Kopfoberkante) bis {@code y} (Sohlen), sodass die Füße exakt auf {@link Field#GROUND_Y}
 * stehen – auf derselben Linie, auf der auch der Ball aufliegt.
 */
public class Player {

  /** Aktuelle X-Position (Fußpunkt). */
  public double x;

  /** Aktuelle Y-Position (Fußpunkt). */
  public double y;

  /** Horizontale Geschwindigkeitskomponente. */
  public double vx = 0;

  /** Vertikale Geschwindigkeitskomponente. */
  public double vy = 0;

  /** Steuerflag: Bewegung nach links. */
  public boolean left = false;

  /** Steuerflag: Bewegung nach rechts. */
  public boolean right = false;

  /** Steuerflag: Sprung (nur mit Bodenkontakt wirksam). */
  public boolean jumping = false;

  /** Steuerflag: Schuss – löst Cooldown aus und setzt sich selbst zurück. */
  public boolean kicking = false;

  /** Team dieses Spielers (Farben, Name). */
  public final Team team;

  /** Anzahl erzielter Tore in der aktuellen Partie. */
  public int score = 0;

  /** Anzeigename des Spielers ("Spieler 1" / "Spieler 2"). */
  public final String name;

  private boolean facingRight;
  private final boolean isPlayerOne;
  private final int startX;

  private int kickAnimationTimer = 0;
  private double walkCycle = 0;
  private double hairLagX = 0;
  private double hairLagY = 0;

  /** {@code true}, solange der Spieler auf der Torlatte steht. */
  private boolean onBar = false;

  /** Y-Position vor der Integration – für Tunneling-sichere Latten-Landung. */
  private double prevFrameY;

  private static final int KICK_DURATION = 15;
  private static final double KICK_FORCE = 20.0;
  private static final double PLAYER_PUSH_FORCE = 15.0;

  /**
   * Erstellt einen Spieler an der angegebenen Startposition.
   *
   * @param startX horizontale Startposition
   * @param isPlayerOne {@code true} für Spieler 1 (links), {@code false} für Spieler 2 (rechts)
   * @param team Team-Objekt; {@code null} erzeugt ein Standard-Team
   */
  public Player(int startX, boolean isPlayerOne, Team team) {
    this.startX = startX;
    this.x = startX;
    this.y = Field.GROUND_Y;
    this.isPlayerOne = isPlayerOne;
    this.team = team != null ? team : new Team("Default", Color.RED, Color.BLUE);
    this.name = isPlayerOne ? "Spieler 1" : "Spieler 2";
    this.facingRight = isPlayerOne;
  }

  /** Setzt den Spieler auf Startposition und Ausgangszustand zurück. */
  public void reset() {
    this.x = startX;
    this.y = Field.GROUND_Y;
    this.vx = 0;
    this.vy = 0;
    this.facingRight = isPlayerOne;
    this.kickAnimationTimer = 0;
    this.onBar = false;
  }

  /**
   * Aktualisiert Physik, Steuerung und Animationen für einen Frame.
   *
   * @param ball Spielball
   * @param stadium Stadion (liefert Tor-Grenzen für Wandkollision)
   * @param opponent gegnerischer Spieler
   */
  public void update(Ball ball, Stadium stadium, Player opponent) {
    if (left) facingRight = false;
    if (right) facingRight = true;

    double dir = 0;
    if (left) dir = -1;
    if (right) dir = 1;

    vx += dir * 1.4;
    boolean onGround = (y >= Field.GROUND_Y) || onBar;

    vx *= onGround ? 0.85 : 0.99;
    if (vx > 8.5) vx = 8.5;
    if (vx < -8.5) vx = -8.5;

    if (jumping && onGround) {
      vy = -16.0;
      onBar = false;
    }
    vy += Field.GRAVITY;

    prevFrameY = y;
    x += vx;
    y += vy;

    if (y > Field.GROUND_Y) {
      y = Field.GROUND_Y;
      vy = 0;
    }
    x = Math.max(stadium.leftGoal.backX + 15, Math.min(stadium.rightGoal.backX - 15, x));

    if (Math.abs(vx) > 0.5) walkCycle += 0.25;
    else walkCycle = 0;
    updateHairPhysics();

    if (kicking && kickAnimationTimer == 0) {
      kickAnimationTimer = KICK_DURATION;

      // Schuss wirkt nur nach vorn (in Blickrichtung), nicht hinter dem Rücken
      boolean ballInFront = facingRight ? ball.x >= x - 15 : ball.x <= x + 15;
      if (ballInFront && Math.hypot(x - ball.x, y - ball.y) < 92) {
        double dx = ball.x - x;
        double dy = ball.y - (y - 10);
        double dist = Math.max(Math.hypot(dx, dy), 1.0);
        ball.vx += (dx / dist) * KICK_FORCE;
        ball.vy += (dy / dist) * KICK_FORCE - 12;
        ball.lastTouchedPlayer = this;
        ball.justKicked = true;
      }

      boolean opponentInFront = facingRight ? opponent.x > x : opponent.x < x;
      if (opponentInFront && Math.hypot(x - opponent.x, y - opponent.y) < 100) {
        double kickDir = facingRight ? 1 : -1;
        opponent.vx += kickDir * PLAYER_PUSH_FORCE;
        opponent.vy -= 5;
      }
      kicking = false;
    }
    if (kickAnimationTimer > 0) kickAnimationTimer--;

    onBar = false;
    checkGoalCollision(stadium.leftGoal);
    checkGoalCollision(stadium.rightGoal);
    checkPlayerCollision(opponent, stadium);
  }

  /**
   * Zeichnet den Spieler (Schatten, Beine, Körper, Kopf, Arme). Der Fußpunkt {@link #y} entspricht
   * den Sohlen.
   *
   * @param g Grafik-Kontext
   * @param ball Spielball – wird für die Pupillen-Verfolgung benötigt
   */
  public void draw(Graphics2D g, Ball ball) {
    int ix = (int) x;
    int iy = (int) y;

    g.setColor(new Color(0, 0, 0, 70));
    g.fillOval(ix - 30, Field.GROUND_Y - 5, 60, 14);

    drawHand(g, ix, iy, false);
    drawLegs(g, ix, iy);

    g.setColor(team.primaryColor);
    g.fillRoundRect(ix - 22, iy - 83, 44, 40, 15, 15);
    g.setColor(team.secondaryColor);
    g.fillRect(ix - 22, iy - 65, 44, 10);

    drawHead(g, ix, iy, ball);
    drawHand(g, ix, iy, true);
  }

  /**
   * Verhindert, dass Spieler ineinander laufen (Überlappungskorrektur mit Energieaustausch). Wird
   * nur von Spieler 1 ausgeführt, damit dieselbe Überlappung nicht zweimal pro Frame aufgelöst
   * wird. Nach der Korrektur werden beide Spieler wieder auf Boden und Spielfeldgrenzen geklemmt,
   * damit niemand im Boden oder Tor versinkt.
   *
   * @param p anderer Spieler
   * @param stadium Stadion (liefert die seitlichen Grenzen)
   */
  private void checkPlayerCollision(Player p, Stadium stadium) {
    if (!isPlayerOne) return;

    double dx = x - p.x;
    double dy = y - p.y;
    double dist = Math.hypot(dx, dy);
    double minDist = 60;

    if (dist < minDist && dist > 0) {
      double overlap = (minDist - dist) / 2;
      double pushX = (dx / dist) * overlap;
      double pushY = (dy / dist) * overlap;

      this.x += pushX;
      this.y += pushY;
      p.x -= pushX;
      p.y -= pushY;

      this.vx += pushX * 0.1;
      p.vx -= pushX * 0.1;

      double lo = stadium.leftGoal.backX + 15;
      double hi = stadium.rightGoal.backX - 15;
      this.x = Math.max(lo, Math.min(hi, this.x));
      p.x = Math.max(lo, Math.min(hi, p.x));
      this.y = Math.min(this.y, Field.GROUND_Y);
      p.y = Math.min(p.y, Field.GROUND_Y);
    }
  }

  /** Simuliert physikalische Haarträgheit: Die Haare folgen der Bewegung mit Verzögerung. */
  private void updateHairPhysics() {
    double targetX = -vx * 1.2;
    double targetY = -vy * 1.2;
    hairLagX += (targetX - hairLagX) * 0.2;
    hairLagY += (targetY - hairLagY) * 0.2;
    hairLagX = Math.max(-15, Math.min(15, hairLagX));
    hairLagY = Math.max(-15, Math.min(15, hairLagY));
  }

  /**
   * Kollisionen mit der Torlatte: Landen und Stehen auf der Latte, Kopfstoß von unten sowie
   * seitliches Blocken auf Lattenhöhe.
   *
   * @param goal Tor, gegen das geprüft wird
   */
  private void checkGoalCollision(Goal goal) {
    Rectangle crossbar = goal.getCrossbarBounds();
    double headTop = y - 150;
    double headBottom = y - 105;
    int playerRadius = 22;

    boolean inBarX =
        x + playerRadius > crossbar.x && x - playerRadius < crossbar.x + crossbar.width;

    // Landung auf der Latte: Fußpunkt hat die Lattenoberkante in diesem Frame überquert
    if (inBarX && vy >= 0 && prevFrameY <= crossbar.y + 1 && y >= crossbar.y - 1) {
      y = crossbar.y;
      vy = 0;
      onBar = true;
    }

    // Kopfstoß von unten
    if (x > crossbar.x && x < crossbar.x + crossbar.width) {
      if (headTop < crossbar.y + crossbar.height && headTop > crossbar.y && vy < 0) {
        vy = 2.0;
        y = crossbar.y + crossbar.height + 150;
        goal.startShake();
      }
    }

    // Seitliches Blocken, wenn der Kopf auf Lattenhöhe ist
    if (headTop < crossbar.y + crossbar.height && headBottom > crossbar.y) {
      if (x + playerRadius > crossbar.x && x < crossbar.x + crossbar.width / 2 && vx > 0) {
        x = crossbar.x - playerRadius;
        vx = -2.0;
        goal.startShake();
      } else if (x - playerRadius < crossbar.x + crossbar.width
          && x > crossbar.x + crossbar.width / 2
          && vx < 0) {
        x = crossbar.x + crossbar.width + playerRadius;
        vx = 2.0;
        goal.startShake();
      }
    }
  }

  /**
   * Zeichnet einen Arm (vordere oder hintere Ebene). Die Arme schwingen gegenläufig zueinander
   * (Phasenversatz {@code π}).
   *
   * @param g Grafik-Kontext
   * @param ix X-Mittelpunkt des Spielers
   * @param iy Y-Fußpunkt des Spielers
   * @param isFrontLayer {@code true} für den vorderen Arm
   */
  private void drawHand(Graphics2D g, int ix, int iy, boolean isFrontLayer) {
    int offsetSign = facingRight ? 1 : -1;
    int layerOffset = isFrontLayer ? 18 : -18;

    int shoulderX = ix + (layerOffset * offsetSign);
    int shoulderY = iy - 81;

    double phase = isFrontLayer ? 0 : Math.PI;
    double angle = Math.sin(walkCycle + phase) * 0.8;
    if (kickAnimationTimer > 0) angle = isFrontLayer ? -2.0 : -2.5;

    int dir = facingRight ? 1 : -1;
    int handX = shoulderX + (int) (Math.sin(angle) * 20 * dir);
    int handY = shoulderY + (int) (Math.cos(angle) * 20);

    g.setColor(team.primaryColor);
    g.fillOval(shoulderX - 7, shoulderY - 7, 14, 14);
    g.setStroke(new BasicStroke(6));
    g.drawLine(shoulderX, shoulderY, handX, handY);
    g.setColor(team.skinColor);
    g.fillOval(handX - 6, handY - 6, 12, 12);
    g.setStroke(new BasicStroke(1));
  }

  /**
   * Zeichnet den prozeduralen Kopf. Die Pupillen verfolgen den Ball per {@code atan2}-Winkel.
   *
   * @param g Grafik-Kontext
   * @param ix X-Mittelpunkt des Spielers
   * @param iy Y-Fußpunkt des Spielers
   * @param ball Spielball (für Pupillen-Richtung)
   */
  private void drawHead(Graphics2D g, int ix, int iy, Ball ball) {
    g.setColor(team.skinColor);
    g.fillOval(ix - 38, iy - 150, 76, 76);
    drawHairLines(g, ix, iy);

    int eyeY = iy - 125;
    int eyeShift = facingRight ? 4 : -4;
    int leftEyeX = ix - 20 + eyeShift;
    int rightEyeX = ix + 4 + eyeShift;

    g.setColor(Color.WHITE);
    g.fillOval(leftEyeX, eyeY, 22, 22);
    g.fillOval(rightEyeX, eyeY, 22, 22);

    if (ball != null) {
      double angle = Math.atan2(ball.y - eyeY, ball.x - ix);
      int px = (int) (Math.cos(angle) * 4);
      int py = (int) (Math.sin(angle) * 4);
      g.setColor(Color.BLACK);
      g.fillOval(leftEyeX + 7 + px, eyeY + 7 + py, 7, 7);
      g.fillOval(rightEyeX + 7 + px, eyeY + 7 + py, 7, 7);
    }
  }

  /**
   * Zeichnet die Beine mit optionaler Tritt-Verschiebung. Die Sohlen enden exakt auf dem Fußpunkt
   * {@code iy}.
   *
   * @param g Grafik-Kontext
   * @param ix X-Mittelpunkt des Spielers
   * @param iy Y-Fußpunkt des Spielers
   */
  private void drawLegs(Graphics2D g, int ix, int iy) {
    int hipY = iy - 45;
    int kickOff = kickAnimationTimer > 0 ? (facingRight ? 20 : -20) : 0;

    g.setColor(team.secondaryColor);
    g.fillRect(ix - 20 + kickOff, hipY, 14, 40);
    g.fillRect(ix + 6 + kickOff, hipY, 14, 40);

    g.setColor(team.primaryColor);
    g.fillRect(ix - 20 + kickOff, hipY + 25, 14, 10);
    g.fillRect(ix + 6 + kickOff, hipY + 25, 14, 10);

    g.setColor(Color.BLACK);
    g.fillRoundRect(ix - 24 + kickOff, hipY + 35, 24, 10, 5, 5);
    g.fillRoundRect(ix + 2 + kickOff, hipY + 35, 24, 10, 5, 5);
  }

  /**
   * Zeichnet Strich-Haare, die durch {@code hairLag} bewegt werden.
   *
   * @param g Grafik-Kontext
   * @param ix X-Mittelpunkt des Spielers
   * @param iy Y-Fußpunkt des Spielers
   */
  private void drawHairLines(Graphics2D g, int ix, int iy) {
    g.setColor(team.secondaryColor);
    g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    int centerX = ix;
    int centerY = iy - 112;
    int skullRadius = 36;

    for (int i = -4; i <= 4; i++) {
      double angle = Math.toRadians(-90 + i * 18);
      int startX = centerX + (int) (Math.cos(angle) * skullRadius);
      int startY = centerY + (int) (Math.sin(angle) * skullRadius);
      int endX = startX + (int) (Math.cos(angle) * 12) + (int) hairLagX;
      int endY = startY + (int) (Math.sin(angle) * 12) + (int) hairLagY;
      g.drawLine(startX, startY, endX, endY);
    }
  }
}
