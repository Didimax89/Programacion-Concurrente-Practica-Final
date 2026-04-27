package cliente;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;

import comun.LockTicket;
import comun.Locks;
import comun.Mensaje;

public class HiloDescargador extends Thread {
	private BufferDescargas _buffer;
	private ClienteGUI _ventanaCliente;
	private Estadisticas _estadisticas;
    private int _idHilo; // Identificador para el Lock
    private String _miCarpeta;
	private String _miNombre;
	private Locks _lockRed; // Lock para enviar mensajes seguros
    

	public HiloDescargador(BufferDescargas buffer, ClienteGUI ventanaCliente, Estadisticas estadisticas, int idHilo, String miNombre) {
		this._buffer = buffer;
		this._ventanaCliente = ventanaCliente;
		this._estadisticas = estadisticas;
		this._idHilo = idHilo;
		this._miCarpeta = "./resources/" + miNombre + "/"; // Carpeta donde guardaremos lo descargado
		this._miNombre = miNombre;
		this._lockRed = new LockTicket();
	}

	@Override
	public void run() {
		while (true) {
			// Se quedara bloqueado gracias al semaforo hasta que haya algo en el buffer
			PeticionDescarga peticion= _buffer.extraerDescarga(); 
			
			_ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | Contactando con " + peticion.getIp() + "...");
			
			// Usamos la IP y el puerto reales del dueño que venian en la peticion
            try (Socket socketP2P = new Socket(peticion.getIp(), peticion.getPuerto());
            	ObjectOutputStream out = new ObjectOutputStream(socketP2P.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socketP2P.getInputStream())) {
            	
            	// Protocolo P2P
            	// 1. Saludo
            	_lockRed.enviarMensajeSeguro(_idHilo, out, new Mensaje.SaludoP2P(_miNombre));
                
                // 2. Esperar confirmacion
            	Mensaje msgConfirmacion = (Mensaje) in.readObject();
                if (msgConfirmacion.getTipo() == 6) {
                    Mensaje.ConfirmacionSaludoP2P conf = (Mensaje.ConfirmacionSaludoP2P) msgConfirmacion;
                    if (!conf.isAceptado()) {
                        _ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | Protocolo rechazado por el usuario.");
                        continue;
                    }
                }
            	
                // 3. Pedir archivo
                _lockRed.enviarMensajeSeguro(_idHilo, out, new Mensaje.PeticionArchivoP2P(peticion.getArchivo()));
                
                // 4. Recibir estado
                Mensaje msgRespuesta = (Mensaje) in.readObject();
                if (msgRespuesta.getTipo() == 8) {
                    Mensaje.RespuestaArchivoP2P resp = (Mensaje.RespuestaArchivoP2P) msgRespuesta;
                    
                    if (resp.isExiste()) {
                        long tamano = resp.getTamanoBytes();
                        _ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | Solicitud ACEPTADA. Descargando " + tamano + " bytes.");
			
                        // 5. Descarga de los bytes
                        FileOutputStream fileOut = new FileOutputStream(_miCarpeta + peticion.getArchivo());
                        byte[] bufferBytes = new byte[4096];
                        int leidos;
                        long totalLeido = 0;
                        
                        while (totalLeido < tamano && (leidos = in.read(bufferBytes)) != -1) {
                            fileOut.write(bufferBytes, 0, leidos);
                            totalLeido += leidos;
                        }
                        fileOut.close();
                        _lockRed.enviarMensajeSeguro(_idHilo, out, new Mensaje.FinDescargaP2P());
                        
                        _estadisticas.registrarDescarga(_idHilo);
                        _ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | DESCARGA COMPLETADA: " + peticion.getArchivo());
                        
                        _ventanaCliente.notificarDescargaCompletada(peticion.getArchivo());
                    } else {
                        _ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | SOLICITUD RECHAZADA. Archivo no encontrado.");
                    }
                }
            } catch (java.io.EOFException eof) {
                // No hacemos nada cuando la descarga termina
            } catch (Exception e) {
                _ventanaCliente.escribirEnPantalla("Hilo " + _idHilo + " | Error de red P2P: " + e.getMessage());
            }
	    }
	}
}