import java.io.Serializable;
public class Mapper206BankSelect2 implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	Memory cpuMem;
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper206BankSelect2(Memory cpuMem, MemoryPort delegate)
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
		int select = cpuMem.getControl();
		if (select == 0 || select == 1)
		{
			value &= 0xfe;
			ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), select * 0x800, value * 1024, 2048);
		}
		else if (select < 6)
		{
			ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1000 + (select - 2) * 0x400, value * 1024, 1024);
		}
		else
		{
			value &= 0x0f;
			cpuMem.loadRomAtAddressMapper206(cpuMem.getCart().prgData(), 0x8000 + (select - 6) * 0x2000, value * 8192, 8192);
		}
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
