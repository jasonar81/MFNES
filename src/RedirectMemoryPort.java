//Memory port that just redirects elsewhere

public class RedirectMemoryPort implements MemoryPort {
	private Memory mem;
	private int addr;
	
	public RedirectMemoryPort(int addr, Memory mem)
	{
		this.addr = addr;
		this.mem = mem;
	}
	
	@Override
	public byte read() {
		return mem.getLayout()[addr].read();
	}

	@Override
	public void write(byte val) {
		mem.getLayout()[addr].write(val);
	}
	
	public void changeRedirect(int addr)
	{
		this.addr = addr;
	}
}
