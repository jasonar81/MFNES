import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ControllerNeuralNet {
	private int layerSize;
	private int numLayers;
	private int[][] parameters;
	private int[] output = new int[6];
	Memory cpuMem;
	private int updateFirstDim;
	private int updateSecondDim;
	private int oldValue;
	private int numParametersSet = 0;
	private int memorySize = 0x10000 - 0x8000 + 0x800;
	private boolean memoryBased;
	private int displaySize = 280 * 240;
	private GUI gui;
	
	public ControllerNeuralNet(int layerSize, int numLayers, boolean init, boolean flag)
	{
		this.layerSize = layerSize;
		this.numLayers = numLayers;
		this.memoryBased = false;
		
		parameters = new int[numLayers+1][];
		parameters[0] = new int[displaySize * layerSize + layerSize];
		for (int i = 1; i < numLayers; ++i)
		{
			parameters[i] = new int[layerSize * layerSize + layerSize];
		}
		
		parameters[numLayers] = new int[layerSize * 6 + 6];
		
		if (init)
		{
			initParameters();
			numParametersSet = numParameters();
		}
	}
	
	public ControllerNeuralNet(int layerSize, int numLayers, boolean init)
	{
		this.layerSize = layerSize;
		this.numLayers = numLayers;
		this.memoryBased = true;
		
		parameters = new int[numLayers+1][];
		parameters[0] = new int[memorySize * layerSize + layerSize];
		for (int i = 1; i < numLayers; ++i)
		{
			parameters[i] = new int[layerSize * layerSize + layerSize];
		}
		
		parameters[numLayers] = new int[layerSize * 6 + 6];
		
		if (init)
		{
			initParameters();
			numParametersSet = numParameters();
		}
	}
	
	public boolean hasMoreSetup()
	{
		return numParametersSet < numParameters();
	}
	
	public void setParameter(int val, int num)
	{
		int offset = 0;
		for (int i = 0; i < parameters.length; ++i)
		{
			if (num < offset + parameters[i].length)
			{
				parameters[i][num - offset] = val;
				return;
			}
			
			offset += parameters[i].length;
		}
	}
	
	public int getParameter(int num)
	{
		int offset = 0;
		for (int i = 0; i < parameters.length; ++i)
		{
			if (num < offset + parameters[i].length)
			{
				return parameters[i][num - offset];
			}
			
			offset += parameters[i].length;
		}
		
		return 0;
	}
	
	public int numParameters()
	{
		int retval = 0;
		for (int i = 0; i < parameters.length; ++i)
		{
			retval += parameters[i].length;
		}
		
		return retval;
	}
	
	public void setCpuMem(Memory cpuMem)
	{
		this.cpuMem = cpuMem;
	}
	
	public void setGui(GUI gui)
	{
		this.gui = gui;
	}
	
	public void updateParameters()
	{
		updateFirstDim = Math.abs(ThreadLocalRandom.current().nextInt()) % parameters.length;
		updateSecondDim = Math.abs(ThreadLocalRandom.current().nextInt()) % parameters[updateFirstDim].length;
		oldValue = parameters[updateFirstDim][updateSecondDim];
		parameters[updateFirstDim][updateSecondDim] = ThreadLocalRandom.current().nextInt();
	}
	
	public void revertParameters()
	{
		parameters[updateFirstDim][updateSecondDim] = oldValue;
	}
	
	private void initParameters()
	{
		for (int i = 0; i < parameters.length; ++i)
		{
			for (int j = 0; j < parameters[i].length; ++j)
			{
				parameters[i][j] = ThreadLocalRandom.current().nextInt();
			}
		}
	}
	
	private void run()
	{
		int[] mem;
		int inputSize;
		
		if (memoryBased)
		{
			mem = cpuMem.getAllMemory();
			inputSize = memorySize;
		}
		else
		{
			mem = gui.getDisplayData();
			inputSize = displaySize;
		}
		
		int[] temp = new int[layerSize];
		//Run first layer
		for (int i = 0; i < layerSize; ++i)
		{
			for (int j = 0; j < inputSize; ++j)
			{
				int x = parameters[0][i * inputSize + j] * mem[j];
				if (x < 0)
				{
					x = 0;
				}
				
				temp[i] += x;
			}
			
			temp[i] += parameters[0][layerSize * inputSize + i];
		}
		
		//Run the rest of the hidden layers
		int[] temp2 = new int[layerSize];
		for (int i = 1; i < numLayers; ++i)
		{
			for (int j = 0; j < layerSize; ++j)
			{
				for (int k = 0; k < layerSize; ++k)
				{
					int x = parameters[i][j * layerSize + k] * temp[k];
					if (x < 0)
					{
						x = 0;
					}
					
					temp2[j] += x;
				}
				
				temp2[i] += parameters[i][layerSize * layerSize + j];
			}
			
			temp = temp2;
			temp2 = new int[layerSize];
		}
		
		//Create output values
		for (int i = 0; i < 6; ++i)
		{
			for (int j = 0; j < layerSize; ++j)
			{
				int x = parameters[numLayers][i * layerSize + j] * temp[j];		
				output[i] += x;
			
			}
			
			output[i] += parameters[numLayers][layerSize * 6 + i];
		}
	}
	
	public int getButtonState()
	{
		run();
		int state = 0;
		if (output[0] >= 0)
		{
			state |= 0x80;
		}
		
		if (output[1] >= 0)
		{
			state |= 0x40;
		}
		
		if (output[2] >= 0)
		{
			state |= 0x20;
		}
		
		if (output[3] >= 0)
		{
			state |= 0x10;
		}
		
		if (output[4] >= 0)
		{
			state |= 0x08;
		}
		
		if (output[5] >= 0)
		{
			state |= 0x04;
		}
		
		return state;
	}
}
