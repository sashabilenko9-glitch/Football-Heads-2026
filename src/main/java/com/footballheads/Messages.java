package com.footballheads;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Zentraler Zugriffspunkt für lokalisierte UI-Texte.
 *
 * <p>Die aktuelle Sprache ist ein einziger globaler Zustand (kein Multi-Window-Szenario mit
 * unterschiedlichen Sprachen pro Fenster nötig): {@link #setLocale} tauscht das aktive {@link
 * ResourceBundle} aus, anschließend gelesene Texte über {@link #get} verwenden sofort die neue
 * Sprache. Bereits konstruierte Swing-Komponenten aktualisieren sich dadurch nicht automatisch -
 * der Aufrufer muss die betroffene Ansicht neu aufbauen (siehe {@code MenuPanel}s
 * Sprachumschalter).
 */
public final class Messages {

  private static final String BUNDLE_NAME = "i18n.messages";

  private static volatile Locale currentLocale = Locale.GERMAN;
  private static volatile ResourceBundle bundle =
      ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);

  private Messages() {}

  /**
   * Setzt die aktive Sprache. Bereits erzeugte Komponenten übernehmen die neue Sprache erst nach
   * einem Neuaufbau.
   *
   * @param locale neue Sprache (nur die Sprachfamilie wird ausgewertet, z. B. {@link Locale#GERMAN}
   *     oder {@link Locale#ENGLISH})
   */
  public static void setLocale(Locale locale) {
    currentLocale = locale;
    bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  /**
   * @return aktuell aktive Sprache
   */
  public static Locale getLocale() {
    return currentLocale;
  }

  /**
   * Liefert den lokalisierten Text für den angegebenen Schlüssel.
   *
   * @param key Schlüssel aus {@code messages_*.properties}
   * @return lokalisierter Text
   */
  public static String get(String key) {
    return bundle.getString(key);
  }
}
