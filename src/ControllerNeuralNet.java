//Neural net for generating controller events from memory or pixel data

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ControllerNeuralNet {
	private int layerSize;
	private int numLayers;
	private int[][] parameters;
	private int[] output = new int[8];
	Memory cpuMem;
	private int oldValue;
	private int numParametersSet = 0;
	private int memorySize = 0x800;
	private boolean memoryBased;
	private int displaySize = 280 * 240;
	private GUI gui;
	private boolean allButtons = false;
	private int paramNumToUpdate = 0;
	int[] temp;
	private int currentState = 0;
	private int currentFrame = 0;;
	
	public void reset()
	{
		
	}
	
	public ControllerNeuralNet(boolean allButtons, int layerSize, int numLayers, boolean init, boolean flag)
	{
		this.layerSize = layerSize;
		this.numLayers = numLayers;
		this.memoryBased = false;
		this.allButtons = allButtons;
		
		int numButtons = 6;
		if (allButtons)
		{
			numButtons = 8;
		}
		
		parameters = new int[numLayers+1][];
		parameters[0] = new int[displaySize * layerSize + layerSize];
		for (int i = 1; i < numLayers; ++i)
		{
			parameters[i] = new int[layerSize * layerSize + layerSize];
		}
		
		parameters[numLayers] = new int[layerSize * numButtons + numButtons];
		
		if (init)
		{
			initParameters();
			numParametersSet = numParameters();
		}
	}
	
	public ControllerNeuralNet(boolean allButtons, int layerSize, int numLayers, boolean init)
	{
		this.allButtons = allButtons;
		this.layerSize = layerSize;
		this.numLayers = numLayers;
		this.memoryBased = true;
		
		int numButtons = 6;
		if (allButtons)
		{
			numButtons = 8;
		}
		
		parameters = new int[numLayers+1][];
		parameters[0] = new int[memorySize * layerSize + layerSize];
		for (int i = 1; i < numLayers; ++i)
		{
			parameters[i] = new int[layerSize * layerSize + layerSize];
		}
		
		parameters[numLayers] = new int[layerSize * numButtons + numButtons];
		
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
	
	public int getParamNumToUpdate()
	{
		return paramNumToUpdate;
	}
	
	public void setParamNumToUpdate(int x)
	{
		paramNumToUpdate = x;
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
		if (paramNumToUpdate == numParameters())
		{
			paramNumToUpdate = 0;
		}
		
		oldValue = getParameter(paramNumToUpdate);
		int newValue = oldValue;
		int delta = ThreadLocalRandom.current().nextInt();
		boolean negative = (delta < 0);
		delta = Math.abs(delta);
		delta %= 256;
		if (negative)
		{
			delta *= -1;
		}
		
		newValue += delta;
		
		setParameter(newValue, paramNumToUpdate);
		System.out.println("Setting parameter " + paramNumToUpdate + " to " + newValue);
	}
	
	private int nextParamNum(int x)
	{
		if (x == numParameters() - 1)
		{
			return 0;
		}
		
		return x + 1;
	}
	
	public void revertParameters()
	{
		setParameter(oldValue, paramNumToUpdate);
		paramNumToUpdate = nextParamNum(paramNumToUpdate);
	}
	
	private void initParameters()
	{	
		for (int i = 0; i < parameters.length; ++i)
		{
			for (int j = 0; j < parameters[i].length; ++j)
			{
				if (i == 0)
				{
					parameters[i][j] = 0;
				}
				else if (i == parameters.length - 1)
				{
					if ((j % layerSize) ==  (j / layerSize))
					{
						parameters[i][j] = 1;
					}
					else
					{
						parameters[i][j] = 0;
					}
				}
				else
				{
					parameters[i][j] = 1;
				}
			}
		}
	}
	
	public void randomInit()
	{	
		for (int i = 0; i < parameters.length; ++i)
		{
			for (int j = 0; j < parameters[i].length; ++j)
			{
				parameters[i][j] = ThreadLocalRandom.current().nextInt();
			}
		}
		
		for (int i = 0; i < parameters.length; ++i)
		{
			for (int j = 0; j < parameters[i].length; ++j)
			{
				parameters[i][j] = ThreadLocalRandom.current().nextInt();
				boolean positive = (parameters[i][j] >= 0);
				parameters[i][j] = Math.abs(parameters[i][j]);
				parameters[i][j] %= (256*256);
				if (!positive)
				{
					parameters[i][j] *= -1;
				}
			}
		}
	}
	
	private void run()
	{
		int[] mem;
		int inputSize;
		
		int numButtons = 6;
		if (allButtons)
		{
			numButtons = 8;
		}
		
		if (memoryBased)
		{
			mem = cpuMem.getAllRam();
			inputSize = memorySize;
		}
		else
		{
			mem = gui.getDisplayData();
			inputSize = displaySize;
		}
		
		run(mem, inputSize, numButtons);
	}
	
	private void run(int[] mem, int inputSize, int numButtons)
	{	
		temp = new int[layerSize];
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
		for (int i = 0; i < numButtons; ++i)
		{
			output[i] = 0;
			for (int j = 0; j < layerSize; ++j)
			{
				int x = parameters[numLayers][i * layerSize + j] * temp[j];		
				output[i] += x;
			
			}
			
			output[i] += parameters[numLayers][layerSize * numButtons + i];
		}
	}
	
	public int getButtonState(long cycle)
	{
		int frame = (int)(cycle / (5 * 341 * 261));
		if (frame == currentFrame)
		{
			return currentState;
		}
		
		run();
		int state = 0;
		if (output[0] > 0)
		{
			state |= 0x80;
		}
		
		if (output[1] > 0)
		{
			state |= 0x40;
		}
		
		if (output[2] > 0)
		{
			state |= 0x20;
		}
		
		if (output[3] > 0)
		{
			state |= 0x10;
		}
		
		if (output[4] > 0)
		{
			state |= 0x08;
		}
		
		if (output[5] > 0)
		{
			state |= 0x04;
		}
		
		if (allButtons)
		{
			if (output[6] > 0)
			{
				state |= 0x02;
			}
			
			if (output[7] > 0)
			{
				state |= 0x01;
			}
		}
		
		currentFrame = frame;
		currentState = state;
		return state;
	}
}
