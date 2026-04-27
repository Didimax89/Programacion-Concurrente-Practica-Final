package cliente;

import java.io.*;
import java.net.Socket;

import comun.LockTicket;
import comun.Locks;
import comun.Mensaje;

public class HiloEmisor extends Thread {
    private Socket _socket;
    private String _miCarpeta;
    private ClienteGUI _ventana;
    private Locks _lockRed;

    public HiloEmisor(Socket socket, String nombreUsuario, ClienteGUI ventana) {
        this._socket = socket;
        this._miCarpeta = "./resources/" + nombreUsuario + "/"; // Buscamos en nuestra carpeta
        this._ventana = ventana;
        this._lockRed = new LockTicket();
    }

    @Override
    public void run() {
        try (
        	ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream());
        	ObjectInputStream in = new ObjectInputStream(_socket.getInputStream())
        ) {
        	// Protocolo P2P
        	// 1. Recibir saludo
        	Mensaje msgSaludo = (Mensaje) in.readObject();
            String solicitante = "Desconocido";
            
            if (msgSaludo.getTipo() == 5) {
                Mensaje.SaludoP2P saludo = (Mensaje.SaludoP2P) msgSaludo;
                solicitante = saludo.getNombreSolicitante();
                _ventana.escribirEnPantalla("[P2P SUBIDA] Conexion entrante de usuario: " + solicitante);
                
                // 2. Confirmar saludo
                _lockRed.enviarMensajeSeguro(0, out, new Mensaje.ConfirmacionSaludoP2P(true));
            } else {
                _lockRed.enviarMensajeSeguro(0, out, new Mensaje.ConfirmacionSaludoP2P(false));
                return;
            }
            
            // 3. Esperar peticion
            Mensaje msgPeticion = (Mensaje) in.readObject();
            if (msgPeticion.getTipo() == 7) {
                Mensaje.PeticionArchivoP2P peticion = (Mensaje.PeticionArchivoP2P) msgPeticion;
                String nombreArchivo = peticion.getNombreArchivo();
                File archivoFisico = new File(_miCarpeta + nombreArchivo);
                
                // 4. Responder estado
                if (archivoFisico.exists()) {
                    _lockRed.enviarMensajeSeguro(0, out, new Mensaje.RespuestaArchivoP2P(true, archivoFisico.length()));
        	
                    // 5. Enviar los bytes
                    FileInputStream fileIn = new FileInputStream(archivoFisico);
                    byte[] buffer = new byte[4096];
                    int leidos;
                    
                    while ((leidos = fileIn.read(buffer)) != -1) {
                        out.write(buffer, 0, leidos); // El ObjectOutputStream permite escribir bytes
                    }
                    out.flush();
                    fileIn.close();
                    
                    in.readObject(); // Espera a que lo reciba antes de cerrar el socket
                    
                    _ventana.escribirEnPantalla("[P2P SUBIDA] Libro '" + nombreArchivo + "' enviado a " + solicitante);
                } else {
                    _lockRed.enviarMensajeSeguro(0, out, new Mensaje.RespuestaArchivoP2P(false, 0));
                    _ventana.escribirEnPantalla("[P2P SUBIDA] " + solicitante + " pidio un libro que no existe.");
                }
            }
        } catch (java.io.EOFException eof) {
        	// No hacemos nada cuando terminamos de enviar
        } catch (Exception e) {
            _ventana.escribirEnPantalla("[P2P SUBIDA] Conexion interrumpida con el cliente.");
        }
    }
}