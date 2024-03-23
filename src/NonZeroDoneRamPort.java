//Used by AI to indicate completion of a run, reports on any value written other than zero

public class NonZeroDoneRamPort implements MemoryPort {
	private byte val;
	private transient AiAgent agent;
	private transient Clock clock;
	
	public NonZeroDoneRamPort(AiAgent agent, Clock clock)
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
		this.val = val;
		
		if (val != 0)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
	}
	
}
