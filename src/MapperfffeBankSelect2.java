
public class MapperfffeBankSelect2 implements MemoryPort {
	private Memory ppuMem;
	private MemoryPort delegate;
	private boolean high;
	
	private static int bank = 0;

	public MapperfffeBankSelect2(Memory cpuMem, MemoryPort delegate, boolean high)
	{
		ppuMem = cpuMem.getPpu().getMem();
		this.delegate = delegate;
		this.high = high;
	}
	
	@Override
	public byte read() {
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		if (!high)
		{
			bank = Byte.toUnsignedInt(val);
		}
		else
		{
			bank += (Byte.toUnsignedInt(val) << 8);
			ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0, bank * (4096 * 3), (4096 * 3));
		}
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}
}
