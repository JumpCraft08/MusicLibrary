import os
import sys
import time
from mutagen.mp4 import MP4
from lyricsgenius import Genius

# Inicializa Genius con tu token
GENIUS_TOKEN = "EcrCCt9QuLRrwFkTF4urOt_q_l7L83ucCVQDv0hhNVHgXOuEz-QO6m7cZUEFi_-M"
genius = Genius(GENIUS_TOKEN)

def añadir_letras(aac_dir):
    if not os.path.isdir(aac_dir):
        print(f"[ERROR] Error: La carpeta AAC no existe en: {aac_dir}", flush=True)
        return

    output_file = os.path.join(aac_dir, "lyrics_output.txt")
    with open(output_file, "w", encoding="utf-8") as f_out:
        for root, _, files in os.walk(aac_dir):
            for archivo in files:
                if archivo.lower().endswith(".m4a"):
                    path = os.path.join(root, archivo)
                    try:
                        audio = MP4(path)
                        art = audio.tags.get('\xa9ART', [''])[0]
                        tit = audio.tags.get('\xa9nam', [''])[0]
                        if not art or not tit:
                            msg = f"[WARNING] Faltan metadatos en: {archivo}"
                            f_out.write(msg + "\n")
                            print(msg, flush=True)
                            continue

                        msg = f"⌛ Buscando letras para: {art} - {tit}"
                        f_out.write(msg + "\n")
                        print(msg, flush=True)

                        song = genius.search_song(tit, art)
                        time.sleep(0.2)

                        if song and song.lyrics:
                            audio["\xa9lyr"] = song.lyrics
                            audio.save()
                            msg = f"[OK] Letras añadidas: {archivo}"
                            f_out.write(msg + "\n")
                            print(msg, flush=True)
                        else:
                            msg = f"[ERROR] Letras no encontradas: {art} - {tit}"
                            f_out.write(msg + "\n")
                            print(msg, flush=True)

                    except Exception as e:
                        msg = f"[ERROR] Error con {archivo}: {e}"
                        f_out.write(msg + "\n")
                        print(msg, flush=True)

    print("[OK] Proceso finalizado.", flush=True)
    
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Uso: python añadir_letras.py <carpeta_M4A>", flush=True)
    else:
        añadir_letras(sys.argv[1])
