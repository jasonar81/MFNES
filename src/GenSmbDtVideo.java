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

public class GenSmbDtVideo implements AiAgent {
	private Clock clock;
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	private Memory ppuMem;
	private Memory cpuMem;
	private Thread cpuThread;
	
	
	private GUI gui;
	private Thread guiThread;
	private volatile long highScore = 0;
	private volatile boolean done;
	private volatile boolean startedDone;
	private volatile long score;
	
	private static GenSmbDtVideo instance;
	
	private long firstUsableCycle = 25377919; 
	private volatile long previousTimer;
	private volatile long previousProgressScore;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private long numControllerRequests = 10000000;
	
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
		
		instance = new GenSmbDtVideo();
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
		
		if (!loadTree())
		{
			tree = new NewMutatingDecisionTree(validStates);
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("smb.nes");
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
			File file = new File(dir + "smb" + ts + ".tree");
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
		previousTimer = 999;
		previousProgressScore = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {16103188, 16979809, 24542115, 25377918};
		clock = new Clock();
		gui = new RecordingDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock, dir + "smb_dt" + ts + ".mp4");
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
		
		cpu.terminate();
		
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
		
	}
	
	private void on()
	{
		
		
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		
		
		cpu.debugHold(true);
		
		
		
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
		long retval = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x71d].read());
		if (retval == 255 && getScreenInLevel() == 0)
		{
			retval = 0;
		}
		
		return retval;
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x0e] = new DoneRamPort((byte)6, this, clock); //Call it a wrap when there's a death
		cpu.getMem().getLayout()[0x7fc] = new NonZeroDoneRamPort(this, clock); //I think this increments with game completions
		cpu.getMem().getLayout()[0x71a] = new NotifyChangesPort(this, clock); //call progress when we get to a new screen
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
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
		
	}
	
	private void cont()
	{
		cpu.debugHold(false);
		
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
