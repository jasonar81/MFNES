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

public class Contra2PDecisionTree implements AiAgent {
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
	private volatile long highScore2 = 0;
	private volatile boolean done;
	private volatile boolean startedDone;
	private volatile ArrayList<Long> deaths = new ArrayList<Long>();
	private volatile long score;
	private volatile long possibleScoreIncrement;
	
	private static Contra2PDecisionTree instance;
	
	private long firstUsableCycle = 62407559;
	private volatile long previousProgressCycle;
	private volatile long previousProgressScore;
	private volatile long remainingLives;
	private volatile long previousRemainingLives;
	private TwoPlayerMutatingDecisionTree tree;
	private TwoPlayerDecisionTreeController controller;
	private long numControllerRequests = 10000;
	private TwoPlayerMutatingDecisionTree tree2;
	private TwoPlayerDecisionTreeController controller2;
	private long numControllerRequests2 = 10000;
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
		instance = new Contra2PDecisionTree();
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
			NewMutatingDecisionTree p1Tree = load1PTree("contra.tree");
			NewMutatingDecisionTree p2Tree = load1PTree("contra.tree2");
			tree = new TwoPlayerMutatingDecisionTree(validStates, p1Tree, p2Tree);
			controller = new TwoPlayerDecisionTreeController(tree);
		}
		
		tree.setValidStates(validStates);
		setup();
		load("contra.nes");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		run();
		
		while (!done) {}
		
		printResults();
		System.out.println("Score of " + score);

		highScore = score;
		System.out.println("New high score!");
		
		HashSet<Integer> addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
		HashSet<Integer> previous;
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
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
			
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
			IfElseNode[] clones = tree2.getRootsClones();
			clones[0].terminal = true;
			clones[0].terminalValue = RIGHT;
			clones[1].terminal = true;
			clones[1].terminalValue = RIGHT;
			tree2.setRoots(clones);
			tree2.reindex();
			controller2 = new TwoPlayerDecisionTreeController(tree2);
		}
		
		tree2.setValidStates(validStates);
		setup2();
		load("contra.nes");
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
			load("contra.nes");
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
			load("contra.nes");
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
			load("contra.nes");
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
			load("contra.nes");
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
				numControllerRequests2 = 10000;
				saveTree();
				saveTree2();
				numControllerRequests = usedControllerRequests * 3;
			}
		}
	}
	
	private boolean confirm(int num)
	{
		int NUM_CONFIRMS = 1;
		long minFinalScore = score;
		for (int i = 0; i < NUM_CONFIRMS; ++i)
		{
			if (num == 1)
			{
				setup();
				load("contra.nes");
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
				load("contra.nes");
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
				load("contra.nes");
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
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("contra2p.tree");
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
			File file = new File("contra2p.tree");
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
			File file = new File("contra2p.tree2");
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
			File file = new File("contra2p.tree2");
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
	
	private void setup()
	{
		score = 0;
		previousProgressCycle = 0;
		previousProgressScore = 0;
		remainingLives = 65536;
		previousRemainingLives = 65536;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		long[] startOnOffTimes = new long[] {11312097, 12063248, 20774066, 21625600};
		gui = new TwoPlayerDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {16166121, 16876455};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
	
	private void setup2()
	{
		score = 0;
		previousProgressCycle = 0;
		previousProgressScore = 0;
		remainingLives = 65536;
		previousRemainingLives = 65536;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		long[] startOnOffTimes = new long[] {11312097, 12063248, 20774066, 21625600};
		gui = new TwoPlayerDecisionTreeGui(numControllerRequests2, firstUsableCycle, controller2, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {16166121, 16876455};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
	
	private void setup3()
	{
		score = 0;
		previousProgressCycle = 0;
		previousProgressScore = 0;
		remainingLives = 65536;
		previousRemainingLives = 65536;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		long[] startOnOffTimes = new long[] {11312097, 12063248, 20774066, 21625600};
		gui = new TwoPlayerDecisionTreeGui(Math.max(numControllerRequests, numControllerRequests2), firstUsableCycle, controller3, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		long[] selectTimes = new long[] {16166121, 16876455};
		((TwoPlayerDecisionTreeGui)gui).setSelectTimes(selectTimes);
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
		//cpu.getMem().getLayout()[0x32] = new RomMemoryPort((byte)2); //Always report back 63 lives remaining
		//cpu.getMem().getLayout()[0x33] = new RomMemoryPort((byte)2); //Always report back 63 lives remaining
		cpu.getMem().getLayout()[0x3a] = new DoneRamPort((byte)2, this, clock); //When continues decrements to 2, call it a wrap
		cpu.getMem().getLayout()[0x65] = new SaveMaxValuePort(); //Distance into current screen
		cpu.getMem().getLayout()[0x64] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x65], false, true, this, clock); //Screen number in level
		cpu.getMem().getLayout()[0x30] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x64], false, true, this, clock); //Level
		cpu.getMem().getLayout()[0xb4] = new DeathPort((byte)1, this, clock); //Detect a death P1
		cpu.getMem().getLayout()[0xb5] = new DeathPort((byte)1, this, clock); //Detect a death P2
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			startedDone = true;
			score += possibleScoreIncrement;
			done = true;
			usedControllerRequests = ((TwoPlayerDecisionTreeGui)gui).getRequests();
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
			return;
		}
		long scoreDelta = getGameScore() - previousProgressScore;
		System.out.println("There were " + deaths.size() + " deaths");
		
		long offset = getScreenOffset();
		offset *= (256 * 256);
		scoreDelta *= 256;
		
		possibleScoreIncrement = (offset + scoreDelta);
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
		
		previousProgressCycle = cycle;
		previousProgressScore = currentScore;
		previousRemainingLives = remainingLives;
		score += (lives + timeScore + offset + scoreDelta);
		
		cont();
	}
	
	private int getGameScore()
	{
		int retval = (cpu.getMem().read(0x07e3) << 8) + cpu.getMem().read(0x07e2);
		retval += (cpu.getMem().read(0x07e5) << 8) + cpu.getMem().read(0x07e4);
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
}
