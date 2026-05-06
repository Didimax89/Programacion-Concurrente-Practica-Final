package servidor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ControladorBD {
	// Monitor Lectores-Escritores
	private int _nr = 0; // Num de lectores leyendo
	private int _nw = 0; // Num de escritores escribiendo

	private final Lock _lock = new ReentrantLock();
	private final Condition _okToRead = _lock.newCondition();
	private final Condition _okToWrite = _lock.newCondition();

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
}