package cliente;
import javax.swing.*;

import comun.Mensaje;
import comun.Usuario;
import comun.Locks;
import comun.LockTicket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ClienteGUI extends JFrame {

	private static final String IP_SERVIDOR = "localhost";
	private static final int PUERTO_SERVIDOR = 5000;

	private JTextArea _areaTexto;
	private BufferDescargas _buffer;
	private Usuario _usuario;
	private ObjectOutputStream _out;
	private Socket _socket;
	private List<Usuario> _catalogoRed = new ArrayList<>();
	private Locks _lockRed = new LockTicket();

	public ClienteGUI() {
		// 1. Pedir nombre e IP del servidor
		String ipServidor = JOptionPane.showInputDialog("Introduce la IP del servidor principal:", "localhost");
		if (ipServidor == null || ipServidor.trim().isEmpty()) {
			System.exit(0);
		}

		String nombre = JOptionPane.showInputDialog(null, "Introduce tu nombre de usuario:", "Inicio de Sesion", JOptionPane.QUESTION_MESSAGE);
		if (nombre == null || nombre.trim().isEmpty()) {
			System.exit(0); // Cerramos si ha cancelado
		}
		
		// 2. Comprobar/Crear carpeta y leer archivos
        GestorArchivos gestor = new GestorArchivos(nombre);
        List<String> misArchivos = gestor.obtenerArchivosLocales();
        
        String miIpReal = "127.0.0.1";
        try {
            miIpReal = InetAddress.getLocalHost().getHostAddress(); // Coge la IP real de tu tarjeta de red (ej. 192.168.1.50)
        } catch (Exception e) {
            System.out.println("No se pudo obtener la IP real, usando localhost.");
        }
		
		int miPuertoP2P = (int)(Math.random() * 1000 + 6000); // TODO mirar esto del random
		_usuario = new Usuario(nombre, miIpReal, miPuertoP2P, misArchivos);
		_buffer = new BufferDescargas(5); // Buffer protegido por semaforos

		// 2. Configurar la ventana principal
		setTitle("App P2P de " + _usuario.getNombre());
		setSize(500, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// 3. Crear el area de texto donde salen los mensajes
		_areaTexto = new JTextArea();
		_areaTexto.setEditable(false);
		JScrollPane scroll = new JScrollPane(_areaTexto);
		add(scroll, BorderLayout.CENTER);

		// 4. Crear el panel de botones abajo
		JPanel panelBotones = new JPanel();
		JButton btnBuscar = new JButton("Buscar Catalogo");
		JButton btnDescargar = new JButton("Descargar Archivo");
		JButton btnSalir = new JButton("Salir");

		panelBotones.add(btnBuscar);
		panelBotones.add(btnDescargar);
		panelBotones.add(btnSalir);
		add(panelBotones, BorderLayout.SOUTH);

		// 5. Eventos de los botones
		btnBuscar.addActionListener(e -> {
            escribirEnPantalla(">> Refrescando catalogo de libros...");
            try {
            	_lockRed.enviarMensajeSeguro(0, _out, new Mensaje.BuscarUsuarios());
            } catch (Exception ex) {}
        });

		btnDescargar.addActionListener(e -> {
            if (_catalogoRed.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero pulsa 'Buscar Usuarios' para cargar el catalogo.");
                return;
            }

            // Preparamos una lista visual para el menu desplegable
            List<String> opcionesMenu = new ArrayList<>();
            for (Usuario u : _catalogoRed) {
                // No mostramos nuestros propios libros
                if (!u.getNombre().equals(_usuario.getNombre())) {
                    for (String libro : u.getArchivosCompartidos()) {
                        opcionesMenu.add(libro + " (de " + u.getNombre() + ")");
                    }
                }
            }

            if (opcionesMenu.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay ningun libro disponible en la red ahora mismo.");
                return;
            }

            // Mostramos el menu desplegable (ComboBox automatico de Java Swing)
            String[] arrayOpciones = opcionesMenu.toArray(new String[0]);
            String eleccion = (String) JOptionPane.showInputDialog(
                    this, "Selecciona el libro que quieres descargar:", "Catálogo de Libros",
                    JOptionPane.QUESTION_MESSAGE, null, arrayOpciones, arrayOpciones[0]);

            // Si elige algo y le da a aceptar, buscamos quien era el dueño para crear la peticion
            if (eleccion != null) {
                for (Usuario u : _catalogoRed) {
                    for (String libro : u.getArchivosCompartidos()) {
                        if (eleccion.equals(libro + " (de " + u.getNombre() + ")")) {
                            escribirEnPantalla(">> Has pedido el libro: " + libro);
                            // Metemos la peticion completa al Buffer (el hilo la sacara y sabra que IP/puerto usar)
                            _buffer.añadirDescarga(new PeticionDescarga(libro, u.getIp(), u.getPuertoP2P()));
                            return;
                        }
                    }
                }
            }
        });

		btnSalir.addActionListener(e -> {
			escribirEnPantalla("Desconectando...");
			System.exit(0); // TODO Aqui enviariamos el mensaje de desconexión al servidor
		});

		// 6. Conectar al servidor y arrancar los hilos
		conectarServidor();
		arrancarHilosDescarga();
		
		// Arrancamos nuestro propio hilo para que otros puedan descargarnos archivos
		new EscuchadorP2P(_usuario.getPuertoP2P(), _usuario.getNombre(), this).start();
	}

	// Metodo para conectar con el servidor
	private void conectarServidor() {
		try {
			_socket = new Socket(IP_SERVIDOR, PUERTO_SERVIDOR);
			_out = new ObjectOutputStream(_socket.getOutputStream());
			
			Mensaje.Conexion msgConexion = new Mensaje.Conexion(_usuario);
			_lockRed.enviarMensajeSeguro(0, _out, msgConexion);
			
			escribirEnPantalla("Conectado al servidor principal con exito");
			
			new OyenteServidor(_socket, this).start(); // Empezamos a escuchar
		} catch (Exception e) {
			escribirEnPantalla("ERROR: No se pudo conectar al servidor");
		}
	}

	// Metodo para arrancar los Consumidores
	private void arrancarHilosDescarga() {
		Estadisticas stats = new Estadisticas();

		// Arrancamos 2 hilos con ids 0 y 1 para poder descargar 2 cosas a la vez
		new HiloDescargador(_buffer, this, stats, 0, _usuario.getNombre()).start();
        new HiloDescargador(_buffer, this, stats, 1, _usuario.getNombre()).start();
	}

	// Metodo utilitario para que cualquier hilo pueda escribir en la pantalla de forma segura
	public void escribirEnPantalla(String mensaje) {
		SwingUtilities.invokeLater(() -> {
			_areaTexto.append(mensaje + "\n");
			// Autoscroll hacia abajo
			_areaTexto.setCaretPosition(_areaTexto.getDocument().getLength());
		});
	}
	
	public void actualizarCatalogo(List<Usuario> usuarios) {
        this._catalogoRed = usuarios;
        escribirEnPantalla("\n CATALOGO DE LIBROS ACTUALIZADO");
        for (Usuario u : _catalogoRed) {
            if (!u.getNombre().equals(_usuario.getNombre()) && !u.getArchivosCompartidos().isEmpty()) {
                escribirEnPantalla("📚 Libreria de " + u.getNombre() + ":");
                for (String libro : u.getArchivosCompartidos()) {
                    escribirEnPantalla("   - " + libro);
                }
            }
        }
        escribirEnPantalla("--------------------------------------\n");
    }
	
	public void notificarDescargaCompletada(String archivo) {
        // Lo añadimos a nuestra lista local
        if (!_usuario.getArchivosCompartidos().contains(archivo)) {
            _usuario.getArchivosCompartidos().add(archivo);
        }
        // Avisamos al servidor central
        try {
            _lockRed.enviarMensajeSeguro(0, _out, new Mensaje.NotificarNuevoArchivo(archivo));
        } catch (Exception e) {}
    }

	// El Main arranca la Interfaz Grafica
	public static void main(String[] args) {
		// Aseguramos que la GUI se crea en el hilo de eventos correcto
		SwingUtilities.invokeLater(() -> {
			ClienteGUI ventana = new ClienteGUI();
			ventana.setLocationRelativeTo(null); // Centrar en pantalla
			ventana.setVisible(true); // Mostrar
		});
	}
}