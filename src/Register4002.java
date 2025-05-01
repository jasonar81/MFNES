
public class Register4002 implements MemoryPort {
	private APU apu;
	
	public Register4002(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		apu.setPulse1InternalTimer((apu.getPulse1InternalTimer() & 0xff00) + Byte.toUnsignedInt(val));
	}

}