
public class Register4001 implements MemoryPort {
	private APU apu;
	
	public Register4001(CPU cpu)
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
		apu.setPulse1SweepEnabled(Utils.getBit(x, 7));
		apu.setPulse1SweepNegate(Utils.getBit(x, 3));
		apu.setPulse1SweepShift(x & 0x07);
		
		int p = (x >> 4) & 0x07;
		apu.setPulse1SweepReload(p);
		apu.setPulse1SweepCounter(p);
		apu.setPulse1SweepReloadFlag();
	}

}