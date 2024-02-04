//GUI implementation for neural net AIs

import java.awt.event.KeyEvent;

public class NetGui extends DefaultGUI {
	private long numControllerRequests;
	private long firstUsableCycle;
	private int state;
	private long requests = 0;
	private ControllerNeuralNet cnn;
	private long[] startOnOffTimes;
	private Clock clock;
	private boolean allButtons;
	private long[] selectTimes = new long[0];
	private boolean restrictedStart = false;
	
	public NetGui(boolean allButtons, long numControllerRequests, long firstUsableCycle, ControllerNeuralNet cnn, long[] startOnOffTimes, Clock clock)
	{
		this.allButtons = allButtons;
		this.numControllerRequests = numControllerRequests;
		this.firstUsableCycle = firstUsableCycle;
		this.cnn = cnn;
		this.startOnOffTimes = startOnOffTimes;
		this.clock = clock;
	}
	
	public void setSelectTimes(long[] selectTimes)
	{
		this.selectTimes = selectTimes;
	}
	
	public void setRestrictedStart()
	{
		restrictedStart = true;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		boolean retval = Utils.getBit(state, 6);
		if (retval)
		{
			++totalBPresses;
		}
		
		return retval;
	}

	@Override
	public boolean getSelect() {
		long cycle = clock.getPpuExpectedCycle();
		
		for (int i = 0; i < selectTimes.length; i += 2)
		{
			if (cycle >= selectTimes[i] && cycle < selectTimes[i+1])
			{
				return true;
			}
		}
		
		if (!allButtons)
		{
			return false;
		}
		
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 1);
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
		
		if (!allButtons)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		
		if (!restrictedStart)
		{
			return Utils.getBit(state, 0);
		}
		
		return state == 1;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
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
		
		state = cnn.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 2);
	}

	@Override
	public void writeAudioData(double data) {}
}
