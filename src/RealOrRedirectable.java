//Memory port that has backing storage but can be switched to redirect while retaining data

public class RealOrRedirectable implements MemoryPort {
	private Memory mem;
	private boolean real;
	private int address;
	private byte val = 0;
	
	public RealOrRedirectable(Memory mem, boolean real, int address)
	{
		this.mem = mem;
		this.real = real;
		this.address = address;
	}

	@Override
	public byte read() {
		if (!real)
		{
			return mem.getLayout()[address].read();
		}
		
		return val;
	}

	@Override
	public void write(byte val) {
		if (!real)
		{
			mem.getLayout()[address].write(val);
		}
		
		this.val = val;
	}

	public void setReal(boolean real)
	{
		this.real = real;
	}
}
