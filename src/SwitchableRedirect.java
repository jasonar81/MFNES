
public class SwitchableRedirect implements MemoryPort {
	private Memory mem;
	private boolean useSecond;
	private int addr1;
	private int addr2;
	
	public SwitchableRedirect(Memory mem, boolean useSecond, int addr1, int addr2)
	{
		this.mem = mem;
		this.useSecond = useSecond;
		this.addr1 = addr1;
		this.addr2 = addr2;
	}
	
	@Override
	public byte read() {
		if (!useSecond)
		{
			return mem.getLayout()[addr1].read();
		}
		
		return mem.getLayout()[addr2].read();
	}

	@Override
	public void write(byte val) {
		if (!useSecond)
		{
			mem.getLayout()[addr1].write(val);
		}
		else
		{
			mem.getLayout()[addr2].write(val);
		}
	}
	
	public void useSecond(boolean val)
	{
		useSecond = val;
	}
}
