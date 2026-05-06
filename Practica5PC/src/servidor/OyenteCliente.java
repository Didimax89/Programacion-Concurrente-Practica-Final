package servidor;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import comun.Mensaje;
import comun.Usuario;
import comun.Locks;
import comun.Consola;
import comun.LockTicket;

public class OyenteCliente implements Runnable {
	private Socket _socket;
	private ControladorBD _controladorBD;
	private BaseDeDatosServidor _baseDeDatos;
	private Usuario _usuario; // Para saber quien es este cliente
	private Locks _lockRed = new LockTicket();

	public OyenteCliente(Socket socket, BaseDeDatosServidor baseDeDatos, ControladorBD controlador) {
		this._socket = socket;
		this._baseDeDatos = baseDeDatos;
        this._controladorBD = controlador;
	}

	@Override
	public void run() {
		try (
			ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(_socket.getInputStream())
		) {

			// Bucle para escuchar continuamente lo que dice el cliente
			while (true) {
				Mensaje m = (Mensaje) in.readObject();
                if (m instanceof Mensaje.Conexion) {
                	this._usuario = ((Mensaje.Conexion) m).getUsuario();
                	// Pedimos permiso para escribir
                    _controladorBD.requestWrite();
                    try {
                        _baseDeDatos.registrarUsuario(_usuario);
                    } finally {
                        _controladorBD.releaseWrite(); // Soltamos el permiso
                    }
				} else if (m instanceof Mensaje.BuscarUsuarios) {
	                // Respondemos con la lista de objetos Usuario (Tipo 4)
					List<Usuario> catalogo;
                    // Pedimos permiso para leer
                    _controladorBD.requestRead();
                    try {
                        catalogo = _baseDeDatos.buscarUsuarios();
                    } finally {
                        _controladorBD.releaseRead(); // Soltamos el permiso
                    }
                    _lockRed.enviarMensajeSeguro(0, out, new Mensaje.RespuestaBusqueda(catalogo));
				} else if (m instanceof Mensaje.NotificarNuevoArchivo) {
					Mensaje.NotificarNuevoArchivo msgNuevo = (Mensaje.NotificarNuevoArchivo) m;
                    // Pedimos permiso para escribir
                    _controladorBD.requestWrite();
                    try {
                        _baseDeDatos.anadirArchivoAUsuario(_usuario.getNombre(), msgNuevo.getNombreArchivo());
                    } finally {
                        _controladorBD.releaseWrite();
                    }
	            }
			}
		} catch (Exception e) { 
			Consola.escribir("Cliente desconectado.");
		} finally {
        // Cuando el bucle se rompe o hay error, borramos al usuario
			if (_usuario != null) {
                // Pedimos permiso para escribir al borrar
                _controladorBD.requestWrite();
                try {
                    _baseDeDatos.eliminarUsuario(_usuario);
                } finally {
                    _controladorBD.releaseWrite();
                }
			}
		}
	}
}