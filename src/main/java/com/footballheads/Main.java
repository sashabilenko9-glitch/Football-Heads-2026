package com.footballheads;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.MenuPanel;

/**
 * Einstiegspunkt der Anwendung. Erstellt das Hauptfenster und übergibt es an das {@link MenuPanel}.
 */
public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  /** Hilfsklasse ohne Zustand – wird nicht instanziiert. */
  private Main() {}

  /**
   * Startet die Anwendung auf dem Swing-Event-Thread.
   *
   * @param args Kommandozeilenargumente (werden nicht ausgewertet)
   */
  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, ex) -> LOG.error("Unhandled exception on thread {}", thread.getName(), ex));

    SwingUtilities.invokeLater(
        () -> {
          LOG.info("Starting Kopf-Fußball 2026");
          JFrame frame = new JFrame("Kopf-Fußball 2026");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
          frame.setContentPane(new MenuPanel(frame));
          frame.setVisible(true);
        });
  }
}
