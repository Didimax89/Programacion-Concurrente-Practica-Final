package comun;

import java.io.IOException;
import java.io.ObjectOutputStream;

public interface Locks {
    void takeLock(int id);
    void releaseLock(int id);
    
    void enviarMensajeSeguro(int id, ObjectOutputStream out, Mensaje msg) throws IOException;
}