package cliente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {
	private String _rutaCarpeta;

	public GestorArchivos(String nombreUsuario) {
		// Nos aseguramos de que existe la carpeta "resources"
		File directorioBase = new File("./resources");
        if (!directorioBase.exists()) {
            directorioBase.mkdirs();
        }
        
        // Ahora la ruta de la carpeta del usuario va dentro de resources
        this._rutaCarpeta = "./resources/" + nombreUsuario;
        File carpeta = new File(this._rutaCarpeta);
        
		// Si la carpeta no existe, la creamos
		if (!carpeta.exists()) {
			carpeta.mkdirs();
			System.out.println("Carpeta de libros creada: " + _rutaCarpeta);
		} else {
			System.out.println("Carpeta de libros encontrada: " + _rutaCarpeta);
		}
	}

	// Metodo para obtener los nombres de los archivos que hay en la carpeta
	public List<String> obtenerArchivosLocales() {
		List<String> listaArchivos = new ArrayList<>();
		File carpeta = new File(this._rutaCarpeta);
		File[] archivos = carpeta.listFiles();

		if (archivos != null) {
			for (File archivo : archivos) {
				if (archivo.isFile()) {
					String nombre = archivo.getName().toLowerCase();
                    // solo se acepta pdfs y epubs
                    if (nombre.endsWith(".pdf") || nombre.endsWith(".epub")) {
                        listaArchivos.add(archivo.getName());
                    }
				}
			}
		}
		return listaArchivos;
	}

	public String getRutaCarpeta() {
		return _rutaCarpeta;
	}
}