//Port used by AI

public class NotifyChangesPort implements MemoryPort {
	private byte val;
	private transient AiAgent agent;
	private transient Clock clock;
	private boolean hold = false;
	
	public NotifyChangesPort(AiAgent agent, Clock clock)
	{
		val = 0;
		this.agent = agent;
		this.clock = clock;
	}
	
	public NotifyChangesPort(AiAgent agent, Clock clock, byte val)
	{
		this.val = val;
		this.agent = agent;
		this.clock = clock;
		hold = true;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (val != this.val)
		{
			if (!hold)
			{
				this.val = val;
			}
			
			agent.progress(clock.getPpuExpectedCycle());
		}
	}
}
