//Read only memory address

public class RomMemoryPort implements MemoryPort {
	private byte val;
	
	public RomMemoryPort(byte val)
	{
		this.val = val;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
	}
	
	public void forceWrite(byte val)
	{
		this.val = val;
	}
	
}
