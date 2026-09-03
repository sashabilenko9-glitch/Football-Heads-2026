package entities;

import java.awt.*;
import java.util.Random;

/**
 * Verwaltet den Stadion-Hintergrund: Tribüne, animierte Zuschauer, Rasen und Werbebanden. Enthält
 * außerdem die Tor-Instanzen ({@link #leftGoal}, {@link #rightGoal}).
 *
 * <p>Zuschauer-Arme werden alle 12 Frames neu gewürfelt (vorberechnetes Array), um 240 {@code
 * Math.random()}-Aufrufe pro Frame und damit Flackern zu vermeiden.
 */
public class Stadium {

  /** Linkes Tor. */
  public final Goal leftGoal;

  /** Rechtes Tor. */
  public final Goal rightGoal;

  private static final int CROWD_ROWS = 6;
  private static final int CROWD_COLS = 40;

  private final Color[] crowdColors;
  private final boolean[] crowdArms;
  private float animationTime = 0;
  private int armUpdateTimer = 0;
  private final Random rng = new Random();
  private final GradientPaint skyGradient =
      new GradientPaint(0, 0, new Color(135, 206, 235), 0, 400, Color.WHITE);
  private final GradientPaint grassGradient =
      new GradientPaint(0, 350, new Color(34, 139, 34), 0, 700, new Color(0, 100, 0));

  /** Initialisiert Zuschauer-Farben, Arm-Zustände und Tore. */
  public Stadium() {
    crowdColors = new Color[CROWD_ROWS * CROWD_COLS];
    crowdArms = new boolean[CROWD_ROWS * CROWD_COLS];
    for (int i = 0; i < crowdColors.length; i++) {
      crowdColors[i] = new Color(rng.nextInt(255), rng.nextInt(255), rng.nextInt(255));
      crowdArms[i] = rng.nextFloat() > 0.95f;
    }
    leftGoal = new Goal(true);
    rightGoal = new Goal(false);
  }

  /** Aktualisiert Animations-Timer, Tor-Shake und Zuschauer-Arme. Einmal pro Frame aufrufen. */
  public void update() {
    animationTime += 0.1f;
    leftGoal.update();
    rightGoal.update();

    armUpdateTimer++;
    if (armUpdateTimer >= 12) {
      armUpdateTimer = 0;
      for (int i = 0; i < crowdArms.length; i++) {
        crowdArms[i] = rng.nextFloat() > 0.95f;
      }
    }
  }

  /** Setzt Tor-Shake und Animations-Timer zurück. */
  public void reset() {
    leftGoal.reset();
    rightGoal.reset();
    animationTime = 0;
  }

  /**
   * Zeichnet den vollständigen Stadion-Hintergrund. Die Tore werden hier <em>nicht</em> gezeichnet;
   * das übernimmt {@code GamePanel} mit {@code drawBack} → Spieler → {@code drawFront}.
   *
   * @param g Grafik-Kontext
   * @param isGoal {@code true} aktiviert schnelles Jubel-Hüpfen der Zuschauer
   */
  public void drawBackground(Graphics2D g, boolean isGoal) {
    int width = 1000;
    int height = 700;

    g.setPaint(skyGradient);
    g.fillRect(0, 0, width, height);

    g.setColor(new Color(100, 100, 100));
    g.fillRect(0, 100, width, 250);

    g.setColor(new Color(50, 50, 60));
    g.fillPolygon(new int[] {0, width, width, 0}, new int[] {80, 80, 120, 120}, 4);

    drawCrowd(g, isGoal);

    g.setPaint(grassGradient);
    g.fillRect(0, 350, width, 350);

    g.setColor(new Color(0, 0, 0, 20));
    for (int i = 0; i < width; i += 100) g.fillRect(i, 350, 50, 350);

    g.setColor(new Color(255, 255, 255, 200));
    g.setStroke(new BasicStroke(4));
    g.drawLine(500, 350, 500, 700);
    g.drawOval(400, 450, 200, 100);
    g.drawLine(0, 350, 1000, 350);
    g.setStroke(new BasicStroke(3));

    drawAdBoards(g);
  }

  /**
   * Zeichnet den Hintergrund ohne Jubel-Animation. Wird im Team-Auswahl-Bildschirm verwendet.
   *
   * @param g Grafik-Kontext
   */
  public void drawBackgroundOnly(Graphics2D g) {
    drawBackground(g, false);
  }

  /**
   * Zeichnet alle Zuschauer mit Sinus-La-Ola-Animation.
   *
   * @param g Grafik-Kontext
   * @param isGoal {@code true} aktiviert schnelles Jubel-Hüpfen
   */
  private void drawCrowd(Graphics2D g, boolean isGoal) {
    for (int row = 0; row < CROWD_ROWS; row++) {
      for (int col = 0; col < CROWD_COLS; col++) {
        int index = row * CROWD_COLS + col;
        g.setColor(crowdColors[index]);

        double offset =
            isGoal
                ? Math.sin(animationTime * 5 + col) * 10
                : Math.sin(animationTime + col * 0.5) * 3;

        int x = 20 + col * 25;
        int y = 150 + row * 25 + (int) offset;

        g.fillOval(x, y, 18, 22);

        if (isGoal || crowdArms[index]) {
          g.fillRect(x - 2, y - 5, 4, 10);
          g.fillRect(x + 16, y - 5, 4, 10);
        }
      }
    }
  }

  /**
   * Zeichnet die Werbebanden entlang der Spielfeld-Grenze.
   *
   * @param g Grafik-Kontext
   */
  private void drawAdBoards(Graphics2D g) {
    int y = 320;
    int h = 30;

    g.setColor(new Color(20, 20, 20));
    g.fillRect(0, y, 1000, h);

    Color[] adColors = {Color.RED, Color.ORANGE, Color.BLUE, Color.MAGENTA};
    String[] brands = {"JAVA", "SVG MITTWEIDA", "GERMANIA", "AUF GEHTS"};
    int w = 250;

    for (int i = 0; i < 4; i++) {
      g.setColor(adColors[i]);
      g.fillRect(i * w + 10, y + 2, w - 20, h - 4);
      g.setColor(Color.WHITE);
      g.setFont(new Font("Arial", Font.BOLD, 14));
      g.drawString(brands[i], i * w + 80, y + 22);
    }
  }
}
