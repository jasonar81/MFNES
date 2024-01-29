import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class SmbAi2 implements AiAgent {
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
	private volatile boolean done;
	private volatile boolean startedDone;
	private volatile long score;
	
	private static SmbAi2 instance;
	
	private long firstUsableCycle = 25377919; 
	private volatile long previousTimer;
	private volatile long previousProgressScore;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	
	private ControllerNeuralNet net;
	private long numControllerRequests = 10000;
	private int layerSize = 8;
	private int numLayers = 1;
	
	public static void main(String[] args)
	{
		instance = new SmbAi2();
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
		load("smb.nes");
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
			load("smb.nes");
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
			FileWriter file = new FileWriter("smb.net");
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
			File file = new File("smb.net");
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
			net.setParamNumToUpdate(paramNumToUpdate);
			
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
		screenScores = new ArrayList<Long>();
		score = 0;
		previousTimer = 999;
		previousProgressScore = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {16103188, 16979809, 24542115, 25377918};
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

	private void load(String filename)
	{
		Cartridge cart = Cartridge.loadCart(filename);
		
		if (cart != null)
		{
			cpu.setupCart(cart);
			ppu.setupCart(cart);
		}
	}
	
	private void run()
	{
		on();
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private void on()
	{
		ppuThread = new Thread(ppu);
		ppuThread.setPriority(10);
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		apuThread = new Thread(apu);
		apuThread.setPriority(10);
		cpu.debugHold(true);
		ppu.debugHold(true);
		ppuThread.start();
		apuThread.start();
		cpuThread.start();
	}
	
	private void printResults()
	{
		System.out.println("Level = " + getLevel());
		System.out.println("Screen in level = " + getScreenInLevel());
		System.out.println("Distance into screen = " + getDistanceIntoScreen());
		System.out.println("Score = " + getGameScore());
	}
	
	private long getLevel()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x760].read());
	}
	
	private long getScreenInLevel()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x71a].read());
	}
	
	private long getDistanceIntoScreen()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x71d].read());
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x0e] = new DoneRamPort((byte)6, this, clock); //Call it a wrap when there's a death
		cpu.getMem().getLayout()[0x7fc] = new NonZeroDoneRamPort(this, clock); //I think this increments with game completions
		cpu.getMem().getLayout()[0x71a] = new NotifyChangesPort(this, clock); //call progress when we get to a new screen
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			startedDone = true;
			
			long partialScore = partialScore(totalTime);
			
			score += partialScore;
			screenScores.add(partialScore);
			screenScores = screenScores;
			System.out.println("Screen scores size is " + screenScores.size());
			System.out.println("Added " + partialScore + " to score");
			done = true;
		}
	}
	
	private long partialScore(long cycle)
	{
		long gameScore = getGameScore();
		long scoreDelta = gameScore - previousProgressScore;
		long posInScreen = getDistanceIntoScreen();
		return scoreDelta + (posInScreen << 24);
	}
	
	public synchronized void setDeath(long cycle)
	{
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
	
	public synchronized void progress(long cycle)
	{
		pause();
		long screenScore = finishedScreenScore(cycle);
		score += screenScore;
		screenScores.add(screenScore);
		screenScores = screenScores;
		System.out.println("Screen scores size is " + screenScores.size());
		System.out.println("Added " + screenScore + " to score");
		cont();
	}
	
	private long getTimer()
	{
		MemoryPort[] layout = cpu.getMem().getLayout();
		return Byte.toUnsignedLong(layout[0x7fa].read()) + Byte.toUnsignedLong(layout[0x7f9].read()) * 10 + Byte.toUnsignedLong(layout[0x7f8].read()) * 100;
	}
	
	private long finishedScreenScore(long cycle)
	{
		long timer = getTimer();
		long timerDelta = previousTimer - timer;
		if (timerDelta < 0)
		{
			timerDelta = 999 - timer;
		}
		
		long offset = 255;
		long currentScore = getGameScore();
		long scoreDelta = previousProgressScore - currentScore;
		if (scoreDelta < 0)
		{
			scoreDelta = 0;
		}
		
		previousTimer = timer;
		previousProgressScore = currentScore;
		
		return ((999 - timerDelta) << 32) + (offset << 24) + scoreDelta;
	}
	
	private int getGameScore()
	{
		MemoryPort[] layout = cpu.getMem().getLayout();
		return Byte.toUnsignedInt(layout[0x7e2].read()) + Byte.toUnsignedInt(layout[0x7e1].read()) * 10 + Byte.toUnsignedInt(layout[0x7e0].read()) * 100 + Byte.toUnsignedInt(layout[0x7df].read()) * 1000 + Byte.toUnsignedInt(layout[0x7de].read()) * 10000 + Byte.toUnsignedInt(layout[0x7dd].read()) * 100000;
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
}
