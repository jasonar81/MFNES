
public class Register400b implements MemoryPort {
	private APU apu;
	
	public Register400b(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		return (byte)0;
	}

	@Override
	public void write(byte val) {
		if (Utils.getBit(apu.getAudioEnableFlags(), 2))
		{
			int x = (Byte.toUnsignedInt(val) & 0x07) << 8;
			apu.setTriangleInternalTimer((apu.getTriangleInternalTimer() & 0xff) + x);
			
			apu.setTriangleLengthCounter(apu.lengthCounterLookup(Byte.toUnsignedInt(val) >> 3));
		}
		
		apu.setLinearCounterReloadFlag(false);
	}

}