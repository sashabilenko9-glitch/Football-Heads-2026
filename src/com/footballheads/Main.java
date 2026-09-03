package com.footballheads;

import ui.MenuPanel;
import javax.swing.*;

/**
 * Einstiegspunkt der Anwendung.
 * Erstellt das Hauptfenster und übergibt es an das {@link MenuPanel}.
 */
public class Main {

    /**
     * Hilfsklasse ohne Zustand – wird nicht instanziiert.
     */
    private Main() {
    }

    /**
     * Startet die Anwendung auf dem Swing-Event-Thread.
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kopf-Fußball 2026");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setContentPane(new MenuPanel(frame));
            frame.setVisible(true);
        });
    }
}
