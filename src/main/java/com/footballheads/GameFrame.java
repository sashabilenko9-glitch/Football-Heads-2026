package com.footballheads;

import entities.Team;
import java.awt.*;
import javax.swing.*;
import ui.InGameUI;
import ui.MobileControlsPanel;

/**
 * Spielfenster: kombiniert {@link GamePanel} (Spielfeld), {@link InGameUI} (Steuerleiste) und
 * {@link MobileControlsPanel} (Bildschirm-Tasten-Overlay).
 */
public final class GameFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  /**
   * Erstellt das Spielfenster mit den angegebenen Teams und startet das Spiel.
   *
   * @param team1 Team des ersten Spielers
   * @param team2 Team des zweiten Spielers
   */
  public GameFrame(Team team1, Team team2) {
    setTitle("Kopf-Fußball 2026");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setUndecorated(false);

    GamePanel gamePanel = new GamePanel(this, team1, team2);
    add(gamePanel);

    // Overlay mit den mobilen Bildschirm-Tasten auf der Palette-Ebene,
    // damit es über dem Spielfeld liegt, ohne das Layout zu beeinflussen.
    MobileControlsPanel mcp = new MobileControlsPanel(gamePanel);

    InGameUI ui = new InGameUI(gamePanel, this, mcp);
    add(ui, BorderLayout.NORTH);

    JLayeredPane lp = getLayeredPane();
    lp.add(mcp, JLayeredPane.PALETTE_LAYER);

    // Overlay-Größe an der LayeredPane (Inhaltsfläche ohne Fensterrahmen und
    // Titelleiste) ausrichten und mit der Fenstergröße synchron halten.
    addComponentListener(
        new java.awt.event.ComponentAdapter() {
          @Override
          public void componentResized(java.awt.event.ComponentEvent e) {
            SwingUtilities.invokeLater(() -> mcp.setBounds(0, 0, lp.getWidth(), lp.getHeight()));
          }
        });

    setVisible(true);
    SwingUtilities.invokeLater(() -> mcp.setBounds(0, 0, lp.getWidth(), lp.getHeight()));
  }

  /** Erstellt das Spielfenster mit den ersten zwei Standard-Teams. */
  public GameFrame() {
    this(Team.getAllTeams()[0], Team.getAllTeams()[1]);
  }

  /** Schließt das Spielfenster und öffnet das Hauptmenü. */
  public void returnToMenu() {
    this.dispose();
    SwingUtilities.invokeLater(
        () -> {
          JFrame menuFrame = new JFrame("Kopf-Fußball 2026");
          menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          menuFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
          menuFrame.setContentPane(new ui.MenuPanel(menuFrame));
          menuFrame.setVisible(true);
        });
  }
}
