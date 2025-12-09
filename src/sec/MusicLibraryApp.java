import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class MusicLibraryApp extends JFrame {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private File selectedFolder;
    private java.util.List<MusicFile> musicFiles;

    // --- Espectrograma ---
    private SpectrogramSidebar spectrogramSidebar;
    private JCheckBoxMenuItem showSpectrogramItem;

    public MusicLibraryApp() {
        setTitle("Biblioteca Musical");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Menú superior ---
        JMenuBar menuBar = new JMenuBar();

        // --- Archivo ---
        JMenu fileMenu = new JMenu("Archivo");

        JMenuItem selectFolderItem = new JMenuItem("Seleccionar carpeta");
        selectFolderItem.addActionListener(e -> chooseFolder());
        fileMenu.add(selectFolderItem);

        JMenuItem convertFlacItem = new JMenuItem("Convertir FLAC a AAC");
        convertFlacItem.addActionListener(e -> {
            if (selectedFolder == null) { showWarning("Selecciona primero una carpeta."); return; }
            new FlacToAac(selectedFolder).convertWithProgress(this);
        });
        fileMenu.add(convertFlacItem);

        JMenuItem generateSpecItem = new JMenuItem("Generar espectrogramas");
        generateSpecItem.addActionListener(e -> {
            if (selectedFolder == null) { showWarning("Selecciona primero una carpeta."); return; }
            new GenerateSpectrograms(selectedFolder).generate(this);
        });
        fileMenu.add(generateSpecItem);

        JMenuItem optimizeFlacItem = new JMenuItem("Optimizar FLAC");
        optimizeFlacItem.addActionListener(e -> {
            if (selectedFolder == null) { showWarning("Selecciona primero una carpeta."); return; }
            new OptimizeFlac(selectedFolder).optimizeWithProgress(this);
        });
        fileMenu.add(optimizeFlacItem);

        JMenuItem hiResToCdItem = new JMenuItem("Hi-Res a CD FLAC");
        hiResToCdItem.addActionListener(e -> {
            if (selectedFolder == null) { showWarning("Selecciona primero una carpeta."); return; }
            new HiResToCdFlac(selectedFolder).convertWithProgress(this);
        });
        fileMenu.add(hiResToCdItem);

        // --- NUEVO: Añadir letras ---
        JMenuItem addLyricsItem = new JMenuItem("Añadir letras a AAC");
        addLyricsItem.addActionListener(e -> {
            if (selectedFolder == null) { 
                showWarning("Selecciona primero una carpeta."); 
                return; 
            }

            new Thread(() -> {
                File m4aFolder = new File(selectedFolder, "M4A");
                if (!m4aFolder.exists()) {
                    SwingUtilities.invokeLater(() -> showWarning("No existe la carpeta M4A en la carpeta seleccionada."));
                    return;
                }

                try (SimpleLogger logger = new SimpleLogger(selectedFolder, "AñadirLetras")) {
                    logger.log("⏳ Iniciando búsqueda y añadido de letras en: " + m4aFolder.getAbsolutePath());

                    List<String> command = new ArrayList<>();
                    command.add("python"); // o "python3" según tu sistema
                    command.add(new File(selectedFolder.getParentFile(), "src/AddLyrics.py").getAbsolutePath());
                    command.add(new File(selectedFolder, "M4A").getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.redirectErrorStream(true);
                    pb.environment().put("PYTHONUTF8", "1");
                    Process process = pb.start();

                    try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.log(line);              // sigue escribiendo en el log
                        System.out.println(line);      // además, se imprime en la consola de Java
                            }
                        }

                    int exitCode = process.waitFor();
                    logger.log(exitCode == 0 ? "✅ Proceso completado correctamente." : "❌ Proceso finalizó con errores.");

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this,
                                    exitCode == 0 ? "Letras añadidas correctamente." : "Hubo errores, revisa el log.",
                                    "Info", JOptionPane.INFORMATION_MESSAGE)
                    );

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() ->
                            showWarning("Error al ejecutar el script: " + ex.getMessage()));
                }
            }).start();
        });
        fileMenu.add(addLyricsItem);

        menuBar.add(fileMenu);

        // --- Ver ---
        JMenu viewMenu = new JMenu("Ver");
        showSpectrogramItem = new JCheckBoxMenuItem("Mostrar espectrograma", false);
        showSpectrogramItem.addActionListener(e -> toggleSpectrogramSidebar(showSpectrogramItem.isSelected()));
        viewMenu.add(showSpectrogramItem);
        menuBar.add(viewMenu);

        // --- Ayuda ---
        JMenu helpMenu = new JMenu("Ayuda");
        JMenuItem aboutItem = new JMenuItem("Acerca de");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Mi aplicación de gestión de FLAC\nVersión 1.0",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        JMenuItem helpContentItem = new JMenuItem("Contenido de ayuda");
        helpContentItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "• Selecciona una carpeta con tu biblioteca.\n" +
                        "• Usa Archivo para convertir/optimizar.\n" +
                        "• Activa Ver → Mostrar espectrograma para ver el panel lateral.\n" +
                        "• Haz clic en una fila para reproducir (FLAC_CD).",
                "Ayuda", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(helpContentItem);

        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // --- Tabla ---
        tableModel = new DefaultTableModel(new Object[]{"Artista", "Álbum", "Nombre", "Versiones"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(table);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) playSelected();
            }
        });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    spectrogramSidebar.updateForSelection(selectedFolder, musicFiles, table);
                }
            }
        });

        // --- Panel espectrograma ---
        spectrogramSidebar = new SpectrogramSidebar();
        add(tableScroll, BorderLayout.CENTER);
        add(spectrogramSidebar, BorderLayout.EAST);
    }

    private void toggleSpectrogramSidebar(boolean show) {
        spectrogramSidebar.setVisible(show);
        revalidate();
        repaint();
        if (show) spectrogramSidebar.updateForSelection(selectedFolder, musicFiles, table);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.getSelectedFile();
            updateTable();
            if (showSpectrogramItem.isSelected()) spectrogramSidebar.updateForSelection(selectedFolder, musicFiles, table);
        }
    }

    private void loadMusicFiles() {
        if (selectedFolder == null) return;
        musicFiles = new MusicScanner(selectedFolder).scan();
        musicFiles.sort(Comparator.comparing(MusicFile::getArtist)
                .thenComparing(MusicFile::getAlbum)
                .thenComparing(MusicFile::getName));
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        if (musicFiles == null) return;
        for (MusicFile f : musicFiles) {
            tableModel.addRow(new Object[]{f.getArtist(), f.getAlbum(), f.getName(), f.versionsAsString()});
        }
    }

    private void updateTable() {
        loadMusicFiles();
        populateTable();
    }

    private void playSelected() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int modelRow = table.convertRowIndexToModel(row);
        MusicFile file = musicFiles.get(modelRow);
        if (!file.versionsAsString().contains("FLAC_CD")) return;
        File flacCDFile = findFile(file, "FLAC_CD");
        if (flacCDFile != null && Desktop.isDesktopSupported()) {
            try { Desktop.getDesktop().open(flacCDFile); } 
            catch (Exception ex) { ex.printStackTrace(); showWarning("No se pudo abrir el archivo con el reproductor predeterminado."); }
        }
    }

    private File findFile(MusicFile file, String version) {
        if (version == null) return null;
        File dir = new File(new File(selectedFolder, version), file.getArtist() + " - " + file.getAlbum());
        if (!dir.exists()) return null;
        return Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(f -> f.getName().startsWith(file.getName() + "."))
                .findFirst().orElse(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicLibraryApp().setVisible(true));
    }
}
