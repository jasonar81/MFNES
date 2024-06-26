import java.io.Serializable;
public class Mapper206BankSelect1 implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	Memory cpuMem;
	MemoryPort delegate;

	public Mapper206BankSelect1(Memory cpuMem, MemoryPort delegate)
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
		cpuMem.writeControl(val & 0x07);
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}
}
