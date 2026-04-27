package cliente;

public class PeticionDescarga {
    private String _nombreArchivo;
    private String _ipPropietario;
    private int _puertoPropietario;

    public PeticionDescarga(String nombreArchivo, String ip, int puerto) {
        this._nombreArchivo = nombreArchivo;
        this._ipPropietario = ip;
        this._puertoPropietario = puerto;
    }

    public String getArchivo() { return _nombreArchivo; }
    public String getIp() { return _ipPropietario; }
    public int getPuerto() { return _puertoPropietario; }
}