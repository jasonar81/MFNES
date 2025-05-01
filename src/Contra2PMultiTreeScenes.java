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

public class Contra2PMultiTreeScenes implements AiAgent{
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
	private ArrayList<Snapshot> snapshots;
	
	private static Contra2PMultiTreeScenes instance;
	
	private long firstUsableCycle = 62407559;
	private volatile long previousProgressCycle;
	private volatile long previousProgressScore;
	private volatile long remainingLives;
	private volatile long previousRemainingLives;
	private volatile long previousProgressShots;
	private TwoPlayerMultiDecisionTreeWithScenes tree;
	private TwoPlayerMultiTreeControllerWithScenes controller;
	private long numControllerRequests = 30000;
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
	
	private static boolean removeLastScene = false;
	
	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			removeLastScene = true;
		}
		
		instance = new Contra2PMultiTreeScenes();	
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
			disallow.add(0x33);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = RIGHT;
			tree = new TwoPlayerMultiDecisionTreeWithScenes(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new TwoPlayerMultiTreeControllerWithScenes(tree);
		tree.setValidStates(validStates);
		setup();
		load("contra.nes");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunAllMode();
		tree.reset();
		tree.setCpu(cpu);
		run();
		
		while (!done) {}
		
		snapshots = cpu.getSnapshots();
		cpu.resetSnapshots();
		printResults();
		System.out.println("Score of " + score);

		highScore = score;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests + 60000;
			setup();
			load("contra.nes");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			tree.setCpu(cpu);
			run();
			
			while (!done) {}
			
			snapshots = cpu.getSnapshots();
			cpu.resetSnapshots();
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
		
		if (removeLastScene)
		{
			tree.removeLastScene();
		}
		
		int sceneNum = tree.numScenes() - 1;
		countWithNoImprovement = tree.getLastNoImprovementCount();
		boolean relegated = false;
		while (true)
		{
			//play screen until no deaths 
			//if (!relegated)
			{
				sceneNum = tree.numScenes() - 1;
			}
			//else
			{
				relegated = false;
			}
			
			System.out.println("Working on scene " + sceneNum);
			if (!workOnScene(sceneNum))
			{
				if (sceneNum > 0)
				{
					sceneNum--;
					relegated = true;
				}
			}
			else
			{
				sceneNum++;
			}
			
			//play all
			numControllerRequests += 60000;
			setup();
			load("contra.nes");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			tree.setCpu(cpu);
			run();
			
			while (!done) {}
			
			snapshots = cpu.getSnapshots();
			cpu.resetSnapshots();
			printResults();
			System.out.println("Score of " + score);
			numControllerRequests = usedControllerRequests + 60000;
			highScore = score;
	
			teardown();
			saveTree();
		}
	}
	
	private boolean workOnScene(int sceneNum)
	{
		countWithNoImprovement = 0;
		long hs = 0;
		while (true)
		{
			Snapshot snap = snapshots.get(sceneNum);
			snap = cloneSnapshot(snap);
			setup(snap);
			cpu.setRestart();
			controller.setCount(snap.count);
			controller.setState(snap.state);
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunSceneMode(sceneNum, countWithNoImprovement);
			tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
			tree.setCpu(cpu);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
			
			HashSet<Integer> addressesAndValues = tree.getAddressesAndValues();
	
			teardown();
			
			if (score > hs && confirm(sceneNum, hs))
			{
				countWithNoImprovement = 0;
				hs = score;
				
				System.out.println("New high score with scene num = " + sceneNum);
				tree.persist();
				saveTree();
			} else if (score == hs)
			{
				countWithNoImprovement++;
				tree.persist();
				saveTree();
				
				if (countWithNoImprovement > 300)
				{
					return completedScene();
				}
			}
			else 
			{
				countWithNoImprovement++;
				tree.revert();
			}
			
			System.out.println("No improvement count: " + countWithNoImprovement);
			System.out.println("Addresses and values size = " + addressesAndValues.size());
			System.out.println("Completed scene = " + completedScene());
			
			tree.mutate(addressesAndValues);
		}
	}
	
	private boolean completedScene()
	{
		return tree.foundNextKey();
	}
	
	private boolean confirm(int sceneNum, long hs)
	{
		long minHighScore = score;
		Snapshot snap = snapshots.get(sceneNum);
		snap = cloneSnapshot(snap);
		setup(snap);
		cpu.setRestart();
		controller.setCount(snap.count);
		controller.setState(snap.state);
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunSceneMode(sceneNum, countWithNoImprovement);
		tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
		tree.setCpu(cpu);
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
		return (score > hs);
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("contra2p_scenes.tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			try
			{
				tree = (TwoPlayerMultiDecisionTreeWithScenes)i.readObject();
			}
			catch(Exception e)
			{
				i.close();
				f.close();
				f = new FileInputStream(file);
				i = new ObjectInputStream(f);
				TwoPlayerMultiDecisionTree temp = (TwoPlayerMultiDecisionTree)i.readObject();
				tree = new TwoPlayerMultiDecisionTreeWithScenes(temp);
			}
			
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
			File file = new File("contra2p_scenes.tree");
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
		long[] startOnOffTimes = new long[] {11312097, 12063248, 20774066, 21625600};
		gui = new TwoPlayerMultiTreeGuiWithScenes(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
		long[] selectTimes = new long[] {16166121, 16876455};
		((TwoPlayerMultiTreeGuiWithScenes)gui).setSelectTimes(selectTimes);
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
		cpu.set2PController(controller);
	}
	
	private Snapshot cloneSnapshot(Snapshot snap)
	{
		Snapshot dest = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(snap);
			out.flush();
			byte[] temp = bos.toByteArray();
			out.close();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			ObjectInputStream in = new ObjectInputStream(bis);
			dest = (Snapshot)in.readObject();
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		return dest;
	}
	
	private void setup(Snapshot snap)
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
		
		clock = snap.clock;
		long[] startOnOffTimes = new long[] {};
		gui = new TwoPlayerMultiTreeGuiWithScenes(160000, 0, controller, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		deaths.clear();
		deaths = deaths;
		
		ppuMem = snap.ppuMem;
		ppu = snap.ppu;
		cpuMem = snap.cpuMem;
		cpu = snap.cpu;
		apu = snap.apu;
		cpu.setApu(apu);
		ppu.setCPU(cpu);
		gui.setCpu(cpu);
		gui.setClock(clock);
		cpu.set2PController(controller);
		
		apu.setGui(gui);
		cpu.setGui(gui);
		ppuMem.setGui(gui);
		cpuMem.setGui(gui);
		ppu.setGui(gui);
		
		gui.setAgent(this);
		clock.periodNanos = 1.0;
	
		((DoneRamPort)cpu.getMem().getLayout()[0x3a]).setAgent(this);
		((SaveMaxValueAndClearElsewherePort)cpu.getMem().getLayout()[0x64]).setAgent(this);
		((SaveMaxValueAndClearElsewherePort)cpu.getMem().getLayout()[0x30]).setAgent(this);
		((DeathPort)cpu.getMem().getLayout()[0xb4]).setAgent(this);
		((DeathPort)cpu.getMem().getLayout()[0xb5]).setAgent(this);
		((Register4016)cpu.getMem().getLayout()[0x4016]).setGui(gui);
		((Register4017)cpu.getMem().getLayout()[0x4017]).setGui(gui);
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
		clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x34] = new RomMemoryPort((byte)0); //Fix the randomizer value
		//cpu.getMem().getLayout()[0x32] = new RomMemoryPort((byte)63); //Always report back 63 lives remaining
		cpu.getMem().getLayout()[0x3a] = new DoneRamPort((byte)2, this, clock); //When continues decrements to 2, call it a wrap
		cpu.getMem().getLayout()[0x65] = new SaveMaxValuePort(); //Distance into current screen
		cpu.getMem().getLayout()[0x64] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x65], false, true, this, clock); //Screen number in level
		cpu.getMem().getLayout()[0x30] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x64], false, true, this, clock); //Level
		cpu.getMem().getLayout()[0xb4] = new DeathPort((byte)1, this, clock); //Detect a death
		cpu.getMem().getLayout()[0xb5] = new DeathPort((byte)1, this, clock); //Detect a death
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
				score += partialScore();
			}
			
			usedControllerRequests = ((TwoPlayerMultiTreeGuiWithScenes)gui).getRequests();
		}
	}
	
	public synchronized void setDeath(long cycle)
	{
		pause();
		
		deaths.add(cycle);
		deaths = deaths;
		
		--remainingLives;
		
		possibleScoreIncrement = partialScore();
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
	
	private long partialScore()
	{
		long scoreDelta = getGameScore();
		long offset = getScreenOffset();
		
		if (cpu.getMem().read(0x30) == 0 && cpu.getMem().read(0x64) == 0x0c)
		{
			return (scoreDelta << 8);
		}
		
		return ((offset << 32) + (scoreDelta << 8));
	}
	
	private int getGameScore()
	{
		int retval = (cpu.getMem().read(0x07e3) << 8) + cpu.getMem().read(0x07e2);
		retval += (cpu.getMem().read(0x07e5) << 8) + cpu.getMem().read(0x07e4);
		retval += cpu.getMem().read(0x37);
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
					cpu.getMem().read(0x587) +
					cpu.getMem().read(0x86);
			System.out.println("Remaining HP = " + remainingHp);
			retval += (256 * 17) - remainingHp;
		
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
