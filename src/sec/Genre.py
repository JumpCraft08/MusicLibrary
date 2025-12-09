import os
from mutagen.flac import FLAC

def main():
    # Pedir el género al usuario
    genero = input("Introduce el género que quieras aplicar a todos los FLAC: ").strip()

    # Recorrer todos los archivos de la carpeta actual
    for archivo in os.listdir("."):
        if archivo.lower().endswith(".flac"):
            try:
                audio = FLAC(archivo)
                audio["genre"] = genero  # Cambiar o añadir el metadato
                audio.save()
                print(f"[OK] Género cambiado a '{genero}' en: {archivo}")
            except Exception as e:
                print(f"[ERROR] No se pudo modificar {archivo}: {e}")

if __name__ == "__main__":
    main()
