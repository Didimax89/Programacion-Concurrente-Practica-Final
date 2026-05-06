package servidor;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import comun.Consola;

public class Servidor {
	private static final int PUERTO = 5000; // El puerto donde escucha el servidor

	public static void main(String[] args) {
		BaseDeDatosServidor baseDeDatos = new BaseDeDatosServidor();
		ControladorBD controlador = new ControladorBD();

		try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
			Consola.escribir("Servidor arrancado y escuchando en el puerto " + PUERTO);
	
			// Bucle infinito para aceptar conexiones continuamente
			while (true) {
				// El programa se queda bloqueado aqui hasta que un Cliente se conecta
				Socket socketCliente = serverSocket.accept();
				Consola.escribir("Nuevo cliente conectado desde: " + socketCliente.getInetAddress());
	
				// Creamos un hilo nuevo para atender a este cliente y le pasamos el socket y la base de datos
				OyenteCliente oyente = new OyenteCliente(socketCliente, baseDeDatos, controlador);
				Thread hiloCliente = new Thread(oyente);
				hiloCliente.start();
			}
		} catch (IOException e) {
			Consola.escribir("Error en el servidor: " + e.getMessage());
			e.printStackTrace();
		}
	}
}