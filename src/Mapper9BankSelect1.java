
public class Mapper9BankSelect1 implements MemoryPort {
	Memory cpuMem;
	MemoryPort delegate;

	public Mapper9BankSelect1(Memory cpuMem, MemoryPort delegate)
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
		cpuMem.loadRomAtAddress(cpuMem.getCart().prgData(), 0x8000, (Byte.toUnsignedInt(val) & 0x0f) * 8192, 8192);
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
