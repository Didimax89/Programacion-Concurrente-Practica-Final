package cliente;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import comun.LockTicket;
import comun.Locks;
import comun.Mensaje;
import comun.Usuario;

public class OyenteServidor extends Thread {
    private String _ipServidor;
    private int _puertoServidor;
    private ClienteGUI _ventana;
    private Usuario _miUsuario;
    
    private Socket _socket;
    private ObjectOutputStream _out;
    private ObjectInputStream _in;
    private Locks _lockRed;

    public OyenteServidor(String ip, int puerto, Usuario usuario, ClienteGUI ventana) {
        this._ipServidor = ip;
        this._puertoServidor = puerto;
        this._miUsuario = usuario;
        this._ventana = ventana;
        this._lockRed = new LockTicket();
    }
    
    // 1. Metodo para establecer la conexion inicial
    public void conectar() throws Exception {
        _socket = new Socket(_ipServidor, _puertoServidor);
        _out = new ObjectOutputStream(_socket.getOutputStream());
        _in = new ObjectInputStream(_socket.getInputStream());

        // Enviamos el mensaje de conexion inicial
        Mensaje.Conexion msgConexion = new Mensaje.Conexion(_miUsuario);
        _lockRed.enviarMensajeSeguro(0, _out, msgConexion);
        
        // Arrancamos el hilo para que empiece a escuchar las respuestas
        this.start(); 
    }

    // 2. Metodo para que la GUI le pida buscar usuarios
    public void pedirCatalogo() {
        try {
            _lockRed.enviarMensajeSeguro(0, _out, new Mensaje.BuscarUsuarios());
        } catch (Exception e) {
            _ventana.escribirEnPantalla("Error al pedir el catalogo al servidor.");
        }
    }

    // 3. Metodo para avisar al servidor de que hemos descargado algo
    public void notificarNuevoArchivo(String archivo) {
        try {
            _lockRed.enviarMensajeSeguro(0, _out, new Mensaje.NotificarNuevoArchivo(archivo));
        } catch (Exception e) {}
    }

    // 4. Metodo para desconectarnos
    public void desconectar() {
        try {
            if (_socket != null && !_socket.isClosed()) {
                _socket.close();
            }
        } catch (Exception e) {}
    }

    // 5. Escucha constantemente 
    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = (Mensaje) _in.readObject();
                
                // Si el servidor nos responde con el catalogo, actualizamos la GUI
                if (m instanceof Mensaje.RespuestaBusqueda) {
                    Mensaje.RespuestaBusqueda res = (Mensaje.RespuestaBusqueda) m;
                    _ventana.actualizarCatalogo(res.getUsuariosConectados());
                }
            }
        } catch (Exception e) {
            _ventana.escribirEnPantalla("Desconectado del servidor principal.");
        }
    }
}