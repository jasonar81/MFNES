//Used by AI to see max value of memory address

public class SaveMaxValuePort implements MemoryPort, ClearablePort {
	private byte val;
	private volatile int maxValue = 0;
	
	public SaveMaxValuePort()
	{
		val = 0;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		this.val = val;
		
		if (Byte.toUnsignedInt(val) > maxValue)
		{
			maxValue = Byte.toUnsignedInt(val);
		}
	}
	
	public int getMaxValue()
	{
		return maxValue;
	}
	
	public void clearMaxValue()
	{
		maxValue = 0;
	}
	
}
