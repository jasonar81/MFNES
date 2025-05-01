
public class Register400c implements MemoryPort {
	private APU apu;
	
	public Register400c(CPU cpu)
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
		apu.setNoiseHoldValue(Utils.getBit(x, 5));
		apu.setNoiseConstantVol(Utils.getBit(x, 4));
		apu.setNoiseVolumeDivider(x & 0x0f);
	}

}