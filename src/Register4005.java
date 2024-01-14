
public class Register4005 implements MemoryPort {
	private APU apu;
	
	public Register4005(CPU cpu)
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
		apu.setPulse2SweepEnabled(Utils.getBit(x, 7));
		apu.setPulse2SweepNegate(Utils.getBit(x, 3));
		apu.setPulse2SweepShift(x & 0x07);
		
		int p = (x >> 4) & 0x07;
		apu.setPulse2SweepReload(p);
		apu.setPulse2SweepCounter(p);
		apu.setPulse2SweepReloadFlag();
	}

}