package cliente;

import comun.LockTicket;
import comun.Locks;

public class Estadisticas {
	private int _totalDescargas = 0;
	private Locks _lock;

	public Estadisticas() {
		this._lock = new LockTicket(); 
	}

	// Este metodo lo llamaran los Hilos Descargadores cuando terminen
	public void registrarDescarga(int idHilo) {
		_lock.takeLock(idHilo); // Protocolo de entrada (exclusion mutua)
		try {
			_totalDescargas++;
		} finally {
			_lock.releaseLock(idHilo); // Protocolo de salida
		}
	}

	public int getTotalDescargas() {
		return _totalDescargas;
	}
}