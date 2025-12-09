import javax.swing.*;
import java.io.*;
import java.util.*;

public class OptimizeFlac {

    private final File baseFolder;

    public OptimizeFlac(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void optimizeWithProgress(JFrame parent) {
        if (!FileProcessor.checkBaseFolder(parent, baseFolder)) return;
        if (!FileProcessor.checkFfmpeg(parent)) return;

        new Thread(() -> {
            List<File> flacFiles = collectFlacFiles();
            if (flacFiles.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No se encontraron archivos FLAC para optimizar.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            FileProcessor.ProgressDialog dialog = FileProcessor.createProgressDialog(parent, "Optimizing FLAC Files", flacFiles.size());
            dialog.show();

            try (SimpleLogger logger = new SimpleLogger(baseFolder, "OptimizeFlac")) {
                for (int i = 0; i < flacFiles.size(); i++) {
                    if (dialog.isStopRequested()) break;

                    File flac = flacFiles.get(i);
                    final int index = i + 1;
                    dialog.setCurrentFile("Optimizing: " + flac.getName());

                    try {
                        File tempFile = new File(flac.getParent(), flac.getName().replaceAll("\\.flac$", ".temp.flac"));

                        List<String> cmd = Arrays.asList(
                                "ffmpeg", "-i", flac.getAbsolutePath(),
                                "-compression_level", "8", "-y", tempFile.getAbsolutePath()
                        );

                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.redirectErrorStream(true);
                        Process process = pb.start();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) System.out.println(line);
                        }

                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            long originalSize = flac.length();
                            long tempSize = tempFile.length();
                            if (tempSize < originalSize) {
                                if (!flac.delete()) {
                                    logger.log("❌ No se pudo eliminar original: " + flac.getAbsolutePath());
                                    continue;
                                }
                                if (!tempFile.renameTo(flac)) {
                                    logger.log("❌ No se pudo renombrar temporal: " + tempFile.getAbsolutePath());
                                    continue;
                                }
                                double ahorro = Math.round((1 - (double) tempSize / originalSize) * 10000) / 100.0;
                                logger.log("✅ Optimizado: " + flac.getAbsolutePath() + " (-" + ahorro + "%)");
                            } else {
                                tempFile.delete();
                                logger.log("ℹ️ Ya optimizado: " + flac.getAbsolutePath());
                            }
                        } else {
                            tempFile.delete();
                            logger.log("❌ Error al optimizar: " + flac.getAbsolutePath());
                        }

                    } catch (Exception e) {
                        logger.log("❌ Excepción: " + flac.getAbsolutePath() + " -> " + e.getMessage());
                        e.printStackTrace();
                    }

                    dialog.setProgress(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(parent,
                    dialog.isStopRequested() ? "Optimización detenida por el usuario." : "Optimización completada.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }

    private List<File> collectFlacFiles() {
        List<File> files = new ArrayList<>();
        for (String folderName : new String[]{"FLAC_HI_RES", "FLAC_CD"}) {
            File folder = new File(baseFolder, folderName);
            if (folder.exists()) addFlacFilesRecursively(folder, files);
        }
        return files;
    }

    private void addFlacFilesRecursively(File folder, List<File> fileList) {
        try (var paths = java.nio.file.Files.walk(folder.toPath())) {
            paths.filter(p -> p.toString().toLowerCase().endsWith(".flac"))
                 .forEach(p -> fileList.add(p.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
