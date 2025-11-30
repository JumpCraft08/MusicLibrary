package com.jumpcraft08.file_manager.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ListFiles {

    public static List<File> listFiles(File folder, int maxDepth, String extension) {
        List<File> result = new ArrayList<>();
        if (maxDepth < 0) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(extension)) {
                result.add(file);
            } else if (file.isDirectory()) {
                result.addAll(listFiles(file, maxDepth - 1, extension));
            }
        }
        return result;
    }

    public static List<Map<File, File>> listFilesWithCover(File folder, int maxDepth, String extension) {
        List<Map<File, File>> result = new ArrayList<>();
        if (maxDepth < 0) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(extension)) {
                Map<File, File> map = new HashMap<>();
                map.put(file, null);
                result.add(map);
            } else if (file.isDirectory()) {

                File cover = new File(file, "cover.jpg");
                List<Map<File, File>> innerFiles = listFilesWithCover(file, maxDepth - 1, extension);

                for (Map<File, File> entry : innerFiles) {
                    entry.replaceAll((musicFile, oldCover) -> cover.exists() ? cover : null);
                }

                result.addAll(innerFiles);
            }
        }

        return result;
    }
}
