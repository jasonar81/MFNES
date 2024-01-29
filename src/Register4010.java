
public class Register4010 implements MemoryPort {
	private APU apu;
	
	public Register4010(CPU cpu)
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
		apu.setDmcIrqEnabled(Utils.getBit(x, 7));
		apu.setDmcLoop(Utils.getBit(x, 6));
		apu.setDmcRate(x & 0x0f);
		//System.out.println("Wrote " + String.format("0x%02X", val) + " to 0x4010");
	}

}