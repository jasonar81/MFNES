
public class Register4017 implements MemoryPort {
	private GUI gui;
	private Register4016 r4016;
	private APU apu;
	private byte lastValue = 0;
	
	public Register4017(GUI gui, Register4016 r4016, CPU cpu)
	{
		this.r4016 = r4016;
		this.gui = gui;
		apu = cpu.getApu();
	}
	
	public byte read() {
		boolean read = false;
		switch (r4016.counter2)
		{
		case 0:
			read = gui.getA2();
			break;
		case 1:
			read = gui.getB2();
			break;
		case 2:
			read = gui.getSelect2();
			break;
		case 3:
			read = gui.getStart2();
			break;
		case 4:
			read = gui.getUp2();
			break;
		case 5:
			read = gui.getDown2();
			break;
		case 6:
			read = gui.getLeft2();
			break;
		case 7:
			read = gui.getRight2();
			break;
		default:
			read = true;
		}
		
		if (!r4016.hold)
		{
			++r4016.counter2;
		}
		
		if (read)
		{
			return 0x41;
		}
		
		return 0x40;
	}

	@Override
	public void write(byte val) {
		lastValue = val;
		int x = Byte.toUnsignedInt(val) & 0xc0;
		apu.setAudioTimerRegister(x);
		apu.setAudioTimer(-3);
		apu.setQuarterFrameCounter(0);
		apu.setHalfFrameCounter(0);
		
		if (Utils.getBit(x, 7))
		{
			apu.setNextAudioTimerCheckpoint(0);
		}
		else
		{
			apu.setNextAudioTimerCheckpoint(7457);
		}
		
		if (Utils.getBit(x, 6))
		{
			apu.setDisableFrameInterrupt(true);
		}
		else
		{
			apu.setDisableFrameInterrupt(false);
		}
	}
	
	public byte getLastValue()
	{
		return lastValue;
	}

}
