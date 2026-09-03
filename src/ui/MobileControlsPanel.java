package ui;

import com.footballheads.GamePanel;
import com.footballheads.InputManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Transparentes Overlay-Panel mit Bildschirm-Tasten: WASD (Spieler 1) und Pfeiltasten (Spieler 2).
 * <p>
 * Entwurfsentscheidungen:
 * <ul>
 *   <li>{@link #p1Active} / {@link #p2Active} verfolgen ausschließlich die Maus
 *       (für {@code setP1MobileInput} / {@code setP2MobileInput}).</li>
 *   <li>{@link #paintComponent} liest den visuellen Zustand aus dem {@link InputManager}
 *       (Tastatur ODER Maus), sodass beide Eingabequellen angezeigt werden.</li>
 *   <li>Ein eigener 16-ms-Timer zeichnet das Panel unabhängig vom {@code GamePanel} neu.
 *       Er läuft nur, solange das Panel angezeigt wird ({@link #addNotify} /
 *       {@link #removeNotify}), damit geschlossene Spielfenster keine Timer hinterlassen.</li>
 *   <li>Während Pause und Endbildschirm verschwinden die Tasten und lassen
 *       Mausklicks durch ({@link #contains} liefert {@code false}).</li>
 * </ul>
 * Hinweis: Da nur ein Mauszeiger existiert, kann per Maus immer nur eine Taste
 * gleichzeitig gehalten werden – die Panels dienen primär als Anzeige und
 * Gelegenheits-Steuerung, die Tastatur bleibt die Hauptsteuerung.
 */
public class MobileControlsPanel extends JPanel {

    /** Kantenlänge einer Bildschirm-Taste in Pixeln. */
    private static final int BTN        = 54;

    /** Abstand zwischen benachbarten Tasten in Pixeln. */
    private static final int GAP        = 7;

    /** Seitlicher Abstand des Tastenclusters zum Fensterrand. */
    private static final int PAD_SIDE   = 28;

    /** Unterer Abstand des Tastenclusters zum Fensterrand. */
    private static final int PAD_BOTTOM = 65;

    /** Nur Maus-Zustand – welche Mobile-Tasten von Spieler 1 gerade gehalten werden. */
    private final boolean[] p1Active = new boolean[4];

    /** Nur Maus-Zustand – welche Mobile-Tasten von Spieler 2 gerade gehalten werden. */
    private final boolean[] p2Active = new boolean[4];

    private final GamePanel    gamePanel;
    private final InputManager inputManager;
    private final Timer        repaintTimer;

    /**
     * Erstellt das Overlay und registriert die Maus-Listener.
     * Der Repaint-Timer wird erst in {@link #addNotify} gestartet.
     *
     * @param gamePanel Spielpanel, an das die Eingaben weitergereicht werden
     */
    public MobileControlsPanel(GamePanel gamePanel) {
        this.gamePanel    = gamePanel;
        this.inputManager = gamePanel.getInputManager();
        this.repaintTimer = new Timer(16, e -> repaint());

        setOpaque(false);
        setFocusable(false);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) { handle(e, true);  }
            @Override public void mouseReleased(MouseEvent e) { handle(e, false); }
            @Override public void mouseDragged (MouseEvent e) { handle(e, true);  }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /**
     * Startet den Repaint-Timer, sobald das Panel Teil einer angezeigten Hierarchie wird.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        repaintTimer.start();
    }

    /**
     * Stoppt den Repaint-Timer, wenn das Panel aus der Hierarchie entfernt wird
     * (z.&nbsp;B. beim Schließen des Spielfensters) – verhindert Timer-Leaks.
     */
    @Override
    public void removeNotify() {
        repaintTimer.stop();
        super.removeNotify();
    }

    /**
     * @return {@code true}, wenn die Tasten aktuell ausgeblendet sind
     *         (Pause oder Endbildschirm)
     */
    private boolean hiddenByGameState() {
        return gamePanel.isPaused() || gamePanel.isMatchEnded();
    }

    /**
     * Beschränkt die Treffer-Fläche des Panels auf die Tastenbereiche,
     * damit Mausklicks außerhalb der Tasten an das Spielfeld durchgereicht werden.
     * Im ausgeblendeten Zustand gehen alle Klicks durch.
     *
     * @param x X-Koordinate in Panel-Koordinaten
     * @param y Y-Koordinate in Panel-Koordinaten
     * @return {@code true}, wenn der Punkt eine der Tasten trifft
     */
    @Override
    public boolean contains(int x, int y) {
        if (hiddenByGameState()) return false;
        Point p = new Point(x, y);
        for (int i = 0; i < 4; i++) {
            if (hitBox(true, i).contains(p) || hitBox(false, i).contains(p)) return true;
        }
        return false;
    }

    /**
     * Liefert das Rechteck einer Bildschirm-Taste.
     * Index-Belegung: {@code 0} = springen, {@code 1} = links, {@code 2} = schießen, {@code 3} = rechts.
     *
     * @param leftCluster {@code true} für den linken Cluster (Spieler 1), sonst rechter Cluster
     * @param idx         Tastenindex (0–3)
     * @return Position und Größe der Taste in Panel-Koordinaten
     */
    private Rectangle btn(boolean leftCluster, int idx) {
        int w  = getWidth(),  h  = getHeight();
        int bx = leftCluster ? PAD_SIDE : (w - PAD_SIDE - 3 * BTN - 2 * GAP);
        int by = h - PAD_BOTTOM - 2 * BTN - GAP;
        int col = (idx == 1) ? 0 : (idx == 3) ? 2 : 1;
        int row = (idx == 0) ? 0 : 1;
        return new Rectangle(bx + col * (BTN + GAP), by + row * (BTN + GAP), BTN, BTN);
    }

    /**
     * Liefert die um 15 Pixel vergrößerte Treffer-Fläche einer Taste,
     * um die Bedienung mit der Maus zu erleichtern.
     *
     * @param leftCluster {@code true} für den linken Cluster (Spieler 1), sonst rechter Cluster
     * @param idx         Tastenindex (0–3)
     * @return vergrößertes Treffer-Rechteck
     */
    private Rectangle hitBox(boolean leftCluster, int idx) {
        Rectangle r = btn(leftCluster, idx);
        return new Rectangle(r.x - 15, r.y - 15, r.width + 30, r.height + 30);
    }

    /**
     * Wertet ein Maus-Ereignis aus und überträgt den neuen Tastenzustand an das Spielpanel.
     * Auch das Loslassen wird immer weitergereicht, damit keine Eingabe hängen bleibt.
     *
     * @param e  Maus-Ereignis
     * @param on {@code true} bei gedrückter/gezogener Maus, {@code false} beim Loslassen
     */
    private void handle(MouseEvent e, boolean on) {
        java.util.Arrays.fill(p1Active, false);
        java.util.Arrays.fill(p2Active, false);
        if (on) {
            Point p = e.getPoint();
            for (int i = 0; i < 4; i++) {
                p1Active[i] = hitBox(true,  i).contains(p);
                p2Active[i] = hitBox(false, i).contains(p);
            }
        }
        // Index: [0] = springen, [1] = links, [2] = schießen, [3] = rechts
        gamePanel.setP1MobileInput(p1Active[1], p1Active[3], p1Active[0], p1Active[2]);
        gamePanel.setP2MobileInput(p2Active[1], p2Active[3], p2Active[0], p2Active[2]);
    }

    /**
     * Zeichnet beide Tastencluster.
     * Der Gedrückt-Zustand wird aus dem {@link InputManager} gelesen,
     * sodass Tastatur- und Mauseingaben gleichermaßen sichtbar sind.
     * Während Pause und Endbildschirm wird nichts gezeichnet.
     *
     * @param g Grafik-Kontext
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getWidth() == 0 || hiddenByGameState()) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] lbl1 = {"W", "A", "S", "D"};
        String[] lbl2 = {"↑", "←", "↓", "→"};

        // Index: [0] = springen, [1] = links, [2] = schießen, [3] = rechts
        boolean[] p1Display = {
            inputManager.getP1Jump(),
            inputManager.getP1Left(),
            inputManager.getP1Kick(),
            inputManager.getP1Right()
        };
        boolean[] p2Display = {
            inputManager.getP2Jump(),
            inputManager.getP2Left(),
            inputManager.getP2Kick(),
            inputManager.getP2Right()
        };

        for (int i = 0; i < 4; i++) {
            drawKey(g2, btn(true,  i), lbl1[i], p1Display[i]);
            drawKey(g2, btn(false, i), lbl2[i], p2Display[i]);
        }
    }

    /**
     * Zeichnet eine einzelne Bildschirm-Taste im Glas-Stil.
     * Im gedrückten Zustand „sinkt" die Taste nach unten und leuchtet cyan.
     *
     * @param g       Grafik-Kontext
     * @param r       Position und Größe der Taste
     * @param label   Beschriftung
     * @param pressed {@code true}, wenn die Taste gerade gedrückt ist
     */
    private void drawKey(Graphics2D g, Rectangle r, String label, boolean pressed) {
        int lift = pressed ? 0 : 3;
        int arc  = 10;

        // Schatten (entfällt im gedrückten Zustand – die Taste „sinkt" nach unten)
        if (!pressed) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(r.x + 2, r.y + 4, r.width, r.height, arc, arc);
        }

        // Vorderseite
        g.setColor(pressed
            ? new Color(0, 180, 255, 140)
            : new Color(0, 0, 0, 90));
        g.fillRoundRect(r.x, r.y + lift, r.width, r.height, arc, arc);

        // Rahmen
        g.setStroke(new BasicStroke(pressed ? 2.5f : 1.8f));
        g.setColor(pressed
            ? new Color(0, 220, 255, 255)
            : new Color(255, 255, 255, 170));
        g.drawRoundRect(r.x, r.y + lift, r.width, r.height, arc, arc);

        // Glanzlicht oben – nur im losgelassenen Zustand
        if (!pressed) {
            g.setColor(new Color(255, 255, 255, 25));
            g.fillRoundRect(r.x + 3, r.y + lift + 3, r.width - 6, r.height / 3, arc, arc);
        }

        // Beschriftung
        g.setFont(new Font("Arial Black", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width  - fm.stringWidth(label)) / 2;
        int ty = r.y + lift + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(label, tx + 1, ty + 1);
        g.setColor(pressed ? new Color(0, 230, 255) : Color.WHITE);
        g.drawString(label, tx, ty);
    }
}
