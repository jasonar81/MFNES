
public class Register4008 implements MemoryPort {
	private APU apu;
	
	public Register4008(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		int x = Byte.toUnsignedInt(val);
		boolean c = Utils.getBit(x, 7);
		apu.setTriangleHoldValue(c);
		apu.setTriangleLinearCounterReloadValue(x & 0x7f);
		
		if (!c)
		{
			apu.setLinearCounterReloadFlag(false);
		}
	}

}
