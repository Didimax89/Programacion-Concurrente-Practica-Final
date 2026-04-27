package comun;
import java.io.Serializable;
import java.util.List;

public abstract class Mensaje implements Serializable {
	private static final long serialVersionUID = 1L; // Esto es necesario siempre que implementamos Serializable
	protected int tipo;

	public Mensaje(int tipo) {
		this.tipo = tipo;
	}
	
	public int getTipo() {
		return tipo;
	}
	
	// TIPO 1: Cliente se conecta al Servidor
    public static class Conexion extends Mensaje {
        private Usuario _usuario;
        
        public Conexion(Usuario usuario) {
            super(1);
            this._usuario = usuario;
        }
        public Usuario getUsuario() { return _usuario; }
    }
    
    // TIPO 2: Servidor confirma la conexion al Cliente
    public static class ConfirmacionConexion extends Mensaje {
        private String _textoConfirmacion;
        
        public ConfirmacionConexion(String texto) {
            super(2);
            this._textoConfirmacion = texto;
        }
        public String getTextoConfirmacion() { return _textoConfirmacion; }
    }
    
    // TIPO 3: Cliente pide el catalogo al Servidor
    public static class BuscarUsuarios extends Mensaje {
        public BuscarUsuarios() {
            super(3);
        }
    }
    
    // TIPO 4: Servidor envia el catalogo al Cliente
    public static class RespuestaBusqueda extends Mensaje {
        private List<Usuario> _usuariosConectados;
        
        public RespuestaBusqueda(List<Usuario> usuarios) {
            super(4);
            this._usuariosConectados = usuarios;
        }
        public List<Usuario> getUsuariosConectados() { return _usuariosConectados; }
    }
    
    // TIPO 5: Saludo inicial P2P
    public static class SaludoP2P extends Mensaje {
        private String _nombreSolicitante;
        
        public SaludoP2P(String nombreSolicitante) {
            super(5);
            this._nombreSolicitante = nombreSolicitante;
        }
        public String getNombreSolicitante() { return _nombreSolicitante; }
    }
    
    // TIPO 6: Confirmacion del saludo
    public static class ConfirmacionSaludoP2P extends Mensaje {
        private boolean _aceptado;
        
        public ConfirmacionSaludoP2P(boolean aceptado) {
            super(6);
            this._aceptado = aceptado;
        }
        public boolean isAceptado() { return _aceptado; }
    }
    
    // TIPO 7: Peticion de un archivo concreto
    public static class PeticionArchivoP2P extends Mensaje {
        private String _nombreArchivo;
        
        public PeticionArchivoP2P(String nombreArchivo) {
            super(7);
            this._nombreArchivo = nombreArchivo;
        }
        public String getNombreArchivo() { return _nombreArchivo; }
    }
    
    // TIPO 8: Respuesta con el estado del archivo y su tamaño
    public static class RespuestaArchivoP2P extends Mensaje {
        private boolean _existe;
        private long _tamanoBytes;
        
        public RespuestaArchivoP2P(boolean existe, long tamanoBytes) {
            super(8);
            this._existe = existe;
            this._tamanoBytes = tamanoBytes;
        }
        public boolean isExiste() { return _existe; }
        public long getTamanoBytes() { return _tamanoBytes; }
    }
    
    // TIPO 9: Cliente avisa al servidor de que tiene un nuevo archivo disponible
    public static class NotificarNuevoArchivo extends Mensaje {
        private static final long serialVersionUID = 1L;
        private String _archivo;
        public NotificarNuevoArchivo(String a) { super(9); this._archivo = a; }
        public String getNombreArchivo() { return _archivo; }
    }

    // TIPO 10: Respuesta de recibo P2P
    public static class FinDescargaP2P extends Mensaje {
        private static final long serialVersionUID = 1L;
        public FinDescargaP2P() { super(10); }
    }
}