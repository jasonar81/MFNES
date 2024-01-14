
public class Mapper9MirroringSelect implements MemoryPort {
	Memory ppuMem;
	MemoryPort delegate;

	public Mapper9MirroringSelect(Memory cpuMem, MemoryPort delegate)
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
		if (Utils.getBit(Byte.toUnsignedInt(val), 0))
		{
			for (int i = 0x2400; i < 0x2800; ++i)
			{
				((RealOrRedirectable)ppuMem.getLayout()[i]).setReal(false);
			}
			
			for (int i = 0x2800; i < 0x2c00; ++i)
			{
				((RealOrRedirectable)ppuMem.getLayout()[i]).setReal(true);
			}
			
			for (int i = 0x2c00; i < 0x3000; ++i)
			{
				((SwitchableRedirect)ppuMem.getLayout()[i]).useSecond(false);
			}
		}
		else
		{
			for (int i = 0x2400; i < 0x2800; ++i)
			{
				((RealOrRedirectable)ppuMem.getLayout()[i]).setReal(true);
			}
			
			for (int i = 0x2800; i < 0x2c00; ++i)
			{
				((RealOrRedirectable)ppuMem.getLayout()[i]).setReal(false);
			}
			
			for (int i = 0x2c00; i < 0x3000; ++i)
			{
				((SwitchableRedirect)ppuMem.getLayout()[i]).useSecond(true);
			}
		}
	}
}
