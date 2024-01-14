
public class NotifyChangesPort implements MemoryPort {
	private byte val;
	private AiAgent agent;
	private Clock clock;
	
	public NotifyChangesPort(AiAgent agent, Clock clock)
	{
		val = 0;
		this.agent = agent;
		this.clock = clock;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (val != this.val)
		{
			this.val = val;
			agent.progress(clock.getPpuExpectedCycle());
		}
		else
		{
			this.val = val;
		}
	}
}
