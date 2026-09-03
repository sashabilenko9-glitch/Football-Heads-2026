package ui;

import com.footballheads.GameFrame;
import com.footballheads.GamePanel;
import java.awt.*;
import javax.swing.*;

/**
 * Schmale Button-Leiste oben rechts im Spielfenster. Bietet Schnellzugriff auf Neustart, Hauptmenü
 * und das Ein-/Ausblenden der Bildschirm-Tasten während des laufenden Spiels.
 */
public final class InGameUI extends JPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Erstellt die UI-Leiste und verknüpft die Buttons mit den entsprechenden Aktionen.
   *
   * @param gamePanel aktives Spielpanel
   * @param frame übergeordnetes Fenster
   * @param controlsOverlay Bildschirm-Tasten-Overlay (für den TASTEN-Umschalter); {@code null}
   *     blendet den Umschalter aus
   */
  public InGameUI(GamePanel gamePanel, GameFrame frame, MobileControlsPanel controlsOverlay) {
    setOpaque(false);
    setLayout(new FlowLayout(FlowLayout.RIGHT, 16, 10));

    if (controlsOverlay != null) {
      add(
          createNeonButton(
              "TASTEN",
              new Color(0, 110, 170),
              e -> controlsOverlay.setVisible(!controlsOverlay.isVisible())));
    }

    add(createNeonButton("RESTART", new Color(0, 160, 255), e -> gamePanel.restartGame()));

    add(createNeonButton("MENÜ", new Color(180, 30, 70), e -> frame.returnToMenu()));
  }

  /**
   * Erstellt einen stilisierten Neon-Button mit benutzerdefiniertem Rendering.
   *
   * @param text Beschriftung
   * @param accent Akzentfarbe für Rahmen und Glow
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
                    accent.getRed() / 7, accent.getGreen() / 7, accent.getBlue() / 7 + 5, 220));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 10, 10);

            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(accent);
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawString(getText(), tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), tx, ty);
          }
        };
    btn.setFont(new Font("Arial", Font.BOLD, 17));
    btn.setPreferredSize(new Dimension(130, 42));
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.addActionListener(action);
    return btn;
  }
}
