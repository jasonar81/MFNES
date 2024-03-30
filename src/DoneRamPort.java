//Used by AI to indicate completion of a run
import java.io.Serializable;

public class DoneRamPort implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private byte val;
	private byte lookoutVal;
	private transient AiAgent agent;
	private Clock clock;
	
	public DoneRamPort(byte lookoutVal, AiAgent agent, Clock clock)
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
		this.val = val;
		
		if (val == lookoutVal)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
	}
	
}
