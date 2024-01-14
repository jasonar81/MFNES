
public class Mapper7BankSelect implements MemoryPort {
	Memory cpuMem;
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper7BankSelect(Memory cpuMem, MemoryPort delegate)
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
		ppuMem.writeControl(val >> 4);
		cpuMem.loadRomAtAddressMapper7(cpuMem.getCart().prgData(), 0x8000, (Byte.toUnsignedInt(val) & 0x07) * 32768, 32768);
	}

	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}
}
