package ui;

import com.footballheads.Messages;
import java.awt.*;
import javax.swing.*;

/**
 * Panel, das die Tastenbelegung beider Spieler als Overlay anzeigt.
 *
 * <p>Das Panel ist {@code non-opaque} und zeichnet seinen transparenten Hintergrund selbst in
 * {@link #paintComponent}, um Repaint-Schlieren zu vermeiden.
 */
public final class ControlsPanel extends JPanel {

  private static final long serialVersionUID = 1L;

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
    gbc.insets = new Insets(12, 0, 12, 0);

    JLabel title = new JLabel(Messages.get("controls.title"), JLabel.CENTER);
    title.setFont(new Font("Arial Black", Font.BOLD, 70));
    title.setForeground(Color.YELLOW);
    add(title, gbc);

    addLabel(Messages.get("controls.player1"), Color.WHITE, 40, gbc);
    addLabel(Messages.get("controls.movement1"), Color.CYAN, 36, gbc);
    addLabel(Messages.get("controls.jump1"), Color.CYAN, 36, gbc);
    addLabel(Messages.get("controls.kick1"), Color.CYAN, 36, gbc);

    add(Box.createVerticalStrut(16), gbc);

    addLabel(Messages.get("controls.player2"), Color.WHITE, 40, gbc);
    addLabel(Messages.get("controls.movement2"), Color.MAGENTA, 36, gbc);
    addLabel(Messages.get("controls.jump2"), Color.MAGENTA, 36, gbc);
    addLabel(Messages.get("controls.kick2"), Color.MAGENTA, 36, gbc);

    add(Box.createVerticalStrut(16), gbc);

    addLabel(Messages.get("controls.pause"), Color.LIGHT_GRAY, 26, gbc);
    addLabel(Messages.get("controls.mobileHint"), Color.LIGHT_GRAY, 26, gbc);

    add(Box.createVerticalStrut(28), gbc);

    JButton close = new JButton(Messages.get("controls.close"));
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
   * @param text Beschriftungstext
   * @param color Schriftfarbe
   * @param size Schriftgröße in Punkten
   * @param gbc Layout-Constraints
   */
  private void addLabel(String text, Color color, int size, GridBagConstraints gbc) {
    JLabel label = new JLabel(text, JLabel.CENTER);
    label.setFont(new Font("Arial", Font.BOLD, size));
    label.setForeground(color);
    add(label, gbc);
  }
}
