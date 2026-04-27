package comun;

public class Entero {
	private volatile int n = 0;
	
	Entero(){
		n = 0;
	}
	
	public void incrementar() {
		n++;
	}
	
	public void decrementar() {
		n--;
	}
	
	public int getValor() {
        return n;
    }
}