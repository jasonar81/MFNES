
public class MapperffffBankSelect1 implements MemoryPort {
	private Memory cpuMem;
	private MemoryPort delegate;
	private boolean high;
	
	private static int bank = 0;

	public MapperffffBankSelect1(Memory cpuMem, MemoryPort delegate, boolean high)
	{
		this.cpuMem = cpuMem;
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
			//System.out.println("Low write of " + String.format("0x%02X", val));
			bank = Byte.toUnsignedInt(val);
		}
		else
		{
			//System.out.println("High write of " + String.format("0x%02X", val));
			bank += (Byte.toUnsignedInt(val) << 8);
			//System.out.println("Bank = " + bank);
			cpuMem.loadRomAtAddressMapperffff(cpuMem.getCart().prgData(), 0x8000, bank * 16384, 16384);
		}
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
