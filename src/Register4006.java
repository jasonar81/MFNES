
public class Register4006 implements MemoryPort {
	private APU apu;
	
	public Register4006(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		apu.setPulse2InternalTimer((apu.getPulse2InternalTimer() & 0xff00) + Byte.toUnsignedInt(val));
	}

}