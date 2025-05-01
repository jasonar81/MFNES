//For AI - saves max value, but also calls progress() on change
//It does treat the value as signed currently
//That probably should be a flag

public class SaveAndUpdateMaxValuePort implements MemoryPort, ClearablePort {
	private byte val;
	private volatile byte maxValue = 0;
	private transient AiAgent agent;
	private Clock clock;
	
	public SaveAndUpdateMaxValuePort(AiAgent agent, Clock clock)
	{
		val = 0;
		this.agent = agent;
		this.clock = clock;
	}
	
	public void setAgent(AiAgent agent)
	{
		this.agent = agent;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (val > maxValue)
		{
			this.val = val;
			maxValue = val;
			agent.progress(clock.getPpuExpectedCycle());
		}
		else
		{
			this.val = val;
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
