import javax.swing.*;
import java.io.*;
import java.util.*;

public class GenerateSpectrograms {

    private final File baseFolder;

    public GenerateSpectrograms(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void generate(JFrame parent) {
        if (!FileProcessor.checkBaseFolder(parent, baseFolder)) return;

        new Thread(() -> {
            File specFolder = new File(baseFolder, "SPEC");
            if (!specFolder.exists()) specFolder.mkdirs();

            List<File> allFiles = new ArrayList<>();
            String[] versions = {"FLAC_HI_RES", "FLAC_CD", "M4A"};
            for (String version : versions) {
                File versionFolder = new File(baseFolder, version);
                if (!versionFolder.exists()) continue;
                File[] albums = versionFolder.listFiles(File::isDirectory);
                if (albums == null) continue;
                for (File albumDir : albums) {
                    File[] musicFiles = albumDir.listFiles(f ->
                            f.getName().toLowerCase().endsWith(".flac") ||
                                    f.getName().toLowerCase().endsWith(".m4a"));
                    if (musicFiles != null) allFiles.addAll(Arrays.asList(musicFiles));
                }
            }

            FileProcessor.ProgressDialog dialog = FileProcessor.createProgressDialog(parent, "Generando espectrogramas", allFiles.size());
            dialog.show();

            try (SimpleLogger logger = new SimpleLogger(baseFolder, "GenerateSpectrograms")) {
                for (int i = 0; i < allFiles.size(); i++) {
                    File musicFile = allFiles.get(i);
                    final int index = i + 1;
                    dialog.setCurrentFile("Generando: " + musicFile.getName());

                    File albumDir = musicFile.getParentFile();
                    String version = albumDir.getParentFile().getName();
                    File destDir = new File(specFolder, version + "/" + albumDir.getName());
                    destDir.mkdirs();

                    String nameWithoutExt = musicFile.getName().substring(0, musicFile.getName().lastIndexOf('.'));
                    File destFile = new File(destDir, nameWithoutExt + ".png");

                    if (destFile.exists()) {
                        logger.log("⚠️ Saltado (ya existe): " + musicFile.getAbsolutePath());
                        dialog.setProgress(index);
                        continue;
                    }

                    List<String> cmd = Arrays.asList(
                            "ffmpeg",
                            "-i", musicFile.getAbsolutePath(),
                            "-lavfi", "showspectrumpic=s=1920x1080:legend=disabled:mode=combined",
                            "-frames:v", "1",
                            "-f", "image2",
                            "-update", "1",
                            destFile.getAbsolutePath()
                    );

                    try {
                        ProcessBuilder pb = new ProcessBuilder(cmd);
                        pb.redirectErrorStream(true);
                        Process process = pb.start();

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) System.out.println(line);
                        }

                        int exitCode = process.waitFor();
                        logger.log(exitCode == 0 ?
                                "✅ Espectrograma generado: " + musicFile.getAbsolutePath() :
                                "❌ Error con: " + musicFile.getAbsolutePath());
                    } catch (Exception e) {
                        logger.log("❌ Excepción con: " + musicFile.getAbsolutePath() + " -> " + e.getMessage());
                        e.printStackTrace();
                    }

                    dialog.setProgress(index);
                    if (dialog.isStopRequested()) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(parent,
                    "Generación " + (dialog.isStopRequested() ? "detenida por el usuario." : "completada."),
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }
}
