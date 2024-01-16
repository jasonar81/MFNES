//GUI implementation for neural net AIs

import java.awt.event.KeyEvent;

public class NetGui extends DefaultGUI {
	private long numControllerRequests;
	private long firstUsableCycle;
	private int state;
	private int requestsWithinState = 0;
	private long requests = 0;
	private ControllerNeuralNet cnn;
	private long[] startOnOffTimes;
	Clock clock;
	
	public NetGui(long numControllerRequests, long firstUsableCycle, ControllerNeuralNet cnn, long[] startOnOffTimes, Clock clock)
	{
		this.numControllerRequests = numControllerRequests;
		this.firstUsableCycle = firstUsableCycle;
		this.cnn = cnn;
		this.startOnOffTimes = startOnOffTimes;
		this.clock = clock;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {}
	
	@Override
	public void keyReleased(KeyEvent e) {}
	
	@Override
	public boolean getA() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 7))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 7);
		return Utils.getBit(state, 7);
	}

	@Override
	public boolean getB() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 6))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 6);
		return Utils.getBit(state, 6);
	}

	@Override
	public boolean getSelect() {
		return false;
	}

	@Override
	public boolean getStart() {
		long cycle = clock.getPpuExpectedCycle();
		
		for (int i = 0; i < startOnOffTimes.length; i += 2)
		{
			if (cycle >= startOnOffTimes[i] && cycle < startOnOffTimes[i+1])
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean getUp() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 5))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 5);
		return Utils.getBit(state, 5);
	}

	@Override
	public boolean getDown() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 4))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 4);
		return Utils.getBit(state, 4);
	}

	@Override
	public boolean getLeft() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 3))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 3);
		return Utils.getBit(state, 3);
	}

	@Override
	public boolean getRight() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		if (Utils.getBit(requestsWithinState, 2))
		{
			state = cnn.getButtonState(clock.getPpuExpectedCycle());
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 2);
		return Utils.getBit(state, 2);
	}

	@Override
	public void writeAudioData(double data) {}
}
