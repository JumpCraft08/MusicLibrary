/*
 * MusicScanner.java
 * -----------------
 * Escanea la carpeta raíz de música.
 * Detecta formatos (Hi-Res, CD, M4A) y extrae artista y álbum de la carpeta.
 */

import java.io.File;
import java.util.*;

public class MusicScanner {

    private final File baseFolder;

    // Formatos soportados
    private static final String[] FORMATS = {"FLAC_HI_RES", "FLAC_CD", "M4A"};

    public MusicScanner(File folder) {
        this.baseFolder = folder;
    }

    public List<MusicFile> scan() {
        Map<String, MusicFile> map = new HashMap<>();

        for (String fmt : FORMATS) {
            File fmtFolder = new File(baseFolder, fmt);
            if (!fmtFolder.exists()) continue;

            File[] albums = fmtFolder.listFiles(File::isDirectory);
            if (albums == null) continue;

            for (File album : albums) {
                // Esperamos que las carpetas sigan el formato: "Artista - Álbum"
                String[] parts = album.getName().split(" - ", 2);
                String artist = parts[0];
                String albumName = (parts.length > 1) ? parts[1] : album.getName();

                File[] files = album.listFiles((d, name) -> name.endsWith(".flac") || name.endsWith(".m4a"));
                if (files == null) continue;

                for (File f : files) {
                    String nameOnly = f.getName().substring(0, f.getName().lastIndexOf('.'));

                    // Clave única: Artista - Álbum - Canción
                    String key = artist + " - " + albumName + " - " + nameOnly;

                    MusicFile mf = map.getOrDefault(key, new MusicFile(nameOnly, artist, albumName));
                    mf.addVersion(fmt);
                    map.put(key, mf);
                }
            }
        }

        return new ArrayList<>(map.values());
    }
}
