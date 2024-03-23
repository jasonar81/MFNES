//Neural net for generating controller events from memory or pixel data

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MultiTreeController implements Serializable {
	private static final long serialVersionUID = -6732487624928621347L;
	Memory cpuMem;
	private transient MultiDecisionTree tree;
	private volatile int counter = 0;
	private volatile int currentState;
	
	public void reset()
	{
		counter = 0;
		currentState = 0;
	}
	
	public MultiTreeController(MultiDecisionTree tree)
	{	
		this.tree = tree;
	}
	
	public void setTree(MultiDecisionTree tree)
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
