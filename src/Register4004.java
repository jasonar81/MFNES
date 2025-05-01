
public class Register4004 implements MemoryPort {
	private APU apu;
	
	public Register4004(CPU cpu)
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
		apu.setPulse2DutyCycle((x & 0xc0) >> 6);
		apu.setPulse2HoldValue(Utils.getBit(x, 5));
		apu.setPulse2ConstantVol(Utils.getBit(x, 4));
		apu.setPulse2VolumeDivider(x & 0x0f);
	}

}