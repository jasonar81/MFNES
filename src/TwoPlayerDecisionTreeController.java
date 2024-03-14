//Neural net for generating controller events from memory or pixel data

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class TwoPlayerDecisionTreeController {
	Memory cpuMem;
	private TwoPlayerMutatingDecisionTree tree;
	private int counter = 0;
	private int currentState;
	
	public void reset()
	{
		counter = 0;
		currentState = 0;
	}
	
	public TwoPlayerDecisionTreeController(TwoPlayerMutatingDecisionTree tree)
	{	
		this.tree = tree;
	}
	
	public void setTree(TwoPlayerMutatingDecisionTree tree)
	{
		this.tree = tree;
	}
	
	public void setCpuMem(Memory cpuMem)
	{
		this.cpuMem = cpuMem;
	}
	
	public int getButtonState(long cycle)
	{	
		if (counter == 0)
		{
			int state = tree.run(cpuMem.getAllRam());
			currentState = state;
			++counter;
			return state;
		}
		else
		{
			++counter;
			if (counter == 16)
			{
				counter = 0;
			}
			
			return currentState;
		}
	}
}
