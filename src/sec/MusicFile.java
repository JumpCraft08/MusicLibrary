/*
 * MusicFile.java
 * ----------------
 * Representa una canción en la biblioteca.
 * Contiene: nombre, artista, álbum y versiones disponibles.
 */

import java.util.HashSet;
import java.util.Set;

public class MusicFile {
    private final String name;
    private final String artist;
    private final String album;
    private final Set<String> versions = new HashSet<>();

    public MusicFile(String name, String artist, String album) {
        this.name = name;
        this.artist = artist;
        this.album = album;
    }

    // --- Getters ---
    public String getName()   { return name; }
    public String getArtist() { return artist; }
    public String getAlbum()  { return album; }

    // Añade una versión (FLAC_CD, Hi-Res, etc.)
    public void addVersion(String version) { versions.add(version); }

    // Devuelve las versiones como texto legible
    public String versionsAsString() { return String.join(", ", versions); }
}
