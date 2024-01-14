//Another wacky AI port

public class SaveMaxValueAndClearElsewherePort implements MemoryPort, ClearablePort {
	private byte val;
	private volatile int maxValue = 0;
	private ClearablePort other;
	private boolean onZero;
	private boolean progress;
	private AiAgent agent;
	private Clock clock;
	
	public SaveMaxValueAndClearElsewherePort(MemoryPort other, boolean onZero, boolean progressOnChange, AiAgent agent, Clock clock)
	{
		val = 0;
		this.other = (ClearablePort)other;
		this.onZero = onZero;
		progress = progressOnChange;
		this.agent = agent;
		this.clock = clock;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (val != this.val && progress)
		{
			if (onZero || val != 0)
			{
				other.clearMaxValue();
				agent.progress(clock.getPpuExpectedCycle());
			}
		}
		
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
