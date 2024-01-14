//The default implementation of a memory address

public class DefaultMemoryPort implements MemoryPort {
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
