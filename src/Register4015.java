
public class Register4015 implements MemoryPort {
	private APU apu;
	
	public Register4015(CPU cpu)
	{
		apu = cpu.getApu();
	}
	
	@Override
	public byte read() {
		int retval = 0;
		if (apu.getDmcInterruptFlag()) 
		{
			retval = Utils.setBit(retval, 7);
		}
		
		if (apu.getFrameInterruptFlag())
		{
			retval = Utils.setBit(retval, 6);
		}
		
		if (apu.getDmcSamplesRemaining() > 0)
		{
			retval = Utils.setBit(retval, 4);
		}
		
		if (apu.getNoiseLengthCounter() > 0 && Utils.getBit(apu.getAudioEnableFlags(), 3))
		{
			retval = Utils.setBit(retval, 3);
		}
		
		if (apu.getTriangleLengthCounter() > 0 && Utils.getBit(apu.getAudioEnableFlags(), 2))
		{
			retval = Utils.setBit(retval, 2);
		}
		
		if (apu.getPulse2LengthCounter() > 0 && Utils.getBit(apu.getAudioEnableFlags(), 1))
		{
			retval = Utils.setBit(retval, 1);
		}
		
		if (apu.getPulse1LengthCounter() > 0 && Utils.getBit(apu.getAudioEnableFlags(), 0))
		{
			retval = Utils.setBit(retval, 0);
		}
		
		apu.clearFrameInterruptFlag();
		return (byte)retval;
	}

	@Override
	public void write(byte val) {
		//System.out.println("Wrote " + String.format("0x%02X", val) + " to 0x4015");
		int x = Byte.toUnsignedInt(val);
		apu.setAudioEnableFlags(x & 0x1f);
		apu.clearDmcInterruptFlag();
		//System.out.println("Cleared DMC interrupt");
		
		if (!Utils.getBit(x, 4))
		{
			apu.zeroDmcRemainingSamples();
		}
		
		if (!Utils.getBit(x, 3))
		{
			apu.setNoiseLengthCounter(0);
		}
		
		if (!Utils.getBit(x, 2))
		{
			apu.setTriangleLengthCounter(0);
		}
		
		if (!Utils.getBit(x, 1))
		{
			apu.setPulse2LengthCounter(0);
		}
		
		if (!Utils.getBit(x, 0))
		{
			apu.setPulse1LengthCounter(0);
		}
		
		//System.out.println("Wrote " + String.format("0x%02X", val) + " to 0x4015");
	}

}
