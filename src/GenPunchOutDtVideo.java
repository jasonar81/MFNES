import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GenPunchOutDtVideo implements AiAgent {
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
	private volatile int currentLevel = 0;
	private volatile boolean knockOut = false;
	private volatile boolean justFinishedLevel = false;
	private volatile long previousCycle;
	
	private static GenPunchOutDtVideo instance;
	
	private long firstUsableCycle = 62856095;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private static long numControllerRequests;
	
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
		numControllerRequests = Integer.parseInt(args[2]);
		
		if (!dir.endsWith("/"))
		{
			dir += "/";
		}
		
		instance = new GenPunchOutDtVideo();
		instance.main();
	}
	
	private void main()
	{
		ArrayList<Integer> validStates = new ArrayList<Integer>();
		validStates.add(0);
		validStates.add(LEFT);
		validStates.add(RIGHT);
		validStates.add(DOWN);
		validStates.add(START);
		validStates.add(SELECT);
		validStates.add(A);
		validStates.add(B);
		validStates.add(UP | A);
		validStates.add(UP | B);
		
		if (!loadTree())
		{	
			tree = new NewMutatingDecisionTree(validStates);
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("punch_out.nes", "sav");
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
			File file = new File(dir + "punch_out" + ts + ".tree");
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
		previousCycle = firstUsableCycle;
		knockOut = false;
		currentLevel = 0;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {61779177, 62856094};
		clock = new Clock();
		gui = new RecordingDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock, dir + "punch_out_dt" + ts + ".mp4");
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
		System.out.println("Level = " + cpu.getMem().read(0x01));
		System.out.println("Total damage delivered " + enemyDamage());
		System.out.println("Total damage sustained " + myDamage());
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

	private int enemyDamage()
	{
		return ((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).getSum();
	}
	
	private int myDamage()
	{
		return ((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).getSum();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x01] = new NotifyChangesPort(this, clock);
		cpu.getMem().getLayout()[0x391] = new TrackSumOfSubtractionsPort(this, clock, true);
		cpu.getMem().getLayout()[0x398] = new TrackSumOfSubtractionsPort(this, clock, false);
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			//Events list ran out
			startedDone = true;
			long screenScore = partialScore(knockOut);
			screenScores.add(screenScore);
			score += screenScore;
			cont();
			System.out.println("Screen scores size is " + screenScores.size());
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
	
	public synchronized void progress(long cycle)
	{
		pause();
		
		//We got pinged because of a level change or damage to us or our opponent
		//We could be knocked out
		
		if (cpu.getMem().getLayout()[0x01].read() > currentLevel)
		{
			//new level 
			System.out.println("Finished level");
			++currentLevel;
			long screenScore = finishedScreenScore(cycle);
			screenScores.add(screenScore);
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			score += screenScore;
			justFinishedLevel = true;
		}
		else if (cpu.getMem().getLayout()[0x391].read() == 0)
		{
			if (!justFinishedLevel)
			{
				//Knocked out
				knockOut = true;
				System.out.println("Knocked out");
				setDone(cycle);
			}
			else
			{
				justFinishedLevel = false;
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			}
		}
		
		cont();
	}
	
	private long finishedScreenScore(long cycle)
	{
		//enemy damage / 255 - my damage / 255 - (seconds / 10)
		long enemyDamage = 256 * 9; //impossible to deliver more damage than this
		long myDamage = myDamage();
		long seconds = (long)((cycle - previousCycle) / 5369317.5);
		if (seconds > 2550)
		{
			seconds = 2550;
		}
		
		previousCycle = cycle;
		return (enemyDamage << 24) + ((256 * 9 - myDamage) << 8) + (255 - seconds / 10); 
	}
	
	private long partialScore(boolean ko)
	{
		long enemyDamage = enemyDamage();
		long myDamage = myDamage();
		
		if (ko)
		{
			myDamage = 256 * 9 - 1;
		}
		
		//Stuck in between rounds screen
		if (cpuMem.getLayout()[4].read() == 1)
		{
			myDamage = 256 * 9;
		}
		
		long seconds = 255 * 10;
		return (enemyDamage << 24) + ((256 * 9 - myDamage) << 8) + (255 - seconds / 10); 
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

	@Override
	public void setDeath(long cycle) {
		
	}
}
