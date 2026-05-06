package comun;

public class Consola {
    public static synchronized void escribir(String mensaje) { // Exclusion mutual al escribir en la terminal al ser synchronized
        System.out.println(mensaje);
    }
}