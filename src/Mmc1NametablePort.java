
public class Mmc1NametablePort implements MemoryPort {
	private int addr;
	private Mmc1Data mmc1Data;
	
	public Mmc1NametablePort(int addr, Mmc1Data mmc1Data)
	{
		this.addr = addr;
		this.mmc1Data = mmc1Data;
	}
	
	@Override
	public byte read() {
		return mmc1Data.nametableData[addr].read();
	}

	@Override
	public void write(byte val) {
		mmc1Data.nametableData[addr].write(val);
	}
	
	public void setAddress(int addr)
	{
		this.addr = addr;
	}
}
