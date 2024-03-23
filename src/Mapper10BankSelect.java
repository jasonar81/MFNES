import java.io.Serializable;
public class Mapper10BankSelect implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	Memory cpuMem;
	MemoryPort delegate;

	public Mapper10BankSelect(Memory cpuMem, MemoryPort delegate)
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
		cpuMem.loadRomAtAddressMapper10(cpuMem.getCart().prgData(), 0x8000, (Byte.toUnsignedInt(val) & 0x0f) * 16384, 16384);
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}

}
