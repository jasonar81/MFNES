import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class ContraAi2 implements AiAgent {
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
	private volatile ArrayList<Long> deaths = new ArrayList<Long>();
	private volatile long score;
	
	private static ContraAi2 instance;
	
	private long firstUsableCycle = 62407559;
	private volatile long previousProgressCycle;
	private volatile long previousProgressScore;
	private volatile long remainingLives;
	private volatile long previousRemainingLives;
	private volatile long previousProgressShots;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private ControllerNeuralNet net;
	private long numControllerRequests = 10000;
	private int layerSize = 8;
	private int numLayers = 1;
	
	public static void main(String[] args)
	{
		instance = new ContraAi2();
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
		load("contra.nes");
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
			load("contra.nes");
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
			FileWriter file = new FileWriter("contra.net");
			PrintWriter out = new PrintWriter(file);
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
			File file = new File("contra.net");
			if (!file.exists())
			{
				return false;
			}
			
			Scanner in = new Scanner(file);
			String line = in.nextLine();
			layerSize = Integer.parseInt(line);
			line = in.nextLine();
			numLayers = Integer.parseInt(line);
			line = in.nextLine();
			numControllerRequests = Long.parseLong(line);
			net = new ControllerNeuralNet(false, layerSize, numLayers, false);
			
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
		previousProgressCycle = 0;
		previousProgressScore = 0;
		remainingLives = 65536;
		previousRemainingLives = 65536;
		previousProgressShots = 0;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		long[] startOnOffTimes = new long[] {11426048, 12714767, 26833377, 28715336};
		gui = new NetGui(false, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		deaths.clear();
		deaths = deaths;
		
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
		System.out.println("Game completions = " + cpu.getMem().read(0x31));
		System.out.println("Level = " + cpu.getMem().read(0x30));
		System.out.println("Screen in level = " + ((SaveMaxValueAndClearElsewherePort)cpu.getMem().getLayout()[0x64]).getMaxValue());
		System.out.println("Distance into screen = " + ((SaveMaxValuePort)cpu.getMem().getLayout()[0x65]).getMaxValue());
		System.out.println("Score = " + getGameScore());
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x34] = new RomMemoryPort((byte)0); //Fix the randomizer value
		//cpu.getMem().getLayout()[0x32] = new RomMemoryPort((byte)63); //Always report back 63 lives remaining
		cpu.getMem().getLayout()[0x3a] = new DoneRamPort((byte)2, this, clock); //When continues decrements to 2, call it a wrap
		cpu.getMem().getLayout()[0x65] = new SaveMaxValuePort(); //Distance into current screen
		cpu.getMem().getLayout()[0x64] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x65], false, true, this, clock); //Screen number in level
		cpu.getMem().getLayout()[0x30] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x64], false, true, this, clock); //Level
		cpu.getMem().getLayout()[0xb4] = new DeathPort((byte)1, this, clock); //Detect a death
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			startedDone = true;
			long scoreDelta = getGameScore() - previousProgressScore;
			System.out.println("There were " + deaths.size() + " deaths");
			
			long offset = getScreenOffset();
			offset *= (256 * 256);
			scoreDelta *= 256;
			long shotsDelta = getTotalShots() - previousProgressShots;
			score += (offset + scoreDelta + shotsDelta);
			screenScores.add(offset + scoreDelta + shotsDelta);
			screenScores = screenScores;
			System.out.println("Screen scores size is " + screenScores.size());
			System.out.println("Added " + (offset + scoreDelta) + " to score");
			done = true;
		}
	}
	
	public synchronized void setDeath(long cycle)
	{
		pause();
		deaths.add(cycle);
		deaths = deaths;
		
		--remainingLives;
		cont();
		if (remainingLives == 0)
		{
			setDone(clock.getPpuExpectedCycle());
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
	
	public synchronized void progress(long cycle)
	{
		long currentScore = getGameScore();
		pause();
		long lives = 65536 - (previousRemainingLives - remainingLives);
		System.out.println("Lives lost = " + (previousRemainingLives - remainingLives));
		lives *= (256L * 256L * 256L * 256L);
		long timeScore = (long)(255.0 - ((cycle - previousProgressCycle) / 5369317.5));
		System.out.println("Took " + ((cycle - previousProgressCycle) / 5369317.5) + " seconds");
		if (timeScore < 0)
		{
			timeScore = 0;
		}
		timeScore *= (256L * 256L * 256L);
		long offset = 255;
		offset *= (256 * 256);
		long scoreDelta = currentScore - previousProgressScore;
		System.out.println("Score " + scoreDelta + " points");
		scoreDelta *= 256;
		long shotsDelta = getTotalShots() - previousProgressShots;
		System.out.println("Number of shots = " + shotsDelta);
		if (shotsDelta > 255)
		{
			shotsDelta = 255;
		}
		
		previousProgressCycle = cycle;
		previousProgressScore = currentScore;
		previousRemainingLives = remainingLives;
		previousProgressShots = getTotalShots();
		score += (lives + timeScore + offset + scoreDelta + shotsDelta);
		screenScores.add(lives + timeScore + offset + scoreDelta + shotsDelta);
		screenScores = screenScores;
		System.out.println("Screen scores size is " + screenScores.size());
		System.out.println("Added " + (lives + timeScore + offset + scoreDelta) + " to score");
		
		cont();
	}
	
	private int getGameScore()
	{
		return (cpu.getMem().read(0x07e3) << 8) + cpu.getMem().read(0x07e2);
	}
	
	private long getScreenOffset()
	{
		return ((SaveMaxValuePort)cpu.getMem().getLayout()[0x65]).getMaxValue();
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
	
	private long getTotalShots()
	{
		return gui.getTotalBPresses();
	}
}
