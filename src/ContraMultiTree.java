import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class ContraMultiTree implements AiAgent{
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
	private volatile ArrayList<Long> deaths = new ArrayList<Long>();
	private volatile long score;
	private volatile long possibleScoreIncrement;
	
	private static ContraMultiTree instance;
	
	private long firstUsableCycle = 62407559;
	private volatile long previousProgressCycle;
	private volatile long previousProgressScore;
	private volatile long remainingLives;
	private volatile long previousRemainingLives;
	private volatile long previousProgressShots;
	private MultiDecisionTree tree;
	private MultiTreeController controller;
	private long numControllerRequests = 7500;
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
		instance = new ContraMultiTree();	
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
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			addresses.add(0x30);
			addresses.add(0x64);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0x32);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = RIGHT;
			tree = new MultiDecisionTree(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new MultiTreeController(tree);
		tree.setValidStates(validStates);
		setup();
		load("contra.nes");
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

		highScore = score;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests * 3;
			setup();
			load("contra.nes");
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
	
			teardown();
			if (score > highScore)
			{
				highScore = score;
				System.out.println("New high score!");
				saveTree();
			}
			else
			{
				highScore = score;
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
			load("contra.nes");
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
			load("contra.nes");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunSceneMode(sceneNum, countWithNoImprovement);
			tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
			run();
			
			while (!done) {}
			
			printResults();
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
		load("contra.nes");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunSceneMode(sceneNum);
		tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
		run();
		
		while (!done) {}
		
		printResults();
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
			File file = new File("contra_scenes.tree");
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
			File file = new File("contra_scenes.tree");
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
		score = 0;
		possibleScoreIncrement = 0;
		previousProgressCycle = 0;
		previousProgressScore = 0;
		remainingLives = 3;
		previousRemainingLives = 3;
		previousProgressShots = 0;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		long[] startOnOffTimes = new long[] {11426048, 12714767, 26833377, 28715336};
		gui = new MultiTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
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
		cpu.terminate();
		gui.terminate();
		
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e) {}
		cpu = null;
		ppu = null;
		cpuMem = null;
		ppuMem = null;
		apu = null;
		clock = null;
		gui = null;
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
			done = true;
			if (possibleScoreIncrement != 0)
			{
				score += possibleScoreIncrement;
			}
			else
			{
				long scoreDelta = getGameScore();
				long offset = getScreenOffset();
				offset *= (256 * 256 * 256);
				scoreDelta += (256 - Byte.toUnsignedInt(cpu.getMem().getLayout()[0x31a].read()));
				scoreDelta *= 256;
				
				score += (offset + scoreDelta);
			}
			
			usedControllerRequests = ((MultiTreeGui)gui).getRequests();
		}
	}
	
	public synchronized void setDeath(long cycle)
	{
		pause();
		
		deaths.add(cycle);
		deaths = deaths;
		
		--remainingLives;
		
		long scoreDelta = getGameScore();
		long offset = getScreenOffset();
		
		possibleScoreIncrement = ((offset << 32) + (scoreDelta << 8));
		setDone(clock.getPpuExpectedCycle());

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
		long currentScore = getGameScore();
		pause();
		long timeScore = (long)(255.0 - ((cycle - previousProgressCycle) / 5369317.5));
		System.out.println("Took " + ((cycle - previousProgressCycle) / 5369317.5) + " seconds");
		if (timeScore < 0)
		{
			timeScore = 0;
		}
	
		long offset = 255;
		long scoreDelta = currentScore;
		System.out.println("Score " + scoreDelta + " points");
		
		
		previousProgressCycle = cycle;
		previousProgressScore = currentScore;
		previousRemainingLives = remainingLives;
		previousProgressShots = getTotalShots();
		score += (timeScore + (offset << 32) + (scoreDelta << 8));
		
		cont();
	}
	
	private int getGameScore()
	{
		int retval = (cpu.getMem().read(0x07e3) << 8) + cpu.getMem().read(0x07e2);
		if (cpu.getMem().read(0x30) == 0 && cpu.getMem().read(0x64) == 0x0c)
		{
			//Count progress on beating level 1 boss that is not reflected in score
			System.out.println("At level 1 boss");
			int remainingHp = cpu.getMem().read(0x578) + 
					cpu.getMem().read(0x579) +
					cpu.getMem().read(0x57a) +
					cpu.getMem().read(0x57b) +
					cpu.getMem().read(0x57c) +
					cpu.getMem().read(0x57d) +
					cpu.getMem().read(0x57e) +
					cpu.getMem().read(0x57f) +
					cpu.getMem().read(0x580) +
					cpu.getMem().read(0x581) +
					cpu.getMem().read(0x582) +
					cpu.getMem().read(0x583) +
					cpu.getMem().read(0x584) +
					cpu.getMem().read(0x585) +
					cpu.getMem().read(0x586) +
					cpu.getMem().read(0x587);
			System.out.println("Remaining HP = " + remainingHp);
			retval += (256 * 16) - remainingHp;
		}
		
		return retval;
	}
	
	private long getScreenOffset()
	{
		return ((SaveMaxValuePort)cpu.getMem().getLayout()[0x65]).getMaxValue();
	}
	
	private long getTotalShots()
	{
		return gui.getTotalBPresses();
	}
}
