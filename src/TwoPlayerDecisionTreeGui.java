//GUI implementation for neural net AIs

import java.awt.event.KeyEvent;

public class TwoPlayerDecisionTreeGui extends DefaultGUI {
	private long numControllerRequests;
	private long firstUsableCycle;
	private int state;
	private long requests = 0;
	private TwoPlayerDecisionTreeController controller;
	private long[] startOnOffTimes;
	private Clock clock;
	private long[] selectTimes = new long[0];
	private long[] rightTimes = new long[0];
	
	public TwoPlayerDecisionTreeGui(long numControllerRequests, long firstUsableCycle, TwoPlayerDecisionTreeController controller, long[] startOnOffTimes, Clock clock)
	{
		this.numControllerRequests = numControllerRequests;
		this.firstUsableCycle = firstUsableCycle;
		this.controller = controller;
		this.startOnOffTimes = startOnOffTimes;
		this.clock = clock;
	}
	
	public long getRequests()
	{
		return requests;
	}
	
	public void setSelectTimes(long[] selectTimes)
	{
		this.selectTimes = selectTimes;
	}
	
	public void setRightTimes(long[] rightTimes)
	{
		this.rightTimes = rightTimes;
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
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 15);
	}
	
	@Override
	public boolean getA2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
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
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		boolean retval = Utils.getBit(state, 14);
		
		return retval;
	}

	@Override
	public boolean getB2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		boolean retval = Utils.getBit(state, 6);
		
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
		
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 9);
	}
	
	@Override
	public boolean getSelect2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
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
		
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 8);
	}
	
	@Override
	public boolean getStart2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 0);
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
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 13);
	}
	
	@Override
	public boolean getUp2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
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
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 12);
	}
	
	@Override
	public boolean getDown2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
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
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 11);
	}
	
	@Override
	public boolean getLeft2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 3);
	}

	@Override
	public boolean getRight() {
		long cycle = clock.getPpuExpectedCycle();
		for (int i = 0; i < rightTimes.length; i += 2)
		{
			if (cycle >= rightTimes[i] && cycle < rightTimes[i+1])
			{
				return true;
			}
		}
		
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 10);
	}
	
	@Override
	public boolean getRight2() {
		if (clock.getPpuExpectedCycle() < firstUsableCycle)
		{
			return false;
		}
		
		if (requests >= numControllerRequests)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
		
		state = controller.getButtonState(clock.getPpuExpectedCycle());
		++requests;
		return Utils.getBit(state, 2);
	}

	@Override
	public void writeAudioData(double data) {}
}
