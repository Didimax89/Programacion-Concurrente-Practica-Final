package cliente;
import java.io.ObjectInputStream;
import java.net.Socket;

import comun.Mensaje;

public class OyenteServidor extends Thread {
	private Socket _socket;
	private ClienteGUI _ventana;

	public OyenteServidor(Socket socket, ClienteGUI ventana) {
		this._socket = socket;
		this._ventana = ventana;
	}

	@Override
	public void run() {
		try (ObjectInputStream in = new ObjectInputStream(_socket.getInputStream())) {
            while (true) {
                Mensaje m = (Mensaje) in.readObject();
                // Verificamos si es la respuesta del catalogo (Tipo 4)
                if (m instanceof Mensaje.RespuestaBusqueda) {
                    Mensaje.RespuestaBusqueda res = (Mensaje.RespuestaBusqueda) m;
                    _ventana.actualizarCatalogo(res.getUsuariosConectados());
                }
            }
        } catch (Exception e) { _ventana.escribirEnPantalla("Conexión con servidor perdida."); }
    }
}