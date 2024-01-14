//Debugging memory port that allows you to see/log writes

public class MonitorMemoryPort implements MemoryPort {
	private byte val;
	private int addr;
	
	public MonitorMemoryPort(int addr)
	{
		val = 0;
		this.addr = addr;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		this.val = val;
		System.out.println("Wrote " + String.format("0x%02X", val) + " to " + String.format("0x%04X", addr));
	}
	
}
