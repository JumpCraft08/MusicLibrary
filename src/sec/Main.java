/*
 * Main.java
 * ---------
 * Punto de entrada del programa.
 * Configura el look and feel nativo del sistema operativo
 * y lanza la ventana principal MusicLibraryApp.
 */

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Intenta aplicar el estilo visual del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, seguimos con el look and feel por defecto
        }

        // Lanza la aplicación en el hilo de Swing (recomendado)
        SwingUtilities.invokeLater(() -> new MusicLibraryApp().setVisible(true));
    }
}
