package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Hauptmenü der Anwendung, verwaltet über {@link CardLayout}.
 * <p>
 * Enthält drei Karten: Hauptmenü ({@code "MENU"}), Steuerung ({@code "CONTROLS"})
 * und Team-Auswahl ({@code "TEAM_SELECT"}).
 * Der Wechsel zwischen ihnen erfolgt ohne neues Fenster.
 * Der Animations-Timer läuft nur, solange das Panel angezeigt wird
 * ({@link #addNotify} / {@link #removeNotify}), damit geschlossene Menü-Fenster
 * keine Timer hinterlassen.
 */
public class MenuPanel extends JPanel {

    private final CardLayout      cardLayout;
    private final JPanel          menuContent;
    private final ControlsPanel   controlsPanel;
    private final TeamSelectPanel teamSelectPanel;
    private final Timer           pulseTimer;

    /** Fortlaufender Animationswert für das pulsierende Beta-Abzeichen. */
    private float starPulse = 0f;

    private static final Color C_BLUE  = new Color(0, 140, 255);
    private static final Color C_CYAN  = new Color(0, 210, 255);
    private static final Color C_WHITE = Color.WHITE;

    /**
     * Erstellt das Menü-Panel und alle Unter-Panels.
     *
     * @param parentFrame übergeordnetes Fenster (wird an {@link TeamSelectPanel} weitergegeben)
     */
    public MenuPanel(JFrame parentFrame) {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        menuContent = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint bg = new GradientPaint(
                    0, 0, new Color(4, 6, 22), 0, getHeight(), new Color(8, 20, 55));
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(0, 100, 200, 18));
                g2.setStroke(new BasicStroke(1f));
                for (int x = 0; x < getWidth();  x += 60) g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 60) g2.drawLine(0, y, getWidth(), y);

                RadialGradientPaint glow = new RadialGradientPaint(
                    getWidth() / 2f, getHeight() / 2f, getHeight() / 2f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 60, 180, 60), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(glow);
                g2.fillRect(0, 0, getWidth(), getHeight());
                drawStarBadge(g2, getWidth() - 100, 95);
            }
        };
        menuContent.setOpaque(false);
        pulseTimer = new Timer(33, e -> {
            starPulse += 0.07f;
            menuContent.repaint();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets    = new Insets(14, 0, 14, 0);

        JLabel title = new JLabel("KOPF-FUSSBALL 2026");
        title.setFont(new Font("Arial Black", Font.BOLD, 72));
        title.setForeground(C_WHITE);
        menuContent.add(title, gbc);

        JLabel subtitle = new JLabel("2 SPIELER  ·  60 SEKUNDEN  ·  CHAOS");
        subtitle.setFont(new Font("Arial", Font.BOLD, 26));
        subtitle.setForeground(C_CYAN);
        menuContent.add(subtitle, gbc);

        menuContent.add(Box.createVerticalStrut(10), gbc);

        JButton playButton = createMenuButton("ZUM SPIEL", C_BLUE);
        playButton.addActionListener(e -> cardLayout.show(this, "TEAM_SELECT"));
        menuContent.add(playButton, gbc);

        JButton controlsButton = createMenuButton("STEUERUNG", new Color(0, 80, 160));
        controlsButton.addActionListener(e -> cardLayout.show(this, "CONTROLS"));
        menuContent.add(controlsButton, gbc);

        JButton exitButton = createMenuButton("BEENDEN", new Color(140, 20, 50));
        exitButton.addActionListener(e -> System.exit(0));
        menuContent.add(exitButton, gbc);

        controlsPanel   = new ControlsPanel(this::showMenu);
        teamSelectPanel = new TeamSelectPanel(parentFrame, this::showMenu);

        add(menuContent,     "MENU");
        add(controlsPanel,   "CONTROLS");
        add(teamSelectPanel, "TEAM_SELECT");

        cardLayout.show(this, "MENU");
    }

    /**
     * Startet den Animations-Timer, sobald das Panel Teil einer angezeigten Hierarchie wird.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        pulseTimer.start();
    }

    /**
     * Stoppt den Animations-Timer, wenn das Panel aus der Hierarchie entfernt wird
     * (z.&nbsp;B. beim Schließen des Menü-Fensters) – verhindert Timer-Leaks.
     */
    @Override
    public void removeNotify() {
        pulseTimer.stop();
        super.removeNotify();
    }

    /**
     * Zeichnet das pulsierende „Beta 0.1“-Abzeichen im Preisschild-Stil.
     *
     * @param g  Grafik-Kontext
     * @param cx X-Mittelpunkt des Sterns
     * @param cy Y-Mittelpunkt des Sterns
     */
    private void drawStarBadge(Graphics2D g, int cx, int cy) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float scale = 1.0f + 0.05f * (float) Math.sin(starPulse);
        g2.translate(cx, cy);
        g2.scale(scale, scale);

        Polygon star = createStarburst(0, 0, 62, 36, 9);

        // Roter Schatten (wirkt wie eine Rahmen-Dicke)
        g2.setColor(new Color(210, 25, 25));
        g2.translate(3, 5);
        g2.fill(star);
        g2.translate(-3, -5);

        // Gelbe Füllung
        g2.setColor(new Color(255, 218, 0));
        g2.fill(star);

        // Text "Beta"
        g2.setFont(new Font("Arial Black", Font.BOLD, 17));
        g2.setColor(new Color(55, 55, 195));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("Beta", -fm.stringWidth("Beta") / 2, -5);

        // Text "0.1"
        g2.setFont(new Font("Arial Black", Font.BOLD, 20));
        fm = g2.getFontMetrics();
        g2.drawString("0.1", -fm.stringWidth("0.1") / 2, 19);

        g2.dispose();
    }

    /**
     * Erstellt ein Starburst-Polygon (Stern mit abwechselnd langen und kurzen Strahlen).
     *
     * @param cx     X-Mittelpunkt
     * @param cy     Y-Mittelpunkt
     * @param outerR äußerer Radius (Strahlenspitzen)
     * @param innerR innerer Radius (Einbuchtungen zwischen den Strahlen)
     * @param spikes Anzahl der Strahlen
     * @return fertiges Polygon
     */
    private Polygon createStarburst(int cx, int cy, int outerR, int innerR, int spikes) {
        Polygon p = new Polygon();
        double step = Math.PI / spikes;
        for (int i = 0; i < spikes * 2; i++) {
            double angle = i * step - Math.PI / 2.0;
            int    r     = (i % 2 == 0) ? outerR : innerR;
            p.addPoint(cx + (int)(Math.cos(angle) * r),
                       cy + (int)(Math.sin(angle) * r));
        }
        return p;
    }

    /**
     * Erstellt einen stilisierten Menü-Button mit benutzerdefiniertem Rendering.
     *
     * @param text   Beschriftung
     * @param accent Akzentfarbe für Rahmen und Glow
     * @return fertiger Button
     */
    private JButton createMenuButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(accent.getRed() / 6, accent.getGreen() / 6,
                                      accent.getBlue() / 6 + 8, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 12, 12);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(accent);
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 34));
        btn.setPreferredSize(new Dimension(430, 80));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Wechselt zurück zur Hauptmenü-Karte.
     */
    private void showMenu() {
        cardLayout.show(this, "MENU");
    }
}
