
public class Register400f implements MemoryPort {
	private APU apu;
	
	public Register400f(CPU cpu)
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
		apu.setNoiseStartFlag();
		apu.setNoiseReload(x >> 3);
		
		if (Utils.getBit(apu.getAudioEnableFlags(), 3))
		{
			apu.setNoiseLengthCounter(apu.lengthCounterLookup(x >> 3));
		}
	}

}