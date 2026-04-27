package servidor;

import comun.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BaseDeDatosServidor {
	// Monitor Lectores-Escritores
	private int _nr = 0; // Num de lectores leyendo
	private int _nw = 0; // Num de escritores escribiendo

	private final Lock _lock = new ReentrantLock();
	private final Condition _okToRead = _lock.newCondition();
	private final Condition _okToWrite = _lock.newCondition();

	// Datos compartidos
	private List<Usuario> _usuariosConectados = new ArrayList<>();

	// Busqueda de usuarios/archivos (lectura)
	public void requestRead() {
		_lock.lock(); // Entramos al monitor
		try {
			while (_nw > 0) { // Esperamos si hay alguien escribiendo
				_okToRead.await();
			}
			_nr++; // Entra un lector
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			_lock.unlock(); // Salimos del monitor
		}
	}

	public void releaseRead() {
		_lock.lock();
		try {
			_nr--; // Sale un lector
			if (_nr == 0) {
				_okToWrite.signal(); // Avisamos a 1 escritor si ya no hay mas lectores
			}
		} finally {
			_lock.unlock();
		}
	}

	// Conectar / Desconectar usuarios (escritura)
	public void requestWrite() {
		_lock.lock();
		try {
			// Esperamos si hay lectores leyendo o algun escritor escribiendo
			while (_nr > 0 || _nw > 0) {
				_okToWrite.await();
			}
			_nw++; // Entra el escritor
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			_lock.unlock();
		}
	}

	public void releaseWrite() {
		_lock.lock();
		try {
			_nw--; // Sale el escritor
			_okToWrite.signal();     // si hay escritores, avisamos a otro
			_okToRead.signalAll();   // Avisamos a todos los lectores esperando
		} finally {
			_lock.unlock();
		}
	}

	// Operaciones Base de Datos

	// Cuando un cliente hace login, el escritor usa:
	public void registrarUsuario(Usuario usuario) {
        requestWrite();
        try {
            _usuariosConectados.add(usuario);
            System.out.println("Servidor: Usuario " + usuario.getNombre() + " registrado con sus libros.");
        } finally {
            releaseWrite();
        }
    }

	// Cuando un cliente busca, el lector usa:
	public List<Usuario> buscarUsuarios() {
        requestRead();
        try {
            System.out.println("Servidor: Enviando catalogo de libros...");
            return new ArrayList<>(_usuariosConectados); 
        } finally {
            releaseRead();
        }
    }
	
	// Elimina a un usuario cuando se desconecta
	public void eliminarUsuario(Usuario usuario) {
        if (usuario == null) return;
        requestWrite(); // Pedimos permiso de escritura en el monitor
        try {
            // Buscamos al usuario por su nombre y lo borramos de la lista
            _usuariosConectados.removeIf(u -> u.getNombre().equals(usuario.getNombre()));
            System.out.println("Servidor: Usuario desconectado y eliminado: " + usuario.getNombre());
        } finally {
            releaseWrite();
        }
    }
	
	public void anadirArchivoAUsuario(String nombreUsuario, String archivo) {
        requestWrite(); // Exclusion mutua para escribir
        try {
            for (Usuario u : _usuariosConectados) {
                if (u.getNombre().equals(nombreUsuario)) {
                    if (!u.getArchivosCompartidos().contains(archivo)) {
                        u.getArchivosCompartidos().add(archivo);
                        System.out.println("Servidor: Nuevo libro '" + archivo + "' añadido al catalogo de " + nombreUsuario);
                    }
                    break;
                }
            }
        } finally {
            releaseWrite();
        }
    }
}