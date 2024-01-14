
public class Mapper66BankSelect implements MemoryPort {
	Memory cpuMem;
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper66BankSelect(Memory cpuMem, MemoryPort delegate)
	{
		this.cpuMem = cpuMem;
		ppuMem = cpuMem.getPpu().getMem();
		this.delegate = delegate;
	}
	
	@Override
	public byte read() {
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		int value = Byte.toUnsignedInt(val) & 0x3f;
		int ppuBank = value & 0x03;
		int cpuBank = (value >> 4) & 0x03;
		
		ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0, ppuBank * 8192, 8192);
		cpuMem.loadRomAtAddressMapper66(cpuMem.getCart().prgData(), 0x8000, cpuBank * 32768, 32768);
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
