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

public class BubbleBobbleMultiTree implements AiAgent {
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
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile int currentLevel = 1;
	private volatile long previousScore = 0;
	private volatile long previousFinishTime = 0;
	
	private static BubbleBobbleMultiTree instance;
	
	private long firstUsableCycle = 52853029;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private MultiDecisionTree tree;
	private MultiTreeController controller;
	private long numControllerRequests = 10000;
	
	private long usedControllerRequests;
	
	private static int A = 0x80;
	private static int B = 0x40;
	private static int UP = 0x20;
	private static int DOWN = 0x10;
	private static int LEFT = 0x08;
	private static int RIGHT = 0x04;
	private static int SELECT = 0x02;
	private static int START = 0x01;
	
	private int countWithNoImprovement = 0;
	
	public static void main(String[] args)
	{
		instance = new BubbleBobbleMultiTree();
		instance.main();
	}
	
	private void main()
	{
		ArrayList<Integer> validStates = new ArrayList<Integer>();
		validStates.add(0);
		validStates.add(LEFT);
		validStates.add(RIGHT);
		validStates.add(LEFT | A);
		validStates.add(RIGHT | A);
		validStates.add(LEFT | B);
		validStates.add(RIGHT | B);
		validStates.add(LEFT | A | B);
		validStates.add(RIGHT | A | B);
		validStates.add(A);
		validStates.add(B);
		validStates.add(A | B);
		
		if (!loadTree())
		{
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			addresses.add(0x401);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0x2e);
			
			IfElseNode root = new IfElseNode();
			root.terminal = true;
			root.terminalValue = RIGHT;
			
			tree = new MultiDecisionTree(validStates, addresses, root, disallow);
		}
		
		controller = new MultiTreeController(tree);
		tree.setValidStates(validStates);
		setup();
		load("bubble_bobble.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunAllMode();
		tree.reset();
		run();
		
		while (!done) {}
		
		printResults();
		System.out.println("Score of " + score);

		processScreenResults();
		highScore = score;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests * 3;
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
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
				saveTree();
			}
			else
			{
				break;
			}
		}
		
		int sceneNum = tree.getLastSceneNum();
		countWithNoImprovement = tree.getLastNoImprovementCount();
		while (true)
		{
			//play screen until no deaths 
			System.out.println("Working on scene " + sceneNum);
			while (sceneNum >= tree.numScenes())
			{
				sceneNum--;
			}
			
			if (!workOnScene(sceneNum))
			{
				if (sceneNum > 0)
				{
					sceneNum--;
				}
				
				continue;
			}
			
			//play all
			numControllerRequests *= 2;
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
			numControllerRequests = usedControllerRequests * 3;
	
			teardown();
			saveTree();
			++sceneNum;
		}
	}
	
	private boolean workOnScene(int sceneNum)
	{
		countWithNoImprovement = 0;
		while (true)
		{
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunSceneMode(sceneNum, countWithNoImprovement);
			tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
			
			HashSet<Integer> addressesAndValues = tree.getAddressesAndValues();
	
			teardown();
			
			if (score > highScore && confirm(sceneNum))
			{
				countWithNoImprovement = 0;
				highScore = score;
				System.out.println("New high score with scene num = " + sceneNum);
				tree.persist();
				saveTree();
			} else if (score == highScore)
			{
				countWithNoImprovement++;
				tree.persist();
				saveTree();
			}
			else 
			{
				countWithNoImprovement++;
				tree.revert();
			}
			
			if (countWithNoImprovement > 300)
			{
				return completedScene();
			}
			else
			{
				System.out.println("No improvement count: " + countWithNoImprovement);
			}
			
			System.out.println("Addresses and values size = " + addressesAndValues.size());
			System.out.println("Completed scene = " + completedScene());
			tree.mutate(addressesAndValues);
		}
	}
	
	private boolean completedScene()
	{
		return tree.foundNextKey();
	}
	
	private boolean confirm(int sceneNum)
	{
		long minHighScore = score;
		setup();
		load("bubble_bobble.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunSceneMode(sceneNum);
		tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + score);
		
		if (score < minHighScore)
		{
			minHighScore = score;
		}

		teardown();
		
		score = minHighScore;
		return (score > highScore);
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("bubble_bobble_scenes.tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree = (MultiDecisionTree)i.readObject();
			tree.makeWhole();
	
			i.close();
			f.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean saveTree()
	{
		try
		{
			File file = new File("bubble_bobble_scenes.tree");
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
	
			o.writeObject(tree);
			o.close();
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
		gui = new MultiTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
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
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		cpu.debugHold(true);
		cpuThread.start();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x2e] = new NotifyChangesPort(this, clock); //Lives remaining
		cpu.getMem().getLayout()[0x401] = new SaveAndUpdateMaxValuePort(this, clock); //Level (0 doesn't count)
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
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
			usedControllerRequests = ((MultiTreeGui)gui).getRequests();
		}
	}
	
	private void pause()
	{
		cpu.debugHold(true);
	}
	
	private void cont()
	{
		cpu.debugHold(false);
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
			else if (cpu.getMem().getLayout()[0x2e].read() == 2)
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
		long lives = cpu.getMem().read(0x2e);
		System.out.println("Remaining lives = " + lives);
		lives <<= 32;
		
		long gameScore = gameScore();
		long delta = gameScore - previousScore;
		if (delta < 0)
		{
			delta = 0;
		}
		
		delta <<= 8;
		
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
		
		delta <<= 8;
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
