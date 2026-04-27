package cliente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EscuchadorP2P extends Thread {
    private int _puerto;
    private String _nombreUsuario;
    private ClienteGUI _ventana;

    public EscuchadorP2P(int puerto, String nombreUsuario, ClienteGUI ventana) {
        this._puerto = puerto;
        this._nombreUsuario = nombreUsuario;
        this._ventana = ventana;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(_puerto)) {
            _ventana.escribirEnPantalla("[P2P] Escuchando peticiones de otros clientes en el puerto " + _puerto);
            
            while (true) {
                // Alguien se ha conectado para pedirnos un archivo
                Socket socketPeticion = serverSocket.accept();
                
                // Atendemos la peticion en un hilo nuevo para no bloquearnos
                new HiloEmisor(socketPeticion, _nombreUsuario, _ventana).start();
            }
        } catch (IOException e) {
            _ventana.escribirEnPantalla("[P2P] Error en el puerto de escucha.");
        }
    }
}