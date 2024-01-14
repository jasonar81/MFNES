//Used by AI, does what it says it does

public class TrackSumOfSubtractionsPort implements MemoryPort {
	private byte val;
	private AiAgent agent;
	private Clock clock;
	private volatile int sum = 0;
	private boolean report;
	
	public TrackSumOfSubtractionsPort(AiAgent agent, Clock clock, boolean report)
	{
		val = 0;
		this.agent = agent;
		this.clock = clock;
		this.report = report;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (Byte.toUnsignedInt(val) < Byte.toUnsignedInt(this.val))
		{
			sum += Byte.toUnsignedInt(this.val) - Byte.toUnsignedInt(val);
			this.val = val;
			if (report)
			{
				agent.progress(clock.getPpuExpectedCycle());
			}
		}
		else
		{
			this.val = val;
		}
	}
	
	public int getSum()
	{
		return sum;
	}
	
	public void reset()
	{
		sum = 0;
	}
}
