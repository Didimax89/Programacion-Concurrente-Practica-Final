package comun;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class LockTicket implements Locks{
	private AtomicInteger number = new AtomicInteger(0);
    private volatile int next = 0;
    
    @Override
    public void takeLock(int id) {
        int turno = number.getAndIncrement(); // FA
        while (turno != next) {Thread.yield();}
    }

    @Override
    public void releaseLock(int id) {
        next++;
    }
    
    @Override
    public void enviarMensajeSeguro(int id, ObjectOutputStream out, Mensaje msg) throws IOException {
        takeLock(id); // Aseguramos exclusion mutua
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // Limpia la cache de Java para evitar envio de objetos obsoletos
        } finally {
            releaseLock(id); // Liberamos para que otro hilo pueda enviar
        }
    }
}
