
public class Mmc3NametablePort implements MemoryPort {
	private int addr;
	
	public Mmc3NametablePort(int addr)
	{
		this.addr = addr;
	}
	
	@Override
	public byte read() {
		return Mmc3Port.nametableData[addr].read();
	}

	@Override
	public void write(byte val) {
		Mmc3Port.nametableData[addr].write(val);
	}
	
	public void setAddress(int addr)
	{
		this.addr = addr;
	}
}
