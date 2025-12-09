import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpectrogramSidebar extends JPanel {
    private JLabel spectroImageLabel;
    private JLabel spectroCaptionLabel;
    private JButton prevButton, nextButton;

    private final List<SpectroEntry> currentSpectrograms = new ArrayList<>();
    private int currentSpectrogramIndex = 0;

    public SpectrogramSidebar() {
        super(new BorderLayout());

        setPreferredSize(new Dimension(420, 1));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230,230,230)));

        // Cabecera
        JLabel title = new JLabel(" Espectrograma", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(title, BorderLayout.NORTH);

        // Imagen central
        spectroImageLabel = new JLabel("No hay espectrograma", SwingConstants.CENTER);
        spectroImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        spectroImageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(new JScrollPane(spectroImageLabel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        // Pie con controles
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));

        prevButton = new JButton("◀");
        nextButton = new JButton("▶");
        prevButton.addActionListener(e -> showSpectrogramAt(currentSpectrogramIndex - 1));
        nextButton.addActionListener(e -> showSpectrogramAt(currentSpectrogramIndex + 1));

        nav.add(prevButton);
        nav.add(nextButton);

        spectroCaptionLabel = new JLabel(" ", SwingConstants.CENTER);
        spectroCaptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        bottom.add(nav, BorderLayout.NORTH);
        bottom.add(spectroCaptionLabel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        setVisible(false); // empieza oculto
    }

    // -------------------- Lógica de espectrogramas --------------------

    public void updateForSelection(File selectedFolder, List<MusicFile> musicFiles, JTable table) {
        if (!isVisible()) return;
        currentSpectrograms.clear();
        currentSpectrogramIndex = 0;

        int row = table.getSelectedRow();
        if (row == -1 || selectedFolder == null || musicFiles == null) {
            showNoSpectrogramMsg("Selecciona una canción para ver su espectrograma.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        MusicFile file = musicFiles.get(modelRow);

        String[] versions = {"FLAC_HI_RES", "FLAC_CD", "M4A"};
        for (String v : versions) {
            File png = locateSpectrogramPng(selectedFolder, v, file);
            if (png != null && png.exists()) {
                currentSpectrograms.add(new SpectroEntry(v, png));
            }
        }

        if (currentSpectrograms.isEmpty()) {
            showNoSpectrogramMsg("No hay espectrogramas para esta pista.\nGenera primero con Archivo → Generar espectrogramas.");
        } else {
            showSpectrogramAt(0);
        }
    }

    private File locateSpectrogramPng(File base, String version, MusicFile mf) {
        File dir = new File(new File(new File(base, "SPEC"), version), mf.getArtist() + " - " + mf.getAlbum());
        if (!dir.exists()) return null;
        File f = new File(dir, mf.getName() + ".png");
        return f.exists() ? f : null;
    }

    private void showNoSpectrogramMsg(String msg) {
        spectroImageLabel.setIcon(null);
        spectroImageLabel.setText("<html><div style='text-align:center;'>" + msg.replace("\n", "<br>") + "</div></html>");
        spectroCaptionLabel.setText(" ");
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    private void showSpectrogramAt(int idx) {
        if (currentSpectrograms.isEmpty()) {
            showNoSpectrogramMsg("No hay espectrogramas para esta pista.");
            return;
        }
        if (idx < 0) idx = 0;
        if (idx >= currentSpectrograms.size()) idx = currentSpectrograms.size() - 1;
        currentSpectrogramIndex = idx;

        SpectroEntry e = currentSpectrograms.get(idx);
        ImageIcon icon = new ImageIcon(e.file.getAbsolutePath());

        int maxW = getWidth() > 0 ? getWidth() - 24 : 396;
        int maxH = 340;
        Image scaled = scaleToFit(icon.getImage(), maxW, maxH);
        spectroImageLabel.setIcon(new ImageIcon(scaled));
        spectroImageLabel.setText(null);

        spectroCaptionLabel.setText(String.format("%s — %s (%d/%d)",
                e.version, e.file.getName(), idx + 1, currentSpectrograms.size()));

        prevButton.setEnabled(currentSpectrograms.size() > 1 && idx > 0);
        nextButton.setEnabled(currentSpectrograms.size() > 1 && idx < currentSpectrograms.size() - 1);
    }

    private Image scaleToFit(Image img, int maxW, int maxH) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return img;
        double rw = maxW / (double) w;
        double rh = maxH / (double) h;
        double r = Math.min(1.0, Math.min(rw, rh));
        int nw = (int) Math.round(w * r);
        int nh = (int) Math.round(h * r);
        return img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    }

    private static class SpectroEntry {
        final String version;
        final File file;
        SpectroEntry(String v, File f) { version = v; file = f; }
    }
}
