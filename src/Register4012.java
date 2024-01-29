
public class Register4012 implements MemoryPort {
	private APU apu;
	
	public Register4012(CPU cpu)
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
		apu.setDmcSampleAddress(x);
		//System.out.println("Wrote " + String.format("0x%02X", val) + " to 0x4012");
	}
}