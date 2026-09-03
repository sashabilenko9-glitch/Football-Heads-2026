package ui;

import com.footballheads.GameFrame;
import com.footballheads.Messages;
import entities.Player;
import entities.Stadium;
import entities.Team;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Locale;
import javax.swing.*;
import utils.Field;

/**
 * Team-Auswahl-Bildschirm. Zeigt für jeden Spieler einen animierten Selektor mit Vorschau-Panel und
 * Pfeil-Navigation an. Der Stadion-Hintergrund ist mit {@code 60%} Verdunklung als Backdrop
 * sichtbar. Beide Spieler müssen unterschiedliche Teams wählen, damit sie im Spiel unterscheidbar
 * sind.
 */
public final class TeamSelectPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Color C_BORDER = new Color(0, 150, 255);
  private static final Color C_BORDER2 = new Color(0, 200, 255, 55);
  private static final Color C_ACCENT = new Color(0, 200, 255);
  private static final Color C_WARN = new Color(255, 170, 40);

  private static final String FONT_ARIAL_BLACK = "Arial Black";
  private static final String FONT_ARIAL = "Arial";

  // Not designed to survive Swing serialization - see docs/adr/0005-quality-gate-tooling.md.
  private final transient Stadium bgStadium;
  private final TeamSelector selectorP1;
  private final TeamSelector selectorP2;
  private final JLabel warnLabel;

  /**
   * Erstellt den Team-Auswahl-Bildschirm.
   *
   * @param parentFrame übergeordnetes Fenster (wird beim Spielstart geschlossen)
   * @param onBack Aktion, die beim Klick auf "Zurück" ausgeführt wird
   */
  public TeamSelectPanel(JFrame parentFrame, Runnable onBack) {
    setLayout(new BorderLayout(0, 0));
    setOpaque(false);

    bgStadium = new Stadium();

    JLabel title = new JLabel(Messages.get("teamselect.title"), JLabel.CENTER);
    title.setFont(new Font(FONT_ARIAL_BLACK, Font.BOLD, 52));
    title.setForeground(Color.WHITE);
    title.setBorder(BorderFactory.createEmptyBorder(24, 0, 14, 0));
    add(title, BorderLayout.NORTH);

    JPanel center = new JPanel(new GridLayout(1, 2, 30, 0));
    center.setOpaque(false);
    center.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 60));

    warnLabel = new JLabel(" ", JLabel.CENTER);
    warnLabel.setFont(new Font(FONT_ARIAL, Font.BOLD, 20));
    warnLabel.setForeground(C_WARN);

    selectorP1 = new TeamSelector(Messages.get("teamselect.player1"), true);
    selectorP2 = new TeamSelector(Messages.get("teamselect.player2"), false);
    selectorP2.nextTeam();

    center.add(selectorP1);
    center.add(selectorP2);
    add(center, BorderLayout.CENTER);

    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 12));
    buttonRow.setOpaque(false);
    buttonRow.add(
        createNeonButton(
            Messages.get("teamselect.start"), new Color(0, 140, 255), e -> startGame(parentFrame)));
    buttonRow.add(
        createNeonButton(
            Messages.get("teamselect.back"), new Color(140, 20, 55), e -> onBack.run()));

    JPanel bottom = new JPanel();
    bottom.setOpaque(false);
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
    warnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
    bottom.add(warnLabel);
    bottom.add(buttonRow);
    add(bottom, BorderLayout.SOUTH);
  }

  /**
   * Startet das Spiel, sofern beide Spieler unterschiedliche Teams gewählt haben. Bei identischer
   * Auswahl wird stattdessen ein Hinweis angezeigt.
   *
   * @param parentFrame Menü-Fenster, das beim Start geschlossen wird
   */
  private void startGame(JFrame parentFrame) {
    Team t1 = selectorP1.getSelectedTeam();
    Team t2 = selectorP2.getSelectedTeam();
    if (t1.name.equals(t2.name)) {
      warnLabel.setText(Messages.get("teamselect.sameTeamWarning"));
      return;
    }
    if (parentFrame != null) parentFrame.dispose();
    new GameFrame(t1, t2);
  }

  /** Entfernt den Gleiches-Team-Hinweis (nach einem Team-Wechsel). */
  private void clearWarning() {
    warnLabel.setText(" ");
  }

  /**
   * Zeichnet den skalierten Stadion-Hintergrund mit halbtransparenter Verdunklung.
   *
   * @param g Grafik-Kontext
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double sx = getWidth() / 1000.0;
    double sy = getHeight() / 700.0;
    AffineTransform old = g2.getTransform();
    g2.scale(sx, sy);
    bgStadium.drawBackgroundOnly(g2);
    g2.setTransform(old);

    g2.setColor(new Color(3, 6, 22, 165));
    g2.fillRect(0, 0, getWidth(), getHeight());
  }

  /**
   * Erstellt einen stilisierten Neon-Button.
   *
   * @param text Beschriftung
   * @param accent Akzentfarbe
   * @param action Aktion bei Klick
   * @return fertiger Button
   */
  private JButton createNeonButton(
      String text, Color accent, java.awt.event.ActionListener action) {
    JButton btn =
        new JButton(text) {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(
                new Color(
                    accent.getRed() / 6, accent.getGreen() / 6, accent.getBlue() / 6 + 6, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 12, 12);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(accent);
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(new Color(0, 0, 0, 90));
            g2.drawString(getText(), tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), tx, ty);
          }
        };
    btn.setFont(new Font(FONT_ARIAL, Font.BOLD, 26));
    btn.setPreferredSize(new Dimension(280, 62));
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.addActionListener(action);
    return btn;
  }

  // =========================================================================

  /**
   * Team-Selektor für einen Spieler: zeigt Teamname, animierte Vorschau und Pfeil-Buttons zur
   * Navigation.
   */
  private final class TeamSelector extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Team[] teams;
    private int currentIndex = 0;
    private final boolean isPlayerOne;

    private final JLabel nameLabel;
    private final PlayerPreviewPanel previewPanel;
    private final JButton leftBtn;
    private final JButton rightBtn;

    /**
     * Erstellt den Selektor für den angegebenen Spieler.
     *
     * @param headerText Kopfzeile (Spielername + Tasten)
     * @param isPlayerOne {@code true} für Spieler 1
     */
    TeamSelector(String headerText, boolean isPlayerOne) {
      this.teams = Team.getAllTeams();
      this.isPlayerOne = isPlayerOne;
      setOpaque(false);
      setLayout(new BorderLayout(0, 6));

      JLabel header = new JLabel(headerText, JLabel.CENTER);
      header.setFont(new Font(FONT_ARIAL, Font.BOLD, 18));
      header.setForeground(C_ACCENT);
      header.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
      add(header, BorderLayout.NORTH);

      JPanel card =
          new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g;
              g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              g2.setColor(new Color(5, 12, 40, 210));
              g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
              g2.setStroke(new BasicStroke(1.5f));
              g2.setColor(C_BORDER2);
              g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 18, 18);
              g2.setStroke(new BasicStroke(2.2f));
              g2.setColor(C_BORDER);
              g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
          };
      card.setOpaque(false);
      card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel nameBg =
          new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g;
              g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              g2.setColor(new Color(0, 80, 180, 130));
              g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
              g2.setStroke(new BasicStroke(1.5f));
              g2.setColor(new Color(0, 160, 255, 160));
              g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            }
          };
      nameBg.setOpaque(false);
      nameBg.setPreferredSize(new Dimension(0, 48));

      nameLabel = new JLabel("", JLabel.CENTER);
      nameLabel.setFont(new Font(FONT_ARIAL_BLACK, Font.BOLD, 20));
      nameLabel.setForeground(Color.WHITE);
      nameLabel.setOpaque(false);
      nameBg.add(nameLabel, BorderLayout.CENTER);
      card.add(nameBg, BorderLayout.NORTH);

      previewPanel = new PlayerPreviewPanel();
      card.add(previewPanel, BorderLayout.CENTER);

      JPanel arrowRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
      arrowRow.setOpaque(false);
      leftBtn = createArrowButton("<");
      rightBtn = createArrowButton(">");
      leftBtn.addActionListener(e -> changeTeam(-1));
      rightBtn.addActionListener(e -> changeTeam(+1));
      arrowRow.add(leftBtn);
      arrowRow.add(rightBtn);
      card.add(arrowRow, BorderLayout.SOUTH);

      add(card, BorderLayout.CENTER);
      refreshUI();
    }

    /**
     * Wechselt das ausgewählte Team um {@code dir} Schritte und entfernt einen eventuell
     * angezeigten Gleiches-Team-Hinweis.
     *
     * @param dir Richtung: {@code -1} zurück, {@code +1} vorwärts
     */
    private void changeTeam(int dir) {
      currentIndex += dir;
      clearWarning();
      refreshUI();
    }

    /** Wählt das nächste Team (wird beim Start für Spieler 2 aufgerufen). */
    public void nextTeam() {
      changeTeam(+1);
    }

    /**
     * Gibt das aktuell gewählte Team zurück.
     *
     * @return ausgewähltes Team
     */
    public Team getSelectedTeam() {
      return teams[currentIndex];
    }

    /** Aktualisiert Label, Vorschau und Button-Status nach einem Team-Wechsel. */
    private void refreshUI() {
      if (nameLabel == null || previewPanel == null) return;
      nameLabel.setText(teams[currentIndex].name.toUpperCase(Locale.ROOT));
      nameLabel.repaint();
      previewPanel.setTeam(teams[currentIndex]);
      leftBtn.setEnabled(currentIndex > 0);
      rightBtn.setEnabled(currentIndex < teams.length - 1);
      leftBtn.repaint();
      rightBtn.repaint();
      revalidate();
      repaint();
    }

    /**
     * Erstellt einen stilisierten Pfeil-Button.
     *
     * @param symbol Button-Symbol ({@code "<"} oder {@code ">"})
     * @return fertiger Button
     */
    private JButton createArrowButton(String symbol) {
      JButton btn =
          new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g;
              g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              Color col = isEnabled() ? C_BORDER : new Color(50, 60, 80);
              g2.setColor(
                  new Color(col.getRed() / 8, col.getGreen() / 8, col.getBlue() / 8 + 4, 210));
              g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
              g2.setStroke(new BasicStroke(1.8f));
              g2.setColor(col);
              g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
              g2.setFont(getFont());
              FontMetrics fm = g2.getFontMetrics();
              int tx = (getWidth() - fm.stringWidth(getText())) / 2;
              int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
              g2.setColor(isEnabled() ? Color.WHITE : new Color(70, 80, 100));
              g2.drawString(getText(), tx, ty);
            }
          };
      btn.setFont(new Font(FONT_ARIAL, Font.BOLD, 20));
      btn.setPreferredSize(new Dimension(70, 42));
      btn.setContentAreaFilled(false);
      btn.setBorderPainted(false);
      btn.setFocusPainted(false);
      btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      return btn;
    }

    // =====================================================================

    /**
     * Animiertes Vorschau-Panel: zeichnet die Spielerfigur mit Idle-Bob-Animation. Der Spieler wird
     * passend zur Panelhöhe skaliert und vertikal zentriert. Der Animations-Timer läuft nur,
     * solange das Panel angezeigt wird ({@link #addNotify} / {@link #removeNotify}).
     */
    private final class PlayerPreviewPanel extends JPanel {

      private static final long serialVersionUID = 1L;

      // Not designed to survive Swing serialization - see docs/adr/0005-quality-gate-tooling.md.
      private transient Player dummyPlayer;
      private double animTime = 0.0;
      private final Timer animTimer;

      /** Maximale Skalierung der Spielerfigur. */
      private static final double MAX_PLAYER_SCALE = 2.1;

      /** Gesamthöhe des Spieler-Sprites in Spielkoordinaten (Kopf bis Sohlen). */
      private static final double SPRITE_HEIGHT = 150.0;

      /** Erstellt das Vorschau-Panel. */
      PlayerPreviewPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 260));
        dummyPlayer = new Player(0, isPlayerOne, teams[0]);

        animTimer =
            new Timer(
                16,
                e -> {
                  animTime += 0.04;
                  repaint();
                });
      }

      /** Startet den Animations-Timer, sobald das Panel angezeigt wird. */
      @Override
      public void addNotify() {
        super.addNotify();
        animTimer.start();
      }

      /** Stoppt den Animations-Timer, wenn das Panel entfernt wird – verhindert Timer-Leaks. */
      @Override
      public void removeNotify() {
        animTimer.stop();
        super.removeNotify();
      }

      /**
       * Setzt das darzustellende Team.
       *
       * @param t neues Team
       */
      public void setTeam(Team t) {
        dummyPlayer = new Player(0, isPlayerOne, t);
        repaint();
      }

      /**
       * Zeichnet den Spieler mit Spotlight, Bodenschatten, Bob- und Tilt-Animation.
       *
       * @param g Grafik-Kontext
       */
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        RadialGradientPaint spotlight =
            new RadialGradientPaint(
                w / 2f,
                h * 0.88f,
                w * 0.42f,
                new float[] {0f, 1f},
                new Color[] {new Color(0, 130, 255, 55), new Color(0, 0, 0, 0)});
        g2.setPaint(spotlight);
        g2.fillRect(0, 0, w, h);

        double bobY = Math.sin(animTime) * 5.0;
        double tiltR = Math.sin(animTime * 0.7) * 0.04;

        // Skalierung so wählen, dass der Sprite (inkl. Bob) in das Panel passt
        double playerScale = Math.min(MAX_PLAYER_SCALE, (h - 24) / SPRITE_HEIGHT);
        double spriteH = SPRITE_HEIGHT * playerScale;
        double feetPanelY = h - (h - spriteH) / 2.0 - 8;
        double cx = w / 2.0;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval((int) (cx - 40), (int) (feetPanelY - 6), 80, 14);

        AffineTransform old = g2.getTransform();

        g2.translate(cx, feetPanelY + bobY);
        g2.scale(playerScale, playerScale);
        g2.rotate(tiltR, 0, -75);
        g2.translate(0, -Field.GROUND_Y);

        entities.Ball dummyBall = new entities.Ball();
        dummyBall.x = (isPlayerOne ? 1 : -1) * 60 / playerScale;
        dummyBall.y = Field.GROUND_Y - 120;

        dummyPlayer.x = 0;
        dummyPlayer.y = Field.GROUND_Y;
        dummyPlayer.draw(g2, dummyBall);

        g2.setTransform(old);
      }
    }
  }
}
