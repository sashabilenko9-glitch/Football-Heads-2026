package ui;

import com.footballheads.GamePanel;
import com.footballheads.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Halbtransparentes Pause-Overlay mit den Optionen "Weiter", "Neustart" und "Hauptmenü".
 * <p>
 * Das Panel ist {@code non-opaque} und zeichnet seinen transparenten Hintergrund
 * selbst in {@link #paintComponent} – ein opakes Panel mit Alpha-Hintergrundfarbe
 * würde bei Teil-Repaints (z.&nbsp;B. Button-Hover) Schlieren hinterlassen.
 */
public class PauseMenu extends JPanel {

    private static final Color C_OVERLAY_BG = new Color(2, 5, 20, 200);

    /**
     * Erstellt das Pause-Menü und verknüpft die Buttons mit ihren Aktionen.
     *
     * @param gamePanel aktives Spielpanel
     * @param frame     übergeordnetes Fenster
     */
    public PauseMenu(GamePanel gamePanel, GameFrame frame) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets    = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("PAUSE");
        title.setFont(new Font("Arial Black", Font.BOLD, 90));
        title.setForeground(new Color(0, 210, 255));
        add(title, gbc);

        add(Box.createVerticalStrut(8), gbc);

        add(createNeonButton("WEITER",    new Color(0, 160, 255),
                e -> gamePanel.togglePause()), gbc);

        // restartGame() hebt die Pause selbst auf und blendet dieses Menü aus –
        // ein zusätzliches togglePause() würde die Pause sofort wieder aktivieren.
        add(createNeonButton("NEUSTART",  new Color(0, 100, 200),
                e -> gamePanel.restartGame()), gbc);

        add(createNeonButton("HAUPTMENÜ", new Color(140, 20, 55),
                e -> frame.returnToMenu()), gbc);
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
     * Erstellt einen stilisierten Neon-Button mit benutzerdefiniertem Rendering.
     *
     * @param text   Beschriftung
     * @param accent Akzentfarbe für Rahmen und Glow
     * @param action Aktion bei Klick
     * @return fertiger Button
     */
    private JButton createNeonButton(String text, Color accent, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fillRoundRect(-4, -4, getWidth() + 8, getHeight() + 8, 18, 18);

                g2.setColor(new Color(accent.getRed() / 7, accent.getGreen() / 7,
                                      accent.getBlue() / 7 + 5, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 12, 12);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(accent);
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 34));
        btn.setPreferredSize(new Dimension(380, 80));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }
}
