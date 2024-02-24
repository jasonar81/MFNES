import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class GenContraDtVideo implements AiAgent {
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
	
	private static GenContraDtVideo instance;
	
	private long firstUsableCycle = 62407559;
	private volatile long previousProgressCycle;
	private volatile long previousProgressScore;
	private volatile long remainingLives;
	private volatile long previousRemainingLives;
	private volatile long previousProgressShots;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private long numControllerRequests = 6000000;
	
	private static int A = 0x80;
	private static int B = 0x40;
	private static int UP = 0x20;
	private static int DOWN = 0x10;
	private static int LEFT = 0x08;
	private static int RIGHT = 0x04;
	private static int SELECT = 0x02;
	private static int START = 0x01;
	
	private static String dir;
	private static String ts;
	
	public static void main(String[] args)
	{
		dir = args[0];
		ts = args[1];
		
		if (!dir.endsWith("/"))
		{
			dir += "/";
		}
		
		instance = new GenContraDtVideo();
		instance.main();
	}
	
	private void main()
	{
		ArrayList<Integer> validStates = new ArrayList<Integer>();
		validStates.add(0);
		validStates.add(UP);
		validStates.add(DOWN);
		validStates.add(LEFT);
		validStates.add(RIGHT);
		validStates.add(UP | A);
		validStates.add(DOWN | A);
		validStates.add(LEFT | A);
		validStates.add(RIGHT | A);
		validStates.add(UP | B);
		validStates.add(DOWN | B);
		validStates.add(LEFT | B);
		validStates.add(RIGHT | B);
		validStates.add(UP | A | B);
		validStates.add(DOWN | A | B);
		validStates.add(LEFT | A | B);
		validStates.add(RIGHT | A | B);
		validStates.add(A);
		validStates.add(B);
		validStates.add(A | B);
		
		validStates.add(UP | LEFT);
		validStates.add(UP | LEFT | A);
		validStates.add(UP | LEFT | B);
		validStates.add(UP | LEFT | A | B);
		validStates.add(UP | RIGHT);
		validStates.add(UP | RIGHT | A);
		validStates.add(UP | RIGHT | B);
		validStates.add(UP | RIGHT | A | B);
		validStates.add(DOWN | LEFT);
		validStates.add(DOWN | LEFT | A);
		validStates.add(DOWN | LEFT | B);
		validStates.add(DOWN | LEFT | A | B);
		validStates.add(DOWN | RIGHT);
		validStates.add(DOWN | RIGHT | A);
		validStates.add(DOWN | RIGHT | B);
		validStates.add(DOWN | RIGHT | A | B);
		
		if (!loadTree())
		{
			tree = new NewMutatingDecisionTree(validStates);
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("contra.nes");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree.getRoot());
		run();
		
		while (!done) {}
		
		teardown();
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File(dir + "contra" + ts + ".tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree = (NewMutatingDecisionTree)i.readObject();
			controller = new DecisionTreeController(tree.getRoot());
	
			i.close();
			f.close();
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
		gui = new RecordingDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock, dir + "contra_dt" + ts + ".mp4");
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
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
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
			
			if (offset == 0)
			{
				shotsDelta = 0;
			}
			
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
