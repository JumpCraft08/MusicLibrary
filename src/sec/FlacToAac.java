import javax.swing.*;
import java.io.*;
import java.util.*;

public class FlacToAac {

    private final File baseFolder;

    public FlacToAac(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void convertWithProgress(JFrame parent) {
        if (!FileProcessor.checkBaseFolder(parent, baseFolder)) return;
        if (!FileProcessor.checkFfmpeg(parent)) return;

        new Thread(() -> {
            List<File> flacFiles = collectFlacFiles();
            if (flacFiles.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "No FLAC files found for conversion.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            FileProcessor.ProgressDialog dialog = FileProcessor.createProgressDialog(parent, "Converting FLAC to AAC", flacFiles.size());
            dialog.show();

            File outputFolder = new File(baseFolder, "M4A");
            if (!outputFolder.exists()) outputFolder.mkdirs();

            try (SimpleLogger logger = new SimpleLogger(baseFolder, "FlacToAac")) {
                for (int i = 0; i < flacFiles.size(); i++) {
                    if (dialog.isStopRequested()) break;

                    File flac = flacFiles.get(i);
                    final int index = i + 1;
                    dialog.setCurrentFile("Converting: " + flac.getName());

                    File destination = buildDestinationFile(flac, outputFolder);
                    File coverFile = new File(flac.getParentFile(), "cover.jpg");

                    if (destination.exists()) {
                        logger.log("⚠️ SKIPPED (already exists): " + destination.getAbsolutePath());
                    } else {
                        List<String> ffmpegCommand = buildFfmpegCommand(flac, destination, coverFile.exists() ? coverFile : null);
                        boolean success = runFfmpeg(ffmpegCommand);
                        logger.log(success ? "✅ CONVERTED: " + destination.getAbsolutePath() : "❌ ERROR: " + destination.getAbsolutePath());
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

    private File buildDestinationFile(File flacFile, File outputFolder) {
        File albumFolder = flacFile.getParentFile();
        String relativePath = baseFolder.toURI().relativize(albumFolder.toURI()).getPath();
        relativePath = relativePath.replaceFirst("^(FLAC_HI_RES/|FLAC_CD/)", "");
        File destinationDir = new File(outputFolder, relativePath);
        if (!destinationDir.exists()) destinationDir.mkdirs();
        return new File(destinationDir, flacFile.getName().replaceAll("\\.flac$", ".m4a"));
    }

    private List<String> buildFfmpegCommand(File flacFile, File destinationFile, File coverFile) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i"); command.add(flacFile.getAbsolutePath());

        if (coverFile != null) {
            command.add("-i"); command.add(coverFile.getAbsolutePath());
            command.add("-map"); command.add("0:a");
            command.add("-map"); command.add("1");
            command.add("-c:a"); command.add("aac");
            command.add("-b:a"); command.add("192k");
            command.add("-c:v:0"); command.add("png");
            command.add("-disposition:v:0"); command.add("attached_pic");
        } else {
            command.add("-map"); command.add("0:a");
            command.add("-c:a"); command.add("aac");
            command.add("-b:a"); command.add("192k");
        }

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
