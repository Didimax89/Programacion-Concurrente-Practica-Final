package cliente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EscuchadorP2P extends Thread {
	private ServerSocket _serverSocket;
    private String _nombreUsuario;
    private ClienteGUI _ventana;

    public EscuchadorP2P(ServerSocket socket, String nombreUsuario, ClienteGUI ventana) {
    	this._serverSocket = socket;
        this._nombreUsuario = nombreUsuario;
        this._ventana = ventana;
    }

    @Override
    public void run() {
        try {
        	_ventana.escribirEnPantalla("[P2P] Escuchando peticiones en el puerto " + _serverSocket.getLocalPort());
            
            while (true) {
                // Alguien se ha conectado para pedirnos un archivo
            	Socket socketPeticion = _serverSocket.accept();
                
                // Atendemos la peticion en un hilo nuevo para no bloquearnos
                new HiloEmisor(socketPeticion, _nombreUsuario, _ventana).start();
            }
        } catch (IOException e) {
            _ventana.escribirEnPantalla("[P2P] Error en el puerto de escucha.");
        }
    }
}