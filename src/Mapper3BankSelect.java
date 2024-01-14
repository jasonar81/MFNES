
public class Mapper3BankSelect implements MemoryPort {
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper3BankSelect(Memory cpuMem, MemoryPort delegate)
	{
		ppuMem = cpuMem.getPpu().getMem();
		this.delegate = delegate;
	}
	
	@Override
	public byte read() {
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0, Byte.toUnsignedInt(val) * 8192, 8192);
	}

}
