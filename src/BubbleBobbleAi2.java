import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class BubbleBobbleAi2 implements AiAgent {
	private Clock clock;
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	private Memory ppuMem;
	private Memory cpuMem;
	private Thread cpuThread;
	private Thread ppuThread;
	private Thread apuThread;
	private GUI gui;
	private Thread guiThread;
	private volatile long highScore = 0;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile int currentLevel = 1;
	private volatile long previousScore = 0;
	private volatile long previousFinishTime = 0;
	
	private static BubbleBobbleAi2 instance;
	
	private long firstUsableCycle = 52853029;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private ControllerNeuralNet net;
	private long numControllerRequests = 200000;
	private int layerSize = 8;
	private int numLayers = 1;
	
	public static void main(String[] args)
	{
		instance = new BubbleBobbleAi2();
		instance.main();
	}
	
	private void main()
	{
		boolean fileExists = false;
		if (!loadNet())
		{
			 net = new ControllerNeuralNet(false, layerSize, numLayers, true);
		}
		else
		{
			fileExists = true;
			System.out.println("Successfully load a " + layerSize + "x" + numLayers + " net with up to " + numControllerRequests + " events");
		}
		
		setup();
		load("bubble_bobble.nes", "sav");
		makeModifications();
		net.reset();
		net.setCpuMem(cpuMem);
		run();
		
		while (!done) {}
		
		printResults();
		System.out.println("Score of " + score);

		processScreenResults();
		highScore = score;
		System.out.println("New high score!");
		
		teardown();
		
		if (!fileExists)
		{
			System.out.println("File did not exist, so saving newly created network");
			saveNet();
		}
		
		while (true)
		{
			net.updateParameters();
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			net.reset();
			net.setCpuMem(cpuMem);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
			
			processScreenResults();
	
			teardown();
			if (score > highScore)
			{
				highScore = score;
				System.out.println("New high score!");
				saveNet();
				if (numControllerRequests < 300000000)
				{
					numControllerRequests *= 2;
				}
			}
			else
			{
				net.revertParameters();
			}
		}
	}
	
	private void saveNet()
	{
		try
		{
			FileWriter file = new FileWriter("bubble_bobble.net");
			PrintWriter out = new PrintWriter(file);
			out.println(net.getParamNumToUpdate());
			out.println(layerSize);
			out.println(numLayers);
			out.println(numControllerRequests);
			
			int numParameters = net.numParameters();
			for (int i = 0; i < numParameters; ++i)
			{
				out.println(net.getParameter(i));
			}
			
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean loadNet()
	{
		try
		{
			File file = new File("bubble_bobble.net");
			if (!file.exists())
			{
				return false;
			}
			
			Scanner in = new Scanner(file);
			String line = in.nextLine();
			int paramNumToUpdate = Integer.parseInt(line);
			line = in.nextLine();
			layerSize = Integer.parseInt(line);
			line = in.nextLine();
			numLayers = Integer.parseInt(line);
			line = in.nextLine();
			numControllerRequests = Long.parseLong(line);
			net = new ControllerNeuralNet(false, layerSize, numLayers, false);
			net.setParamNumToUpdate(param);
			
			int paramNum = 0;
			while (in.hasNextLine() && net.hasMoreSetup())
			{
				line = in.nextLine();
				int val = Integer.parseInt(line);
				net.setParameter(val, paramNum++);
			}
			
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private void setup()
	{
		previousFinishTime = firstUsableCycle;
		previousScore = 0;
		currentLevel = 1;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {3551418, 4298614, 15538630, 16164028, 17682829, 18130489,
				20452414, 20834989, 22643841, 23248948, 26187211, 27332024, 38767736, 38767882,
				45561100, 46095082};
		clock = new Clock();
		gui = new NetGui(false, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		
		ppuMem = new Memory(Memory.PPU, null, gui);
		ppu = new PPU(clock, ppuMem, gui);
		cpuMem = new Memory(Memory.CPU, ppu, gui);
		cpu = new CPU(clock, cpuMem, ppu, gui);
		apu = new APU(cpu, gui, clock);
		cpu.setApu(apu);
		ppu.setCPU(cpu);
		cpuMem.setCpu(cpu);
		ppuMem.setCpu(cpu);
		gui.setCpu(cpu);
		gui.setClock(clock);
	}
	
	private void teardown()
	{
		apu.terminate();
		cpu.terminate();
		ppu.terminate();
		gui.terminate();
		
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e) {}
	}

	private void load(String filename, String saveFilename)
	{
		Cartridge cart = Cartridge.loadCart(filename);
		
		if (cart != null)
		{
			cpu.setupCart(cart, saveFilename);
			ppu.setupCart(cart);
		}
	}
	
	private void run()
	{
		on();
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private void printResults()
	{
		System.out.println("Level = " + screenScores.size());
		System.out.println("Game score = " + gameScore());
	}
	
	private int getLevel()
	{
		return ((SaveAndUpdateMaxValuePort)cpu.getMem().getLayout()[0x401]).getMaxValue();
	}
	
	private void on()
	{
		ppuThread = new Thread(ppu);
		ppuThread.setPriority(10);
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		apuThread = new Thread (apu);
		apuThread.setPriority(10);
		cpu.debugHold(true);
		ppu.debugHold(true);
		ppuThread.start();
		apuThread.start();
		cpuThread.start();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x2e] = new NotifyChangesPort(this, clock); //Lives remaining
		cpu.getMem().getLayout()[0x401] = new SaveAndUpdateMaxValuePort(this, clock); //Level (0 doesn't count)
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			long screenScore = partialScore();
			screenScores.add(screenScore);
			score += screenScore;
			done = true;
		}
	}
	
	private void pause()
	{
		cpu.debugHold(true);
		ppu.debugHold(true);
	}
	
	private void cont()
	{
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private boolean processScreenResults()
	{
		boolean retval = false;
		for (int i = 0; i < screenScores.size(); ++i)
		{
			if (i < bestScreenScores.size())
			{
				if (screenScores.get(i) > bestScreenScores.get(i))
				{
					retval = true;
					System.out.println("Screen " + i + " had a new best score of " + screenScores.get(i) + " old best was " + bestScreenScores.get(i));
					bestScreenScores.set(i, screenScores.get(i));
				}
			}
			else
			{
				retval = true;
				System.out.println("Screen " + i + " was never played before. Got a score of " + screenScores.get(i));
				bestScreenScores.add(screenScores.get(i));
			}
		}
		
		return retval;
	}
	
	public synchronized void progress(long cycle)
	{
		pause();
		
		//We got pinged because of a level change or loss of life
		if (cycle >= firstUsableCycle)
		{
			int level = getLevel();
			if (level > currentLevel)
			{
				//new level 
				System.out.println("Finished level");
				currentLevel = level;
				System.out.println("Level is now " + level);
				long screenScore = finishedScreenScore(cycle);
				screenScores.add(screenScore);
				score += screenScore;
			}
			else if (cpu.getMem().getLayout()[0x2e].read() == 0)
			{
				setDone(cycle);
			}
		}
		
		cont();
	}
	
	private long finishedScreenScore(long cycle)
	{
		long seconds = (long)((cycle - previousFinishTime) / 5369317.5);
		System.out.println("Level took " + seconds + "s");
		if (seconds > 255)
		{
			seconds = 255;
		}
		
		seconds = 255 - seconds;
		seconds <<= 24;
		long lives = cpu.getMem().read(0x2e);
		System.out.println("Remaining lives = " + lives);
		lives <<= 32;
		
		long gameScore = gameScore();
		long delta = gameScore - previousScore;
		if (delta < 0)
		{
			delta = 0;
		}
		
		System.out.println("Score in this level = " + delta);
		previousScore = gameScore;
		previousFinishTime = cycle;
		return lives + seconds + delta;
	}
	
	private long partialScore()
	{
		System.out.println("Processing partial screen");
		long seconds = 0;
		long lives = 0;
		
		long gameScore = gameScore();
		long delta = gameScore - previousScore;
		if (delta < 0)
		{
			delta = 0;
		}
		
		System.out.println("Score in this level = " + delta);
		return lives + seconds + delta;
	}
	
	private long gameScore()
	{
		long retval = 0;
		int val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x44a].read());
		if (val != 0x27)
		{
			retval += val;
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x449].read());
		if (val != 0x27)
		{
			retval += (val * 10);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x448].read());
		if (val != 0x27)
		{
			retval += (val * 100);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x447].read());
		if (val != 0x27)
		{
			retval += (val * 1000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x446].read());
		if (val != 0x27)
		{
			retval += (val * 10000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x445].read());
		if (val != 0x27)
		{
			retval += (val * 100000);
		}
		
		return retval;
	}

	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
}
