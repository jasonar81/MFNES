//Used by AI to indicate completion of a run

public class DoneRamPort implements MemoryPort {
	private byte val;
	private byte lookoutVal;
	private AiAgent agent;
	private Clock clock;
	
	public DoneRamPort(byte lookoutVal, AiAgent agent, Clock clock)
	{
		val = 0;
		this.lookoutVal = lookoutVal;
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
		
		if (val == lookoutVal)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
	}
	
}
