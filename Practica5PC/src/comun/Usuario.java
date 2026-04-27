package comun;
import java.io.Serializable;
import java.util.List;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	private String _nombre;
	private String _ip;
	private int _puertoP2P; // El puerto donde este cliente escuchara a otros clientes
	private List<String> _archivosCompartidos;

	public Usuario(String nombre, String ip, int puertoP2P, List<String> archivos) {
		this._nombre = nombre;
		this._ip = ip;
		this._puertoP2P = puertoP2P;
		this._archivosCompartidos = archivos;
	}

	public String getNombre() { return _nombre; }
	public String getIp() { return _ip; }
	public int getPuertoP2P() { return _puertoP2P; }
	public List<String> getArchivosCompartidos() { return _archivosCompartidos; }
}