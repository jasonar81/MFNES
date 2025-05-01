
public class Register400e implements MemoryPort {
	private APU apu;
	
	public Register400e(CPU cpu)
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
		apu.setNoiseMode(Utils.getBit(x, 7));
		apu.setNoisePeriod(x & 0x0f);
	}

}