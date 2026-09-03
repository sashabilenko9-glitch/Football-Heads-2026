package com.footballheads;

import entities.Ball;
import entities.Player;
import entities.Stadium;
import entities.Team;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.PauseMenu;

/**
 * Zentrales Spielpanel: enthält Game-Loop, Physik-Update, Rendering und Eingabeverarbeitung.
 *
 * <p>Das Panel arbeitet mit einer festen logischen Auflösung von 1000 × 700 Pixeln. In {@link
 * #paintComponent} wird die Szene <em>gleichmäßig</em> skaliert (kleinerer Faktor aus Breite/Höhe)
 * und zentriert – nicht passende Ränder bleiben als schwarze Balken (Letterbox), damit Ball und
 * Köpfe auf jedem Seitenverhältnis rund bleiben.
 *
 * <p>Der Game-Loop läuft mit festem Zeitschritt (Akkumulator-Muster): Der Swing-Timer dient nur als
 * Taktgeber; die Physik wird in festen Schritten von {@code STEP_NS} nachgeholt, sodass die
 * Spielgeschwindigkeit nicht vom Timer-Jitter abhängt.
 *
 * <p>Tastatureingaben laufen über Key-Bindings ({@code WHEN_IN_FOCUSED_WINDOW}) statt über einen
 * {@code KeyListener} – sie funktionieren damit unabhängig davon, welche Komponente gerade den
 * Fokus hat.
 *
 * <p>Rendering-Reihenfolge:
 *
 * <ol>
 *   <li>Stadion-Hintergrund
 *   <li>Ballschatten
 *   <li>Spieler
 *   <li>Ball
 *   <li>Hintere Torteile (Netz, hinterer Pfosten)
 *   <li>Vordere Torteile (Latte, vorderer Pfosten)
 *   <li>HUD
 *   <li>Tor-Text / Endbildschirm
 * </ol>
 */
public final class GamePanel extends JPanel implements ActionListener, MouseListener {

  private static final long serialVersionUID = 1L;

  private static final int BASE_WIDTH = 1000;
  private static final int BASE_HEIGHT = 700;
  private static final int MATCH_DURATION = 60;
  private static final int GOAL_PAUSE = 120;
  private static final long ONE_SECOND_NS = 1_000_000_000L;

  /** Fester Physik-Zeitschritt: 1/60 Sekunde in Nanosekunden. */
  private static final long STEP_NS = 16_666_667L;

  /** Obergrenze für einen Frame-Sprung (verhindert Aufhol-Spirale nach Lags). */
  private static final long MAX_FRAME_NS = 100_000_000L;

  /** Ballradius in Spielkoordinaten (für die Torlinien-Prüfung). */
  private static final int BALL_RADIUS = 20;

  private static final Color C_BORDER = new Color(0, 160, 255);
  private static final Color C_BORDER2 = new Color(0, 220, 255, 80);
  private static final Color C_ACCENT = new Color(0, 200, 255);
  private static final Color C_GOLD = new Color(255, 210, 0);
  private static final Color C_OWN_GOAL = new Color(255, 80, 80);
  private static final Color C_WHITE = Color.WHITE;

  private static final Rectangle BTN_REPLAY = new Rectangle(280, 590, 180, 55);
  private static final Rectangle BTN_MENU = new Rectangle(540, 590, 180, 55);

  private static final String FONT_ARIAL_BLACK = "Arial Black";
  private static final String FONT_ARIAL = "Arial";

  private static final Font F_SCORE = new Font(FONT_ARIAL_BLACK, Font.BOLD, 85);
  private static final Font F_TIMER = new Font(FONT_ARIAL_BLACK, Font.BOLD, 52);
  private static final Font F_TIMER_GG = new Font(FONT_ARIAL_BLACK, Font.BOLD, 26);
  private static final Font F_GOAL_TEXT = new Font(FONT_ARIAL_BLACK, Font.BOLD, 100);
  private static final Font F_END_TITLE = new Font(FONT_ARIAL_BLACK, Font.BOLD, 58);
  private static final Font F_END_SCORE = new Font(FONT_ARIAL_BLACK, Font.BOLD, 90);
  private static final Font F_BTN = new Font(FONT_ARIAL, Font.BOLD, 22);
  private static final Font F_EVENT_BOLD = new Font(FONT_ARIAL, Font.BOLD, 15);
  private static final Font F_EVENT = new Font(FONT_ARIAL, Font.PLAIN, 15);
  private static final Font F_COL_HDR = new Font(FONT_ARIAL, Font.BOLD, 17);
  private static final Font F_HINT = new Font(FONT_ARIAL, Font.PLAIN, 15);

  /** HUD-Hintergrund: leicht transparent, damit der Ball dahinter sichtbar bleibt. */
  private static final Color C_HUD_BG = new Color(5, 10, 35, 205);

  private static final Color C_SCORE_GLOW = new Color(0, 180, 255, 80);
  private static final Color C_GOAL_DARK = new Color(0, 0, 0, 150);
  private static final Color C_OWN_GOAL_C = new Color(255, 80, 80);
  private static final Color C_END_BG = new Color(2, 5, 20, 220);
  private static final Color C_END_PANEL = new Color(8, 15, 45, 240);
  private static final Color C_DIVIDER = new Color(0, 150, 255, 120);
  private static final Color C_SEPARATOR = new Color(255, 255, 255, 15);
  private static final Color C_GOAL_SHADE = new Color(255, 200, 0, 38);

  /** Unveränderliches Tor-Ereignis für die Spielstatistik auf dem Endbildschirm. */
  private static final class GoalEvent {
    final String time;
    final String playerName;
    final boolean isOwnGoal;
    final boolean isGoldenGoal;
    final boolean forPlayer1;

    GoalEvent(
        String time,
        String playerName,
        boolean isOwnGoal,
        boolean isGoldenGoal,
        boolean forPlayer1) {
      this.time = time;
      this.playerName = playerName;
      this.isOwnGoal = isOwnGoal;
      this.isGoldenGoal = isGoldenGoal;
      this.forPlayer1 = forPlayer1;
    }
  }

  /**
   * Zustände des Spielablaufs (State-Pattern). Ersetzt die früheren einzelnen {@code
   * matchEnded}/{@code paused}/{@code goalCelebration}-Flags durch einen einzigen, stets
   * eindeutigen Zustand - siehe docs/adr/0007-state-and-observer-patterns.md.
   */
  private enum MatchState {
    PLAYING,
    PAUSED,
    GOAL_CELEBRATION,
    ENDED
  }

  private static final Logger LOG = LoggerFactory.getLogger(GamePanel.class);

  // Not designed to survive Swing serialization (no game ever gets serialized) - see
  // docs/adr/0005-quality-gate-tooling.md.
  private final transient GameFrame parentFrame;
  private transient Stadium stadium;
  private transient Player p1;
  private transient Player p2;
  private transient Ball ball;
  private final transient Team team1;
  private final transient Team team2;
  private final Timer gameTimer;
  private final PauseMenu pauseMenu;

  /** Zentraler Eingabe-Manager: führt Tastatur- und Mobil-Eingaben zusammen. */
  private final transient InputManager inputManager = new InputManager();

  /** Gleichmäßiger Skalierungsfaktor (Letterbox). */
  private double scale = 1.0;

  /** Horizontaler Versatz des Spielfelds im Panel (Letterbox-Rand). */
  private double offsetX = 0;

  /** Vertikaler Versatz des Spielfelds im Panel (Letterbox-Rand). */
  private double offsetY = 0;

  private MatchState state = MatchState.PLAYING;
  private int goalResetTimer = 0;

  /** {@code true}, wenn nach der laufenden Tor-Feier das Spiel endet (Golden Goal). */
  private boolean goldenGoalPending = false;

  private int timeLeft = MATCH_DURATION;
  private boolean goldenTime = false;
  private int extraTime = 0;
  private long lastTimeNs;

  /** Zeitbasis des Fixed-Timestep-Loops. */
  private long lastFrameNs;

  private long accumulatorNs = 0;

  private final transient List<GoalEvent> matchEvents = new ArrayList<>();

  /**
   * Beobachter für Tor- und Spielende-Ereignisse (Observer-Pattern). Entkoppelt die Spiellogik von
   * dem, was auf ein Tor/Spielende reagiert - aktuell nur Logging, künftig z.&nbsp;B. Soundeffekte
   * oder eine Replay-Aufzeichnung (siehe ROADMAP.md).
   */
  private final transient List<MatchListener> matchListeners = new ArrayList<>();

  /**
   * Stellt den {@link InputManager} für das {@code MobileControlsPanel} und andere externe
   * Komponenten bereit.
   *
   * @return zentraler Eingabe-Manager dieses Panels
   */
  public InputManager getInputManager() {
    return inputManager;
  }

  /**
   * Registriert einen Beobachter für Tor- und Spielende-Ereignisse.
   *
   * @param listener zu registrierender Beobachter
   */
  public void addMatchListener(MatchListener listener) {
    matchListeners.add(listener);
  }

  /**
   * Gibt an, ob das Spiel gerade pausiert ist.
   *
   * @return {@code true}, wenn das Spiel gerade pausiert ist
   */
  public boolean isPaused() {
    return state == MatchState.PAUSED;
  }

  /**
   * Gibt an, ob der Endbildschirm angezeigt wird.
   *
   * @return {@code true}, wenn der Endbildschirm angezeigt wird
   */
  public boolean isMatchEnded() {
    return state == MatchState.ENDED;
  }

  /**
   * Erstellt das Spielpanel, richtet Eingabe ein und startet den Game-Loop.
   *
   * @param frame übergeordnetes Fenster
   * @param t1 Team des ersten Spielers
   * @param t2 Team des zweiten Spielers
   */
  public GamePanel(GameFrame frame, Team t1, Team t2) {
    this.parentFrame = frame;
    this.team1 = t1;
    this.team2 = t2;

    setBackground(Color.BLACK);
    setFocusable(true);
    setupKeyBindings();
    addMouseListener(this);

    gameTimer = new Timer(16, this);
    stadium = new Stadium();

    pauseMenu = new PauseMenu(this, parentFrame);
    pauseMenu.setVisible(false);
    setLayout(new BorderLayout());
    add(pauseMenu, BorderLayout.CENTER);

    addMatchListener(new LoggingMatchListener());

    LOG.info("New match: {} vs {}", t1.name, t2.name);
    restartGame();
    SwingUtilities.invokeLater(this::requestFocusInWindow);
    gameTimer.start();
  }

  /** Protokolliert Tor- und Spielende-Ereignisse über SLF4J. */
  private static final class LoggingMatchListener implements MatchListener {
    @Override
    public void onGoal(String scoringTeamName, boolean ownGoal, boolean goldenGoal) {
      LOG.info(
          "Goal for {}{}{}",
          scoringTeamName,
          ownGoal ? " (own goal)" : "",
          goldenGoal ? " [GOLDEN GOAL]" : "");
    }

    @Override
    public void onMatchEnded(String team1Name, int score1, String team2Name, int score2) {
      LOG.info("Match ended: {} {} - {} {}", team1Name, score1, score2, team2Name);
    }
  }

  /**
   * Registriert die Key-Bindings beider Spieler sowie ESC (Pause). {@code WHEN_IN_FOCUSED_WINDOW}
   * macht die Steuerung unabhängig vom Fokus.
   */
  private void setupKeyBindings() {
    bindKey(KeyEvent.VK_A, "p1.left", on -> inputManager.setP1LeftFromKeyboard(on));
    bindKey(KeyEvent.VK_D, "p1.right", on -> inputManager.setP1RightFromKeyboard(on));
    bindKey(KeyEvent.VK_W, "p1.jump", on -> inputManager.setP1JumpFromKeyboard(on));
    bindKey(KeyEvent.VK_S, "p1.kick", on -> inputManager.setP1KickFromKeyboard(on));
    bindKey(KeyEvent.VK_LEFT, "p2.left", on -> inputManager.setP2LeftFromKeyboard(on));
    bindKey(KeyEvent.VK_RIGHT, "p2.right", on -> inputManager.setP2RightFromKeyboard(on));
    bindKey(KeyEvent.VK_UP, "p2.jump", on -> inputManager.setP2JumpFromKeyboard(on));
    bindKey(KeyEvent.VK_DOWN, "p2.kick", on -> inputManager.setP2KickFromKeyboard(on));

    getInputMap(WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pause");
    getActionMap()
        .put(
            "pause",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                if (state != MatchState.ENDED) togglePause();
              }
            });
  }

  /**
   * Bindet eine Taste (gedrückt/losgelassen) an einen booleschen Eingabe-Setter.
   *
   * @param keyCode Tastencode aus {@link KeyEvent}
   * @param name eindeutiger Aktionsname
   * @param target Setter, der den Zustand erhält
   */
  private void bindKey(int keyCode, String name, java.util.function.Consumer<Boolean> target) {
    InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = getActionMap();

    im.put(KeyStroke.getKeyStroke(keyCode, 0, false), name + ".press");
    am.put(
        name + ".press",
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            target.accept(true);
          }
        });

    im.put(KeyStroke.getKeyStroke(keyCode, 0, true), name + ".release");
    am.put(
        name + ".release",
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            target.accept(false);
          }
        });
  }

  /** Setzt das Spiel vollständig zurück und startet eine neue Partie. */
  public void restartGame() {
    p1 = new Player(280, true, team1);
    p2 = new Player(720, false, team2);
    ball = new Ball();
    if (stadium != null) stadium.reset();

    timeLeft = MATCH_DURATION;
    goldenTime = false;
    extraTime = 0;
    state = MatchState.PLAYING;
    goldenGoalPending = false;
    matchEvents.clear();
    lastTimeNs = System.nanoTime();
    lastFrameNs = lastTimeNs;
    accumulatorNs = 0;

    if (!gameTimer.isRunning()) gameTimer.start();
    if (pauseMenu != null) pauseMenu.setVisible(false);
    requestFocusInWindow();
    repaint();
  }

  /**
   * Schaltet den Pause-Zustand um. Ignoriert den Aufruf bei beendetem Spiel oder laufender
   * Tor-Feier.
   */
  public void togglePause() {
    if (state == MatchState.ENDED || state == MatchState.GOAL_CELEBRATION) return;
    state = (state == MatchState.PAUSED) ? MatchState.PLAYING : MatchState.PAUSED;
    if (pauseMenu != null) pauseMenu.setVisible(state == MatchState.PAUSED);
    if (state == MatchState.PLAYING) {
      lastTimeNs = System.nanoTime();
      lastFrameNs = lastTimeNs;
      accumulatorNs = 0;
      requestFocusInWindow();
    }
  }

  /**
   * Taktgeber des Game-Loops (Swing-Timer). Sammelt die real vergangene Zeit und holt die Physik in
   * festen {@code STEP_NS}-Schritten nach (Fixed-Timestep-Akkumulator).
   *
   * @param e Timer-Ereignis
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    long now = System.nanoTime();
    long frameNs = now - lastFrameNs;
    lastFrameNs = now;

    if (state == MatchState.PAUSED || state == MatchState.ENDED) {
      // Weiter zeichnen, damit halbtransparente Overlays keine Artefakte hinterlassen
      repaint();
      return;
    }

    if (frameNs > MAX_FRAME_NS) frameNs = MAX_FRAME_NS;
    accumulatorNs += frameNs;

    while (accumulatorNs >= STEP_NS && state != MatchState.ENDED) {
      stepGame();
      accumulatorNs -= STEP_NS;
    }
    repaint();
  }

  /**
   * Führt genau einen Physik-Schritt aus: Eingabe-Synchronisation, Stadion-Animation, Tor-Feier,
   * Match-Timer, Spieler-, Ball- und Tor-Logik.
   */
  private void stepGame() {
    p1.left = inputManager.getP1Left();
    p1.right = inputManager.getP1Right();
    p1.jumping = inputManager.getP1Jump();
    p1.kicking = inputManager.getP1Kick();

    p2.left = inputManager.getP2Left();
    p2.right = inputManager.getP2Right();
    p2.jumping = inputManager.getP2Jump();
    p2.kicking = inputManager.getP2Kick();

    stadium.update();

    if (state == MatchState.GOAL_CELEBRATION) {
      goalResetTimer--;
      if (goalResetTimer <= 0) {
        state = MatchState.PLAYING;
        if (goldenGoalPending) {
          endMatch();
          return;
        }
        p1.reset();
        p2.reset();
        ball.reset();
        stadium.reset();
        lastTimeNs = System.nanoTime();
      }
      return;
    }

    updateTimer();
    p1.update(ball, stadium, p2);
    p2.update(ball, stadium, p1);
    ball.update(p1, p2, stadium);
    checkGoals();
  }

  /** Zählt die Spielzeit sekündlich herunter und aktiviert bei Gleichstand Golden-Goal. */
  private void updateTimer() {
    long now = System.nanoTime();
    if (now - lastTimeNs < ONE_SECOND_NS) return;
    lastTimeNs += ONE_SECOND_NS;

    if (!goldenTime) {
      timeLeft--;
      if (timeLeft <= 0) {
        if (p1.score == p2.score) {
          goldenTime = true;
          extraTime = 0;
        } else {
          endMatch();
        }
      }
    } else {
      extraTime++;
    }
  }

  /**
   * Prüft, ob der Ball die Torlinie vollständig überquert hat, und löst die Feier aus. Der Ball
   * zählt erst als Tor, wenn er komplett hinter der Pfosten-Ebene ist (Mittelpunkt plus Radius) –
   * so verschwindet er optisch sauber hinter dem vorderen Pfosten (Tiefen-Illusion, siehe {@link
   * entities.Goal}).
   */
  private void checkGoals() {
    boolean inGoalHeight = ball.y > stadium.leftGoal.topY + 20 && ball.y < stadium.leftGoal.bottomY;
    if (!inGoalHeight) return;

    if (ball.x + BALL_RADIUS < stadium.leftGoal.frontX) {
      p2.score++;
      stadium.leftGoal.startShake();
      recordGoal(p2);
      triggerGoalCelebration();
    } else if (ball.x - BALL_RADIUS > stadium.rightGoal.frontX) {
      p1.score++;
      stadium.rightGoal.startShake();
      recordGoal(p1);
      triggerGoalCelebration();
    }
  }

  /**
   * Startet die Tor-Feier-Animation. Bei Golden Goal wird das Spielende erst nach Ablauf der Feier
   * ausgelöst, damit der entscheidende Treffer nicht abrupt im Endbildschirm untergeht.
   */
  private void triggerGoalCelebration() {
    state = MatchState.GOAL_CELEBRATION;
    goalResetTimer = GOAL_PAUSE;
    if (goldenTime) goldenGoalPending = true;
  }

  /** Beendet das Spiel und stoppt den Game-Loop-Timer. */
  private void endMatch() {
    state = MatchState.ENDED;
    gameTimer.stop();
    for (MatchListener listener : matchListeners) {
      listener.onMatchEnded(team1.name, p1.score, team2.name, p2.score);
    }
    repaint();
  }

  /**
   * Erstellt einen {@link GoalEvent} und hängt ihn an die Ereignisliste. Eigentor-Erkennung erfolgt
   * über {@link Ball#lastTouchedPlayer}.
   *
   * @param scorer Spieler, in dessen Tor der Ball gefallen ist
   */
  // Player has no equals() override by design (each instance is a unique, mutable game
  // entity) - reference comparison below is intentional identity comparison, not a bug.
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  private void recordGoal(Player scorer) {
    Player lastTouched = ball.lastTouchedPlayer;
    String timeStr = goldenTime ? "+" + extraTime + "'" : (MATCH_DURATION - timeLeft) + "'";

    boolean isOwnGoal;
    String name;

    if (scorer == p2 && lastTouched == p1) {
      isOwnGoal = true;
      name = p1.team.name;
    } else if (scorer == p1 && lastTouched == p2) {
      isOwnGoal = true;
      name = p2.team.name;
    } else {
      isOwnGoal = false;
      name = (lastTouched != null) ? lastTouched.team.name : scorer.team.name;
    }

    matchEvents.add(new GoalEvent(timeStr, name, isOwnGoal, goldenTime, scorer == p1));
    for (MatchListener listener : matchListeners) {
      listener.onGoal(name, isOwnGoal, goldenTime);
    }
  }

  /**
   * Zeichnet die gesamte Spielszene mit gleichmäßiger Skalierung und Letterbox.
   *
   * @param g Grafik-Kontext (wird intern auf {@code Graphics2D} gecastet)
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int w = getWidth();
    int h = getHeight();
    scale = Math.min(w / (double) BASE_WIDTH, h / (double) BASE_HEIGHT);
    offsetX = (w - BASE_WIDTH * scale) / 2.0;
    offsetY = (h - BASE_HEIGHT * scale) / 2.0;

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.translate(offsetX, offsetY);
    g2d.scale(scale, scale);
    g2d.clipRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

    boolean cheering = state == MatchState.GOAL_CELEBRATION || state == MatchState.ENDED;

    // 1. Hintergrund
    stadium.drawBackground(g2d, cheering);

    // 2. Ballschatten und Spielobjekte – VOR dem Netz
    ball.drawShadow(g2d);
    p1.draw(g2d, ball);
    p2.draw(g2d, ball);
    ball.draw(g2d);

    // 3. Hintere Torteile (Netz + hinterer Pfosten) – über Spielern und Ball,
    //    dadurch erscheinen Spieler und Ball im Tor HINTER dem Netz
    stadium.leftGoal.drawBack(g2d);
    stadium.rightGoal.drawBack(g2d);

    // 4. Vordere Torteile (Latte + vorderer Pfosten) – über allem
    stadium.leftGoal.drawFront(g2d);
    stadium.rightGoal.drawFront(g2d);

    // 5. HUD, Tor-Text, Endbildschirm
    drawHUD(g2d);
    if (state == MatchState.GOAL_CELEBRATION) drawGoalText(g2d);
    if (state == MatchState.ENDED) drawEndScreen(g2d);

    g2d.dispose();
  }

  /**
   * Hellt sehr dunkle Team-Farben für Anzeigen (HUD-Punkt, Endbildschirm-Text) auf, damit
   * z.&nbsp;B. ein schwarzes Trikot auf dunklem Grund sichtbar bleibt. Die eigentliche Trikotfarbe
   * des Spielers bleibt unverändert.
   *
   * @param c Team-Farbe
   * @return anzeigetaugliche Farbe
   */
  private static Color displayColor(Color c) {
    int luminance = (c.getRed() * 3 + c.getGreen() * 6 + c.getBlue()) / 10;
    if (luminance >= 60) return c;
    return new Color(
        Math.min(255, c.getRed() + 100),
        Math.min(255, c.getGreen() + 100),
        Math.min(255, c.getBlue() + 110));
  }

  /**
   * Zeichnet das Scoreboard-Panel mit Spielstand, Timer und Team-Indikatoren.
   *
   * @param g Grafik-Kontext (bereits skaliert)
   */
  private void drawHUD(Graphics2D g) {
    int bx = 300;
    int by = 15;
    int bw = 400;
    int bh = 105;

    g.setColor(C_HUD_BG);
    g.fillRoundRect(bx, by, bw, bh, 25, 25);

    g.setStroke(new BasicStroke(3));
    g.setColor(goldenTime ? C_GOLD : C_BORDER);
    g.drawRoundRect(bx + 1, by + 1, bw - 2, bh - 2, 25, 25);
    g.setColor(goldenTime ? new Color(255, 210, 0, 60) : C_BORDER2);
    g.setStroke(new BasicStroke(7));
    g.drawRoundRect(bx + 1, by + 1, bw - 2, bh - 2, 25, 25);

    String score = p1.score + " : " + p2.score;
    g.setFont(F_SCORE);
    g.setColor(C_SCORE_GLOW);
    centerString(g, score, 502, 92);
    g.setColor(C_WHITE);
    centerString(g, score, 500, 90);

    String timeStr =
        goldenTime
            ? Messages.get("hud.golden") + " " + formatTime(extraTime)
            : formatTime(timeLeft);
    Color tc = goldenTime ? C_GOLD : (timeLeft <= 10 ? C_OWN_GOAL : C_ACCENT);
    g.setFont(goldenTime ? F_TIMER_GG : F_TIMER);
    g.setColor(tc);
    centerString(g, timeStr, 500, 145);

    drawGlowDot(g, 335, 55, displayColor(team1.primaryColor));
    drawGlowDot(g, 665, 55, displayColor(team2.primaryColor));
    if (state != MatchState.PAUSED) {
      g.setFont(F_HINT);
      g.setColor(new Color(255, 255, 255, 90));
      String hint = Messages.get("hud.pauseHint");
      int hw = g.getFontMetrics().stringWidth(hint);
      g.drawString(hint, (BASE_WIDTH - hw) / 2, BASE_HEIGHT - 12);
    }
  }

  /**
   * Zeichnet einen leuchtenden Team-Farbpunkt im HUD.
   *
   * @param g Grafik-Kontext
   * @param cx X-Mittelpunkt
   * @param cy Y-Mittelpunkt
   * @param c Team-Farbe
   */
  private void drawGlowDot(Graphics2D g, int cx, int cy, Color c) {
    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
    g.fillOval(cx - 18, cy - 18, 36, 36);
    g.setColor(c);
    g.fillOval(cx - 10, cy - 10, 20, 20);
    g.setColor(new Color(255, 255, 255, 150));
    g.fillOval(cx - 5, cy - 7, 6, 5);
  }

  /**
   * Zeichnet den Tor-Text mit automatischer Schriftgrößenanpassung. Die Größe wird reduziert, bis
   * der Text in 820 Pixel passt.
   *
   * @param g Grafik-Kontext
   */
  private void drawGoalText(Graphics2D g) {
    String txt = Messages.get("goal.text");
    int fontSize = 100;

    g.setFont(F_GOAL_TEXT);
    while (g.getFontMetrics().stringWidth(txt) > 820 && fontSize > 40) {
      fontSize--;
      g.setFont(new Font(FONT_ARIAL_BLACK, Font.BOLD, fontSize));
    }

    FontMetrics fm = g.getFontMetrics();
    int tw = fm.stringWidth(txt);
    int tx = 500 - tw / 2;
    int ty = 375;

    g.setColor(C_GOAL_DARK);
    g.fillRoundRect(tx - 24, ty - fontSize - 8, tw + 48, fontSize + 32, 22, 22);

    g.setColor(C_GOAL_SHADE);
    for (int i = 6; i >= 1; i--) {
      g.drawString(txt, tx + i, ty + i);
      g.drawString(txt, tx - i, ty - i);
    }

    g.setColor(new Color(0, 0, 0, 190));
    g.drawString(txt, tx + 4, ty + 4);
    g.setColor(C_GOLD);
    g.drawString(txt, tx, ty);
  }

  /**
   * Zeichnet den Endbildschirm mit Ergebnis, Tor-Protokoll und Aktions-Buttons. Das Tor-Protokoll
   * ist pro Spalte auf die verfügbare Höhe begrenzt; bei Überlauf werden nur die letzten Einträge
   * mit „…"-Marker angezeigt.
   *
   * @param g Grafik-Kontext
   */
  private void drawEndScreen(Graphics2D g) {
    g.setColor(C_END_BG);
    g.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

    int px = 40;
    int py = 30;
    int pw = 920;
    int ph = 630;
    g.setColor(C_END_PANEL);
    g.fillRoundRect(px, py, pw, ph, 20, 20);

    g.setStroke(new BasicStroke(1.5f));
    g.setColor(C_BORDER2);
    g.drawRoundRect(px + 4, py + 4, pw - 8, ph - 8, 18, 18);
    g.setStroke(new BasicStroke(2.5f));
    g.setColor(C_BORDER);
    g.drawRoundRect(px, py, pw, ph, 20, 20);

    String title;
    Color titleColor;
    if (p1.score > p2.score) {
      title = team1.name + "  " + Messages.get("end.winSuffix");
      titleColor = displayColor(team1.primaryColor);
    } else if (p2.score > p1.score) {
      title = team2.name + "  " + Messages.get("end.winSuffix");
      titleColor = displayColor(team2.primaryColor);
    } else {
      title = Messages.get("end.draw");
      titleColor = C_ACCENT;
    }

    int fontSize = 58;
    g.setFont(F_END_TITLE);
    while (g.getFontMetrics().stringWidth(title) > 820 && fontSize > 22) {
      fontSize--;
      g.setFont(new Font(FONT_ARIAL_BLACK, Font.BOLD, fontSize));
    }

    g.setColor(new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), 60));
    centerString(g, title, 502, 107);
    g.setColor(titleColor);
    centerString(g, title, 500, 105);

    drawNeonLine(g, 80, 118, 920, 118);

    g.setFont(F_END_SCORE);
    g.setColor(new Color(0, 200, 255, 50));
    centerString(g, p1.score + " - " + p2.score, 502, 212);
    g.setColor(C_WHITE);
    centerString(g, p1.score + " - " + p2.score, 500, 210);

    int colY = 250;
    g.setFont(F_COL_HDR);
    g.setColor(displayColor(team1.primaryColor));
    g.drawString(team1.name, 90, colY);

    g.setColor(displayColor(team2.primaryColor));
    String t2n = team2.name;
    g.drawString(t2n, 910 - g.getFontMetrics().stringWidth(t2n), colY);

    g.setStroke(new BasicStroke(1f));
    g.setColor(C_DIVIDER);
    g.drawLine(90, colY + 8, 430, colY + 8);
    g.drawLine(570, colY + 8, 910, colY + 8);

    g.setColor(C_SEPARATOR);
    g.drawLine(500, colY - 10, 500, 580);

    List<GoalEvent> leftEvents = new ArrayList<>();
    List<GoalEvent> rightEvents = new ArrayList<>();
    for (GoalEvent ev : matchEvents) {
      (ev.forPlayer1 ? leftEvents : rightEvents).add(ev);
    }

    int startY = colY + 28;
    int maxRows = (570 - startY) / 26;
    drawEventColumn(g, leftEvents, startY, maxRows, true);
    drawEventColumn(g, rightEvents, startY, maxRows, false);

    drawNeonButton(g, Messages.get("end.replay"), BTN_REPLAY, new Color(0, 140, 255));
    drawNeonButton(g, Messages.get("end.menu"), BTN_MENU, new Color(180, 30, 80));
  }

  /**
   * Zeichnet eine Spalte des Tor-Protokolls, begrenzt auf {@code maxRows} Zeilen. Passt die Liste
   * nicht vollständig, werden die ältesten Einträge durch „…" ersetzt.
   *
   * @param g Grafik-Kontext
   * @param events Ereignisse dieser Spalte
   * @param startY Y-Startposition der ersten Zeile
   * @param maxRows maximale Zeilenanzahl
   * @param isLeft {@code true} für die linke Spalte (Spieler 1)
   */
  private void drawEventColumn(
      Graphics2D g, List<GoalEvent> events, int startY, int maxRows, boolean isLeft) {
    int total = events.size();
    int shown = Math.min(total, maxRows);
    boolean cut = total > maxRows;
    int fromIdx = cut ? total - (maxRows - 1) : 0;
    int yPos = startY;

    if (cut) {
      g.setFont(F_EVENT);
      g.setColor(new Color(255, 255, 255, 120));
      if (isLeft) g.drawString("…", 90, yPos);
      else g.drawString("…", 900, yPos);
      yPos += 26;
      shown = maxRows - 1;
    }

    for (int i = fromIdx; i < total && (i - fromIdx) < shown; i++) {
      GoalEvent ev = events.get(i);
      String label =
          ev.isOwnGoal ? ev.playerName + "  " + Messages.get("end.ownGoal") : ev.playerName;
      if (ev.isGoldenGoal) label += "  ⭐";
      Color entryColor = ev.isOwnGoal ? C_OWN_GOAL_C : C_WHITE;

      if (isLeft) {
        g.setFont(F_EVENT_BOLD);
        g.setColor(new Color(0, 200, 255, 160));
        g.drawString(ev.time, 90, yPos);
        g.setColor(entryColor);
        g.setFont(F_EVENT);
        g.drawString(label, 125, yPos);
      } else {
        g.setFont(F_EVENT);
        int tw = g.getFontMetrics().stringWidth(label);
        g.setColor(entryColor);
        g.drawString(label, 910 - tw - 45, yPos);
        g.setFont(F_EVENT_BOLD);
        g.setColor(new Color(0, 200, 255, 160));
        g.drawString(ev.time, 871, yPos);
      }
      yPos += 26;
    }
  }

  /**
   * Zeichnet eine zweilagige Neon-Linie (Glow-Effekt durch unterschiedliche Alpha-Werte).
   *
   * @param g Grafik-Kontext
   * @param x1 X-Start
   * @param y Y-Position der oberen Lage
   * @param x2 X-Ende
   * @param yEnd Y-Position der unteren Lage
   */
  private void drawNeonLine(Graphics2D g, int x1, int y, int x2, int yEnd) {
    g.setStroke(new BasicStroke(1f));
    g.setColor(new Color(0, 180, 255, 50));
    g.drawLine(x1, y, x2, yEnd);
    g.setColor(new Color(0, 180, 255, 120));
    g.drawLine(x1, y + 1, x2, yEnd + 1);
  }

  /**
   * Zeichnet einen stilisierten Neon-Button mit Glow, Glasschein und Schatten-Text.
   *
   * @param g Grafik-Kontext
   * @param text Beschriftung
   * @param r Position und Größe
   * @param accent Akzentfarbe für Rahmen und Glow
   */
  private void drawNeonButton(Graphics2D g, String text, Rectangle r, Color accent) {
    g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
    g.fillRoundRect(r.x - 4, r.y - 4, r.width + 8, r.height + 8, 18, 18);

    g.setColor(
        new Color(accent.getRed() / 5, accent.getGreen() / 5, accent.getBlue() / 5 + 10, 230));
    g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

    g.setColor(new Color(255, 255, 255, 25));
    g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height / 2, 12, 12);

    g.setStroke(new BasicStroke(2f));
    g.setColor(accent);
    g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

    g.setFont(F_BTN);
    int tw = g.getFontMetrics().stringWidth(text);
    g.setColor(new Color(0, 0, 0, 120));
    g.drawString(text, r.x + (r.width - tw) / 2 + 1, r.y + 36);
    g.setColor(C_WHITE);
    g.drawString(text, r.x + (r.width - tw) / 2, r.y + 35);
  }

  /**
   * Zeichnet einen String horizontal zentriert um den angegebenen X-Mittelpunkt.
   *
   * @param g Grafik-Kontext
   * @param s Text
   * @param cx X-Mittelpunkt
   * @param y Baseline-Y
   */
  private void centerString(Graphics g, String s, int cx, int y) {
    g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2, y);
  }

  /**
   * Formatiert einen Sekundenwert als {@code MM:SS}.
   *
   * @param totalSec Gesamtanzahl der Sekunden
   * @return formatierter Zeitstring
   */
  private static String formatTime(int totalSec) {
    return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
  }

  /**
   * Wertet Mausklicks auf dem Endbildschirm aus (Neustart / Menü). Die Klick-Koordinaten werden um
   * Letterbox-Versatz und Skalierung korrigiert.
   *
   * @param e Maus-Ereignis
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (state != MatchState.ENDED) return;
    int mx = (int) ((e.getX() - offsetX) / scale);
    int my = (int) ((e.getY() - offsetY) / scale);
    if (BTN_REPLAY.contains(mx, my)) {
      restartGame();
    } else if (BTN_MENU.contains(mx, my)) {
      parentFrame.returnToMenu();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  /**
   * Setzt den Steuerzustand von Spieler 1 aus der externen (mobilen) Panel-Steuerung. Wird bewusst
   * <em>nicht</em> durch Spielzustände blockiert: Auch das Loslassen einer Taste während Tor-Feier
   * oder Spielende muss den {@link InputManager} erreichen, sonst bleibt die Eingabe hängen.
   *
   * @param left {@code true}, solange „links" gehalten wird
   * @param right {@code true}, solange „rechts" gehalten wird
   * @param jump {@code true}, solange „springen" gehalten wird
   * @param kick {@code true}, solange „schießen" gehalten wird
   */
  public void setP1MobileInput(boolean left, boolean right, boolean jump, boolean kick) {
    inputManager.setP1MoveLeftFromMobile(left);
    inputManager.setP1MoveRightFromMobile(right);
    inputManager.setP1JumpFromMobile(jump);
    inputManager.setP1KickFromMobile(kick);
  }

  /**
   * Setzt den Steuerzustand von Spieler 2 aus der externen (mobilen) Panel-Steuerung. Wird bewusst
   * <em>nicht</em> durch Spielzustände blockiert (siehe {@link #setP1MobileInput}).
   *
   * @param left {@code true}, solange „links" gehalten wird
   * @param right {@code true}, solange „rechts" gehalten wird
   * @param jump {@code true}, solange „springen" gehalten wird
   * @param kick {@code true}, solange „schießen" gehalten wird
   */
  public void setP2MobileInput(boolean left, boolean right, boolean jump, boolean kick) {
    inputManager.setP2MoveLeftFromMobile(left);
    inputManager.setP2MoveRightFromMobile(right);
    inputManager.setP2JumpFromMobile(jump);
    inputManager.setP2KickFromMobile(kick);
  }
}
