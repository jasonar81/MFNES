
public class DeathPort implements MemoryPort {
	private byte val;
	private byte lookoutVal;
	private AiAgent agent;
	private Clock clock;
	
	public DeathPort(byte lookoutVal, AiAgent agent, Clock clock)
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
		if (this.val != val && val == lookoutVal)
		{
			agent.setDeath(clock.getPpuExpectedCycle());
		}
		
		this.val = val;
	}
	
}
