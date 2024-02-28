//Neural net for generating controller events from memory or pixel data

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DecisionTreeController {
	Memory cpuMem;
	private IfElseNode tree;
	private int counter = 0;
	private int currentState;
	
	public void reset()
	{
		counter = 0;
		currentState = 0;
	}
	
	public DecisionTreeController(IfElseNode tree)
	{	
		this.tree = tree;
	}
	
	public void setTree(IfElseNode tree)
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
			if (counter == 8)
			{
				counter = 0;
			}
			
			return currentState;
		}
	}
}
