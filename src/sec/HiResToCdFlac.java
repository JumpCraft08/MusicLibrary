import javax.swing.*;
import java.io.*;
import java.util.*;

public class HiResToCdFlac {

    private final File baseFolder;

    public HiResToCdFlac(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void convertWithProgress(JFrame parent) {
        if (!FileProcessor.checkBaseFolder(parent, baseFolder)) return;
        if (!FileProcessor.checkFfmpeg(parent)) return;

        new Thread(() -> {
            List<File> hiResFiles = collectHiResFlacFiles();
            if (hiResFiles.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No FLAC_HI_RES files found for conversion.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            FileProcessor.ProgressDialog dialog = FileProcessor.createProgressDialog(parent, "Converting Hi-Res FLAC to CD FLAC", hiResFiles.size());
            dialog.show();

            File cdFolder = new File(baseFolder, "FLAC_CD");
            if (!cdFolder.exists()) cdFolder.mkdirs();

            try (SimpleLogger logger = new SimpleLogger(baseFolder, "HiResToCdFlac")) {
                for (int i = 0; i < hiResFiles.size(); i++) {
                    if (dialog.isStopRequested()) break;

                    File flac = hiResFiles.get(i);
                    final int index = i + 1;
                    dialog.setCurrentFile("Converting: " + flac.getName());

                    File destination = buildDestinationFile(flac, cdFolder);
                    File coverFile = new File(flac.getParentFile(), "cover.jpg");

                    if (destination.exists()) {
                        logger.log("⚠️ SKIPPED (already exists): " + destination.getAbsolutePath());
                    } else {
                        List<String> ffmpegCommand = buildFfmpegCommand(flac, destination, coverFile.exists() ? coverFile : null);
                        boolean success = runFfmpeg(ffmpegCommand);
                        logger.log(success ? "✅ CONVERTED: " + destination.getAbsolutePath() : "❌ ERROR: " + destination.getAbsolutePath());

                        // Copiar cover.jpg si existe y no estaba incluido
                        if (coverFile.exists() && success) {
                            try {
                                File destCover = new File(destination.getParentFile(), "cover.jpg");
                                if (!destCover.exists()) java.nio.file.Files.copy(coverFile.toPath(), destCover.toPath());
                            } catch (IOException e) {
                                logger.log("❌ Error copying cover: " + coverFile.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }
                    }

                    dialog.setProgress(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(parent,
                    dialog.isStopRequested() ? "Conversion stopped by user." : "Conversion completed.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }).start();
    }

    private File buildDestinationFile(File flacFile, File cdFolder) {
        File albumFolder = flacFile.getParentFile();
        String relativePath = baseFolder.toURI().relativize(albumFolder.toURI()).getPath();
        relativePath = relativePath.replaceFirst("^FLAC_HI_RES/", "");
        File destinationDir = new File(cdFolder, relativePath);
        if (!destinationDir.exists()) destinationDir.mkdirs();
        return new File(destinationDir, flacFile.getName());
    }

    private List<String> buildFfmpegCommand(File flacFile, File destinationFile, File coverFile) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i"); command.add(flacFile.getAbsolutePath());
        command.add("-vn"); // Ignorar stream de video

        command.add("-ar"); command.add("44100");
        command.add("-ac"); command.add("2");
        command.add("-sample_fmt"); command.add("s16");
        command.add("-af"); command.add("aresample=resampler=soxr");

        command.add(destinationFile.getAbsolutePath());
        return command;
    }

    private boolean runFfmpeg(List<String> command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) System.out.println(line);
            }

            return process.waitFor() == 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private List<File> collectHiResFlacFiles() {
        List<File> files = new ArrayList<>();
        File hiResFolder = new File(baseFolder, "FLAC_HI_RES");
        if (hiResFolder.exists()) addFlacFilesRecursively(hiResFolder, files);
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
