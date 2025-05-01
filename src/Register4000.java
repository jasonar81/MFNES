
public class Register4000 implements MemoryPort {
	private APU apu;
	
	public Register4000(CPU cpu)
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
		apu.setPulse1DutyCycle((x & 0xc0) >> 6);
		apu.setPulse1HoldValue(Utils.getBit(x, 5));
		apu.setPulse1ConstantVol(Utils.getBit(x, 4));
		apu.setPulse1VolumeDivider(x & 0x0f);
	}

}