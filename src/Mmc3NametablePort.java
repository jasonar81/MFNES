
public class Mmc3NametablePort implements MemoryPort {
	private int addr;
	private Mmc3Data mmc3Data;
	
	public Mmc3NametablePort(int addr, Mmc3Data mmc3Data)
	{
		this.addr = addr;
		this.mmc3Data = mmc3Data;
	}
	
	@Override
	public byte read() {
		return mmc3Data.nametableData[addr].read();
	}

	@Override
	public void write(byte val) {
		mmc3Data.nametableData[addr].write(val);
	}
	
	public void setAddress(int addr)
	{
		this.addr = addr;
	}
}
