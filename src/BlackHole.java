//Memory address that does nothing
import java.io.Serializable;

public class BlackHole implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;


	public BlackHole()
	{
	}
	
	@Override
	public byte read() {
		return 0;
	}

	@Override
	public void write(byte val) {
	}
	
}
