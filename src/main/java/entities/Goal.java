package entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import utils.Field;

/**
 * Repräsentiert ein Tor (links oder rechts).
 *
 * <p>Die Darstellung ist in zwei Ebenen aufgeteilt ({@link #drawBack} / {@link #drawFront}), damit
 * Spieler optisch zwischen Netz und vorderem Pfosten stehen können. Der vordere Pfosten ist bewusst
 * nur Vordergrund-Kulisse (Tiefen-Illusion des Genres): Der Ball fliegt „hinter ihm" ins Tor;
 * Kollision hat nur die Latte.
 */
public class Goal {

  /** X-Position des vorderen (feldseitigen) Pfostens. */
  public int frontX;

  /** X-Position des hinteren Pfostens. */
  public int backX;

  /** Y-Koordinate der Latte. */
  public int topY;

  /** Y-Koordinate des Torbodens – entspricht {@link Field#GROUND_Y}. */
  public int bottomY;

  /** {@code true} für linkes Tor, {@code false} für rechtes. */
  public boolean isLeft;

  private int shakeTimer = 0;

  /**
   * Aktueller Wackel-Versatz. Wird einmal pro Frame in {@link #update} gewürfelt, damit Netz
   * ({@code drawBack}) und Pfosten ({@code drawFront}) synchron wackeln.
   */
  private int shakeDx = 0;

  private int shakeDy = 0;

  private static final int SHAKE_FRAMES = 15;
  private static final int SHAKE_AMPLITUDE = 5;

  /**
   * Erstellt das Tor und setzt Koordinaten abhängig von der Seite.
   *
   * @param isLeft {@code true} für linkes Tor, {@code false} für rechtes
   */
  public Goal(boolean isLeft) {
    this.isLeft = isLeft;
    this.topY = 300;
    this.bottomY = Field.GROUND_Y;

    if (isLeft) {
      this.frontX = 120;
      this.backX = 60;
    } else {
      this.frontX = 880;
      this.backX = 940;
    }
  }

  /** Aktualisiert den Wackel-Timer und würfelt den Frame-Versatz. Einmal pro Frame aufrufen. */
  public void update() {
    if (shakeTimer > 0) {
      shakeTimer--;
      shakeDx = (int) (Math.random() * SHAKE_AMPLITUDE * 2) - SHAKE_AMPLITUDE;
      shakeDy = (int) (Math.random() * SHAKE_AMPLITUDE / 2);
    } else {
      shakeDx = 0;
      shakeDy = 0;
    }
  }

  /** Löst den Wackeleffekt aus (bei Treffer auf Pfosten oder Latte). */
  public void startShake() {
    this.shakeTimer = SHAKE_FRAMES;
  }

  /** Setzt den Wackeleffekt sofort zurück. */
  public void reset() {
    this.shakeTimer = 0;
    this.shakeDx = 0;
    this.shakeDy = 0;
  }

  /**
   * Zeichnet die hintere Tor-Ebene: Netz und hinterer Pfosten. Muss <em>vor</em> den Spielern
   * gerendert werden.
   *
   * @param g Grafik-Kontext
   */
  public void drawBack(Graphics2D g) {
    AffineTransform old = g.getTransform();
    applyShake(g);

    g.setColor(new Color(220, 220, 220, 140));
    g.setStroke(new BasicStroke(1f));

    for (int i = 0; i <= 10; i++) {
      int y = topY + i * 28;
      if (y > bottomY) break;
      g.drawLine(frontX, y, backX, y);
    }

    for (int i = 0; i <= 5; i++) {
      int xOffset = i * (Math.abs(frontX - backX) / 5);
      int x = isLeft ? backX + xOffset : backX - xOffset;
      g.drawLine(x, topY, x, bottomY);
    }

    g.drawLine(backX, topY, backX, bottomY);

    g.setColor(Color.WHITE);
    g.setStroke(new BasicStroke(6));
    g.drawLine(backX, topY + 4, backX, bottomY);

    g.setTransform(old);
  }

  /**
   * Zeichnet die vordere Tor-Ebene: Latte und vorderer Pfosten. Muss <em>nach</em> den Spielern
   * gerendert werden, um Tiefenwirkung zu erzeugen.
   *
   * @param g Grafik-Kontext
   */
  public void drawFront(Graphics2D g) {
    AffineTransform old = g.getTransform();
    applyShake(g);

    g.setColor(Color.WHITE);
    g.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
    g.drawLine(frontX, topY, frontX, bottomY);

    int[] xPoints = {frontX, backX, backX, frontX};
    int[] yPoints = {topY, topY, topY + 8, topY + 8};
    g.fillPolygon(xPoints, yPoints, 4);

    g.setTransform(old);
  }

  /**
   * Gibt die Kollisionsbox der Latte zurück.
   *
   * @return Rechteck der Latte in Spielkoordinaten
   */
  public Rectangle getCrossbarBounds() {
    int x = Math.min(frontX, backX);
    int w = Math.abs(frontX - backX);
    return new Rectangle(x, topY, w, 15);
  }

  /**
   * Verschiebt den Grafik-Kontext um den aktuellen Wackel-Versatz. Der Versatz wird in {@link
   * #update} bestimmt und ist für alle Zeichenebenen desselben Frames identisch.
   *
   * @param g Grafik-Kontext
   */
  private void applyShake(Graphics2D g) {
    if (shakeTimer > 0) {
      g.translate(shakeDx, shakeDy);
    }
  }
}
