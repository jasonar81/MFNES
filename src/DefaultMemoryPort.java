//The default implementation of a memory address
import java.io.Serializable;

public class DefaultMemoryPort implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private byte val;
	
	public DefaultMemoryPort()
	{
		val = 0;
	}
	
	public DefaultMemoryPort(byte val)
	{
		this.val = val;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		this.val = val;
	}
	
}
