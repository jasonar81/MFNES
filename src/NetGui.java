//GUI implementation for neural net AIs

import java.awt.event.KeyEvent;

public class NetGui extends DefaultGUI {
	private long numControllerRequests;
	private long firstUsableCycle;
	private int state;
	private int requestsWithinState = 0;
	private long requests = 0;
	private ControllerNeuralNet cnn;
	
	public NetGui(long numControllerRequests, long firstUsableCycle, ControllerNeuralNet cnn)
	{
		this.numControllerRequests = numControllerRequests;
		this.firstUsableCycle = firstUsableCycle;
		this.cnn = cnn;
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
			state = cnn.getButtonState();
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
			state = cnn.getButtonState();
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
		if ((cycle >= 11426048 && cycle < 12714767) ||
				(cycle >= 26833377 && cycle < 28715336))
		{
			return true;
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
			state = cnn.getButtonState();
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
			state = cnn.getButtonState();
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
			state = cnn.getButtonState();
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
			state = cnn.getButtonState();
			requestsWithinState = 0;
		}
		
		++requests;
		requestsWithinState = Utils.setBit(requestsWithinState, 2);
		return Utils.getBit(state, 2);
	}

	@Override
	public void writeAudioData(double data) {}
}
