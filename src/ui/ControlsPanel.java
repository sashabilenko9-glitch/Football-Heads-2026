package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel, das die Tastenbelegung beider Spieler als Overlay anzeigt.
 * <p>
 * Das Panel ist {@code non-opaque} und zeichnet seinen transparenten Hintergrund
 * selbst in {@link #paintComponent}, um Repaint-Schlieren zu vermeiden.
 */
public class ControlsPanel extends JPanel {

    private static final Color C_OVERLAY_BG = new Color(0, 0, 0, 200);

    /**
     * Erstellt das Steuerungs-Panel.
     *
     * @param onClose Aktion, die beim Klick auf "Schließen" ausgeführt wird
     */
    public ControlsPanel(Runnable onClose) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets    = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("STEUERUNG", JLabel.CENTER);
        title.setFont(new Font("Arial Black", Font.BOLD, 70));
        title.setForeground(Color.YELLOW);
        add(title, gbc);

        addLabel("Spieler 1 (links):", Color.WHITE,   40, gbc);
        addLabel("Bewegung — A / D",   Color.CYAN,    36, gbc);
        addLabel("Sprung — W",         Color.CYAN,    36, gbc);
        addLabel("Schießen — S",       Color.CYAN,    36, gbc);

        add(Box.createVerticalStrut(16), gbc);

        addLabel("Spieler 2 (rechts):", Color.WHITE,    40, gbc);
        addLabel("Bewegung — ← →",     Color.MAGENTA,  36, gbc);
        addLabel("Sprung — ↑",         Color.MAGENTA,  36, gbc);
        addLabel("Schießen — ↓",       Color.MAGENTA,  36, gbc);

        add(Box.createVerticalStrut(16), gbc);

        addLabel("ESC — Pause",                                        Color.LIGHT_GRAY, 26, gbc);
        addLabel("Bildschirm-Tasten: per Maus (eine Taste gleichzeitig)", Color.LIGHT_GRAY, 26, gbc);

        add(Box.createVerticalStrut(28), gbc);

        JButton close = new JButton("SCHLIESSEN");
        close.setFont(new Font("Arial", Font.BOLD, 40));
        close.setBackground(Color.RED.darker());
        close.setForeground(Color.WHITE);
        close.setPreferredSize(new Dimension(320, 80));
        close.setFocusPainted(false);
        close.addActionListener(e -> onClose.run());
        add(close, gbc);
    }

    /**
     * Zeichnet den halbtransparenten Overlay-Hintergrund.
     *
     * @param g Grafik-Kontext
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(C_OVERLAY_BG);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Erstellt ein zentriertes Label mit der angegebenen Schriftgröße und Farbe und fügt es hinzu.
     *
     * @param text  Beschriftungstext
     * @param color Schriftfarbe
     * @param size  Schriftgröße in Punkten
     * @param gbc   Layout-Constraints
     */
    private void addLabel(String text, Color color, int size, GridBagConstraints gbc) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(color);
        add(label, gbc);
    }
}
