
public class Register4003 implements MemoryPort {
	private APU apu;
	
	public Register4003(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		if (Utils.getBit(apu.getAudioEnableFlags(), 0))
		{
			int x = (Byte.toUnsignedInt(val) & 0x07) << 8;
			apu.setPulse1InternalTimer((apu.getPulse1InternalTimer() & 0xff) + x);
			
			apu.setPulse1LengthCounter(apu.lengthCounterLookup(Byte.toUnsignedInt(val) >> 3));
			//System.out.println("Setting length counter to " + apu.lengthCounterLookup(Byte.toUnsignedInt(val) >> 3));
		}
		
		apu.setPulse1InternalIndex(0);
		apu.setPulse1StartFlag();
	}

}