//Used by AI to report a player death
import java.io.Serializable;

public class DeathPort implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private byte val;
	private byte lookoutVal;
	private transient AiAgent agent;
	private Clock clock;
	
	public DeathPort(byte lookoutVal, AiAgent agent, Clock clock)
	{
		val = 0;
		this.lookoutVal = lookoutVal;
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
		if (this.val != val && val == lookoutVal)
		{
			agent.setDeath(clock.getPpuExpectedCycle());
		}
		
		this.val = val;
	}
	
}
