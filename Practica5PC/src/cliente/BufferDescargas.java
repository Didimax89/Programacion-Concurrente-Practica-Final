package cliente;
import java.util.concurrent.Semaphore;

public class BufferDescargas {
	private PeticionDescarga[] _buffer;
	private int _in = 0;
	private int _out = 0;

	// Semaforos
	private Semaphore _huecos;     // Cuenta cuantos huecos libres quedan
	private Semaphore _productos;  // Cuenta cuantos archivos hay listos para descargar
	private Semaphore _mutex;      // Protege las variables 'in' y 'out' (exclusion mutua)

	public BufferDescargas(int capacidad) {
		_buffer = new PeticionDescarga[capacidad];
		_huecos = new Semaphore(capacidad);
		_productos = new Semaphore(0);
		_mutex = new Semaphore(1);
	}

	// El Productor (menu del usuario) llama a este metodo
	public void añadirDescarga(PeticionDescarga peticion) {
		try {
			_huecos.acquire(); // Esperamos a que haya un hueco libre en el buffer
			_mutex.acquire();  // Bloqueamos para que nadie mas toque el array
			
			_buffer[_in] = peticion;
			_in = (_in + 1) % _buffer.length; // Avanzamos de forma circular

			_mutex.release();  // Soltamos el array
			_productos.release(); // Avisamos de que hay un nuevo producto (descarga) listo
		} catch (InterruptedException e) {}
	}

	// El Consumidor (hilo en segundo plano) llama a este metodo
	public PeticionDescarga extraerDescarga() {
		PeticionDescarga peticion = null;
		try {
			_productos.acquire(); // Esperamos a que haya algo que descargar
			_mutex.acquire();     // Bloqueamos el array

			peticion = _buffer[_out];
			_out = (_out + 1) % _buffer.length;

			_mutex.release();     // Soltamos el array
			_huecos.release();    // Avisamos de que vuelve a haber un hueco libre
		} catch (InterruptedException e) {}
		return peticion;
	}
}