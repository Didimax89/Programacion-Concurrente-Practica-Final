package servidor;

import comun.Consola;
import comun.Usuario;
import java.util.ArrayList;
import java.util.List;

public class BaseDeDatosServidor {
    private List<Usuario> _usuariosConectados = new ArrayList<>();

    // Cuando un cliente hace login, el escritor usa:
    public void registrarUsuario(Usuario usuario) {
        _usuariosConectados.add(usuario);
        Consola.escribir("Servidor: Usuario " + usuario.getNombre() + " registrado con sus libros.");
    }

    // Cuando un cliente busca, el lector usa:
    public List<Usuario> buscarUsuarios() {
    	Consola.escribir("Servidor: Enviando catalogo de libros...");
        return new ArrayList<>(_usuariosConectados);
    }

    // Elimina a un usuario cuando se desconecta
    public void eliminarUsuario(Usuario usuario) {
        if (usuario == null) return;
        // Buscamos al usuario por su nombre y lo borramos de la lista
        _usuariosConectados.removeIf(u -> u.getNombre().equals(usuario.getNombre()));
        Consola.escribir("Servidor: Usuario desconectado y eliminado: " + usuario.getNombre());
    }

    public void anadirArchivoAUsuario(String nombreUsuario, String archivo) {
        for (Usuario u : _usuariosConectados) {
            if (u.getNombre().equals(nombreUsuario)) {
                if (!u.getArchivosCompartidos().contains(archivo)) {
                    u.getArchivosCompartidos().add(archivo);
                    Consola.escribir("Servidor: Nuevo libro '" + archivo + "' a adido al catalogo de " + nombreUsuario);
                }
                break;
            }
        }
    }
}