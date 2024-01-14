
public class Mapper9BankSelect5 implements MemoryPort {
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper9BankSelect5(Memory cpuMem, MemoryPort delegate)
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
		//System.out.println("Mapper 9 select 5 = " + String.format("0x%02X", val));
		ppuMem.loadState2AtAddress(ppuMem.getCart().chrData(), 0x1000, (Byte.toUnsignedInt(val) & 0x1f) * 4096, 4096);
	}
}
