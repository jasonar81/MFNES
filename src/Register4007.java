
public class Register4007 implements MemoryPort {
	private APU apu;
	
	public Register4007(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		if (Utils.getBit(apu.getAudioEnableFlags(), 1))
		{
			int x = (Byte.toUnsignedInt(val) & 0x07) << 8;
			apu.setPulse2InternalTimer((apu.getPulse2InternalTimer() & 0xff) + x);
			
			apu.setPulse2LengthCounter(apu.lengthCounterLookup(Byte.toUnsignedInt(val) >> 3));
		}
		
		apu.setPulse2InternalIndex(0);
		apu.setPulse2StartFlag();
	}

}