
public class Register4011 implements MemoryPort {
	private APU apu;
	
	public Register4011(CPU cpu)
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
		apu.setDmcChannelValue(x & 0x7f);
	}

}