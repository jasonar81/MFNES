
public class Mmc1NametablePort implements MemoryPort {
	private int addr;
	
	public Mmc1NametablePort(int addr)
	{
		this.addr = addr;
	}
	
	@Override
	public byte read() {
		return Mmc1Port.nametableData[addr].read();
	}

	@Override
	public void write(byte val) {
		Mmc1Port.nametableData[addr].write(val);
	}
	
	public void setAddress(int addr)
	{
		this.addr = addr;
	}
}
