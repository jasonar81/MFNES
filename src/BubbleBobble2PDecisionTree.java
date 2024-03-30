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

public class BubbleBobble2PDecisionTree implements AiAgent {
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
	private volatile long highScore2 = 0;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile int currentLevel = 1;
	private volatile long previousScore = 0;
	private volatile long previousFinishTime = 0;
	
	private static BubbleBobble2PDecisionTree instance;
	
	private long firstUsableCycle = 52853029;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private TwoPlayerMutatingDecisionTree tree;
	private TwoPlayerDecisionTreeController controller;
	private long numControllerRequests = 20000;
	private TwoPlayerMutatingDecisionTree tree2;
	private TwoPlayerDecisionTreeController controller2;
	private long numControllerRequests2 = 20000;
	private TwoPlayerMutatingDecisionTree tree3;
	private TwoPlayerDecisionTreeController controller3;
	
	private long usedControllerRequests;
	
	private static int A = 0x80;
	private static int B = 0x40;
	private static int UP = 0x20;
	private static int DOWN = 0x10;
	private static int LEFT = 0x08;
	private static int RIGHT = 0x04;
	private static int SELECT = 0x02;
	private static int START = 0x01;
	
	
	public static void main(String[] args)
	{
		instance = new BubbleBobble2PDecisionTree();
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
			NewMutatingDecisionTree p1Tree = load1PTree("bubble_bobble.tree");
			NewMutatingDecisionTree p2Tree = load1PTree("bubble_bobble.tree2");
			tree = new TwoPlayerMutatingDecisionTree(validStates, p1Tree, p2Tree);
			controller = new TwoPlayerDecisionTreeController(tree);
		}
		
		tree.setValidStates(validStates);
		setup();
		load("bubble_bobble.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		run();
		
		while (!done) {}
		
		printResults();
		System.out.println("Score of " + score);

		processScreenResults();
		highScore = score;
		System.out.println("New high score!");
		
		HashSet<Integer> addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
		HashSet<Integer> previous;
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
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);

			processScreenResults();
			
			addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (score > highScore && confirm(1))
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
		
		if (!loadTree2())
		{
			tree2 = new TwoPlayerMutatingDecisionTree(validStates);
			controller2 = new TwoPlayerDecisionTreeController(tree2);
		}
		
		tree2.setValidStates(validStates);
		setup2();
		load("bubble_bobble.nes", "sav");
		makeModifications();
		controller2.reset();
		controller2.setCpuMem(cpuMem);
		controller2.setTree(tree2);
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + score);

		highScore2 = score;
		if (highScore2 > highScore)
		{
			System.out.println("New high score!");
		}
		
		HashSet<Integer> addressesAndValues2 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
		HashSet<Integer> previous2;
		teardown();
		
		while (true)
		{
			numControllerRequests2 = usedControllerRequests * 3;
			setup2();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller2.reset();
			controller2.setCpuMem(cpuMem);
			controller2.setTree(tree2);
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
			
			addressesAndValues2 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (score > highScore2 && confirm(2))
			{
				highScore2 = score;
				
				if (score > highScore)
				{
					System.out.println("New high score!");
				}
				
				saveTree2();
			}
			else
			{
				break;
			}
		}
		
		tree3 = new TwoPlayerMutatingDecisionTree(validStates);
		controller3 = new TwoPlayerDecisionTreeController(tree3);
		
		while (true)
		{
			tree.mutate(addressesAndValues);
			previous = addressesAndValues;
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
			
			previous = addressesAndValues;
			addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (score > highScore && confirm(1))
			{
				highScore = score;
				System.out.println("New high score!");
				saveTree();
				numControllerRequests = usedControllerRequests * 3;
				
				tree.persist();
			}
			else if (score == highScore)
			{
				saveTree();
				tree.persist();
			}
			else
			{
				tree.revert();
				saveTree();
				addressesAndValues = previous;
			}
			
			tree2.mutate(addressesAndValues2);
			previous2 = addressesAndValues2;
			setup2();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller2.reset();
			controller2.setCpuMem(cpuMem);
			controller2.setTree(tree2);
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
			
			previous2 = addressesAndValues2;
			addressesAndValues2 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (score > highScore2 && confirm(2))
			{
				if (score > highScore)
				{
					System.out.println("New high score!");
					highScore2 = highScore;
					highScore = score;
					HashSet<Integer> aav = addressesAndValues;
					addressesAndValues = addressesAndValues2;
					addressesAndValues2 = aav;
					tree3.setRoots(tree2.getRootsClones());
					tree2.setRoots(tree.getRootsClones());
					tree.setRoots(tree3.getRootsClones());
					tree3.resetRoots();
					tree.reindex();
					tree2.reindex();
					saveTree();
					saveTree2();
					long temp = numControllerRequests;
					numControllerRequests = usedControllerRequests * 3;
					numControllerRequests2 = temp;
				}
				else
				{
					highScore2 = score;
					saveTree2();
					numControllerRequests2 = usedControllerRequests * 3;
				
					tree2.persist();
				}
			}
			else if (score == highScore2)
			{
				if (score > highScore)
				{
					highScore2 = highScore;
					highScore = score;
					HashSet<Integer> aav = addressesAndValues;
					addressesAndValues = addressesAndValues2;
					addressesAndValues2 = aav;
					tree3.setRoots(tree2.getRootsClones());
					tree2.setRoots(tree.getRootsClones());
					tree.setRoots(tree3.getRootsClones());
					tree3.resetRoots();
					tree.reindex();
					tree2.reindex();
					saveTree();
					saveTree2();
					long temp = numControllerRequests;
					numControllerRequests = usedControllerRequests * 3;
					numControllerRequests2 = temp;
				}
				else
				{
					saveTree2();
					tree2.persist();
				}
			}
			else
			{
				tree2.revert();
				saveTree2();
				addressesAndValues2 = previous2;
			}
			
			tree3.setRoots(tree.merge(tree2, addressesAndValues, addressesAndValues2));
			tree3.reindex();
			setup3();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			controller3.reset();
			controller3.setCpuMem(cpuMem);
			controller3.setTree(tree3);
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
	
			HashSet<Integer> addressesAndValues3 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
			teardown();
			if (score > highScore2 && score > highScore && confirm(3))
			{
				highScore = score;
				highScore2 = -1;
				addressesAndValues = addressesAndValues3;
				System.out.println("New high score!");
				tree.setRoots(tree3.getRootsClones());
				tree2.resetRoots();
				tree3.resetRoots();
				tree.reindex();
				tree2.reindex();
				saveTree();
				saveTree2();
				numControllerRequests2 = 20000;
				numControllerRequests = usedControllerRequests * 3;
			}
		}
	}
	
	private boolean confirm(int num)
	{
		int NUM_CONFIRMS = 2;
		long minFinalScore = score;
		for (int i = 0; i < NUM_CONFIRMS; ++i)
		{
			if (num == 1)
			{
				setup();
				load("bubble_bobble.nes", "sav");
				makeModifications();
				controller.reset();
				controller.setCpuMem(cpuMem);
				controller.setTree(tree);
				run();
				
				while (!done) {}
				
				printResults();
				System.out.println("Score of " + score);
		
				teardown();
				if (!(score > highScore))
				{
					return false;
				}
			} else if (num == 2)
			{
				setup2();
				load("bubble_bobble.nes", "sav");
				makeModifications();
				controller2.reset();
				controller2.setCpuMem(cpuMem);
				controller2.setTree(tree2);
				run();
				
				while (!done) {}
				
				printResults();
				System.out.println("Score of " + score);
		
				teardown();
				if (!(score > highScore2))
				{
					return false;
				}
			}
			else
			{
				setup3();
				load("bubble_bobble.nes", "sav");
				makeModifications();
				controller3.reset();
				controller3.setCpuMem(cpuMem);
				controller3.setTree(tree3);
				run();
				
				while (!done) {}
				
				printResults();
				System.out.println("Score of " + score);
		
				teardown();
				if (!(score > highScore && score > highScore2))
				{
					return false;
				}
			}
			
			if (score < minFinalScore)
			{
				minFinalScore = score;
			}
		}
		
		score = minFinalScore;
		return true;
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("bubble_bobble2p.tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree = (TwoPlayerMutatingDecisionTree)i.readObject();
			controller = new TwoPlayerDecisionTreeController(tree);
	
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
			File file = new File("bubble_bobble2p.tree");
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
	
	private boolean loadTree2()
	{
		try
		{
			File file = new File("bubble_bobble2p.tree2");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree2 = (TwoPlayerMutatingDecisionTree)i.readObject();
			controller2 = new TwoPlayerDecisionTreeController(tree2);
	
			i.close();
			f.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean saveTree2()
	{
		try
		{
			File file = new File("bubble_bobble2p.tree2");
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
	
			o.writeObject(tree2);
			o.close();
			f.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private NewMutatingDecisionTree load1PTree(String filename)
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				return null;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			NewMutatingDecisionTree tree = (NewMutatingDecisionTree)i.readObject();
	
			i.close();
			f.close();
			
			return tree;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
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
		
		long[] startOnOffTimes = new long[] {24791604, 25812912, 31096147, 31933297};
		clock = new Clock();
		gui = new TwoPlayerDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {28701596, 29323805};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
	
	private void setup2()
	{
		previousFinishTime = firstUsableCycle;
		previousScore = 0;
		currentLevel = 1;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {24791604, 25812912, 31096147, 31933297};
		clock = new Clock();
		gui = new TwoPlayerDecisionTreeGui(numControllerRequests2, firstUsableCycle, controller2, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {28701596, 29323805};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
	
	private void setup3()
	{
		previousFinishTime = firstUsableCycle;
		previousScore = 0;
		currentLevel = 1;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {24791604, 25812912, 31096147, 31933297};
		clock = new Clock();
		gui = new TwoPlayerDecisionTreeGui(Math.max(numControllerRequests, numControllerRequests2), firstUsableCycle, controller3, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {28701596, 29323805};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
		cpu.debugHold(true);;
		cpuThread.start();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x2e] = new NotifyChangesPort(this, clock); //Lives remaining P1
		cpu.getMem().getLayout()[0x42] = new NotifyChangesPort(this, clock); //Lives remaining P2
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
			usedControllerRequests = ((TwoPlayerDecisionTreeGui)gui).getRequests();
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
			else if (cpu.getMem().getLayout()[0x2e].read() == 0)
			{
				if (cpu.getMem().getLayout()[0x42].read() == 0)
				{
					setDone(cycle);
				}
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
		lives += cpu.getMem().read(0x42);
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
		int val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x3d].read());
		if (val != 0x27)
		{
			retval += val;
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x3c].read());
		if (val != 0x27)
		{
			retval += (val * 10);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x3b].read());
		if (val != 0x27)
		{
			retval += (val * 100);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x3a].read());
		if (val != 0x27)
		{
			retval += (val * 1000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x39].read());
		if (val != 0x27)
		{
			retval += (val * 10000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x38].read());
		if (val != 0x27)
		{
			retval += (val * 100000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x51].read());
		if (val != 0x27)
		{
			retval += val;
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x50].read());
		if (val != 0x27)
		{
			retval += (val * 10);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x4f].read());
		if (val != 0x27)
		{
			retval += (val * 100);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x4e].read());
		if (val != 0x27)
		{
			retval += (val * 1000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x4d].read());
		if (val != 0x27)
		{
			retval += (val * 10000);
		}
		
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x4c].read());
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
