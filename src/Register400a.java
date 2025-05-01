
public class Register400a implements MemoryPort {
	private APU apu;
	
	public Register400a(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		apu.setTriangleInternalTimer((apu.getTriangleInternalTimer() & 0xff00) + Byte.toUnsignedInt(val));
	}

}