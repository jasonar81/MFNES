
public class Mapper71BankSelect implements MemoryPort {
	Memory cpuMem;
	MemoryPort delegate;

	public Mapper71BankSelect(Memory cpuMem, MemoryPort delegate)
	{
		this.cpuMem = cpuMem;
		this.delegate = delegate;
	}
	
	@Override
	public byte read() {
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		cpuMem.loadRomAtAddress(cpuMem.getCart().prgData(), 0x8000, (Byte.toUnsignedInt(val) & 0x0f) * 16384, 8192);
		cpuMem.loadRomAtAddressMapper71(cpuMem.getCart().prgData(), 0xC000, (Byte.toUnsignedInt(val) & 0x0f) * 16384 + 0x4000, 8192);
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
