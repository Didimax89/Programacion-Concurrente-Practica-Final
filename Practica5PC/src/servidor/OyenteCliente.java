package servidor;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import comun.Mensaje;
import comun.Usuario;
import comun.Locks;
import comun.LockTicket;

public class OyenteCliente implements Runnable {
	private Socket _socket;
	private BaseDeDatosServidor _baseDeDatos;
	private Usuario _usuario; // Para saber quien es este cliente
	private Locks _lockRed = new LockTicket();

	public OyenteCliente(Socket socket, BaseDeDatosServidor baseDeDatos) {
		this._socket = socket;
		this._baseDeDatos = baseDeDatos;
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
                    _baseDeDatos.registrarUsuario(_usuario);
				} else if (m instanceof Mensaje.BuscarUsuarios) {
	                // Respondemos con la lista de objetos Usuario (Tipo 4)
	                _lockRed.enviarMensajeSeguro(0, out, new Mensaje.RespuestaBusqueda(_baseDeDatos.buscarUsuarios()));
				} else if (m instanceof Mensaje.NotificarNuevoArchivo) {
	                Mensaje.NotificarNuevoArchivo msgNuevo = (Mensaje.NotificarNuevoArchivo) m;
	                _baseDeDatos.anadirArchivoAUsuario(_usuario.getNombre(), msgNuevo.getNombreArchivo());
	            }
			}
		} catch (Exception e) { 
			System.out.println("Cliente desconectado.");
		} finally {
        // Cuando el bucle se rompe o hay error, borramos al usuario
        if (_usuario != null) {
            _baseDeDatos.eliminarUsuario(_usuario);
        }
    }
	}
}