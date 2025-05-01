//Neural net for generating controller events from memory or pixel data

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MultiTreeControllerWithScenes implements Serializable {
	private static final long serialVersionUID = -6732487624928621347L;
	Memory cpuMem;
	private transient MultiDecisionTreeWithScenes tree;
	private volatile int counter = 0;
	private volatile int currentState;
	
	public void reset()
	{
		counter = 0;
		currentState = 0;
	}
	
	public MultiTreeControllerWithScenes(MultiDecisionTreeWithScenes tree)
	{	
		this.tree = tree;
	}
	
	public void setTree(MultiDecisionTreeWithScenes tree)
	{
		this.tree = tree;
	}
	
	public void setCpuMem(Memory cpuMem)
	{
		this.cpuMem = cpuMem;
	}
	
	public void setCount(int count)
	{
		counter = count;
	}
	
	public void setState(int state)
	{
		currentState = state;
	}
	
	public int getCount()
	{
		return counter;
	}
	
	public int getState()
	{
		return currentState;
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
