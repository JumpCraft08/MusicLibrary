import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileProcessor {

    public static boolean checkBaseFolder(JFrame parent, File baseFolder) {
        if (baseFolder == null || !baseFolder.exists()) {
            JOptionPane.showMessageDialog(parent,
                    "❌ Error: La carpeta base no existe.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean checkFfmpeg(JFrame parent) {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "❌ Error: ffmpeg no está disponible en PATH.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static ProgressDialog createProgressDialog(JFrame parent, String title, int max) {
        JDialog dialog = new JDialog(parent, title, true);
        JProgressBar progressBar = new JProgressBar(0, max);
        progressBar.setStringPainted(true);
        JLabel currentFileLabel = new JLabel("Iniciando...");
        JButton stopButton = new JButton("Stop");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(currentFileLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(stopButton, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setSize(450, 120);
        dialog.setLocationRelativeTo(parent);

        return new ProgressDialog(dialog, progressBar, currentFileLabel, stopButton);
    }

    public static class ProgressDialog {
        private final JDialog dialog;
        private final JProgressBar progressBar;
        private final JLabel currentFileLabel;
        private final JButton stopButton;
        private volatile boolean stopRequested = false;

        public ProgressDialog(JDialog dialog, JProgressBar progressBar, JLabel currentFileLabel, JButton stopButton) {
            this.dialog = dialog;
            this.progressBar = progressBar;
            this.currentFileLabel = currentFileLabel;
            this.stopButton = stopButton;
            this.stopButton.addActionListener(e -> stopRequested = true);
        }

        public void show() {
            SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        }

        public void dispose() {
            SwingUtilities.invokeLater(dialog::dispose);
        }

        public void setProgress(int value) {
            SwingUtilities.invokeLater(() -> progressBar.setValue(value));
        }

        public void setCurrentFile(String text) {
            SwingUtilities.invokeLater(() -> currentFileLabel.setText(text));
        }

        public boolean isStopRequested() {
            return stopRequested;
        }
    }
}
