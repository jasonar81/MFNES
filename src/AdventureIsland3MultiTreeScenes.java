import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class AdventureIsland3MultiTreeScenes implements AiAgent {
	private Clock clock;
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	private Memory ppuMem;
	private Memory cpuMem;
	private Thread cpuThread;
	private GUI gui;
	private Thread guiThread;
	private volatile double highScore = 0;
	private volatile double finalScore;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile long livesLost;
	private static HashSet<Integer> levels = new HashSet<Integer>();
	private ArrayList<Snapshot> snapshots;
	
	private static AdventureIsland3MultiTreeScenes instance;
	
	private long firstUsableCycle = 42326988;
	private MultiDecisionTreeWithScenes tree;
	private MultiTreeControllerWithScenes controller;
	private long numControllerRequests = 5000;
	
	private long usedControllerRequests;
	private long previousCycle;
	
	private static int A = 0x80;
	private static int B = 0x40;
	private static int UP = 0x20;
	private static int DOWN = 0x10;
	private static int LEFT = 0x08;
	private static int RIGHT = 0x04;
	private static int SELECT = 0x02;
	private static int START = 0x01;
	
	private int countWithNoImprovement = 0;
	private int prevOffset;
	private int prevLevel;
	
	public static void main(String[] args)
	{
		instance = new AdventureIsland3MultiTreeScenes();
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
		validStates.add(START);
		if (!loadTree())
		{
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			addresses.add(0xd0);
			addresses.add(0xd1);
			addresses.add(0xb3);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0x394);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = RIGHT;
			tree = new MultiDecisionTreeWithScenes(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new MultiTreeControllerWithScenes(tree);
		tree.setValidStates(validStates);
		setup();
		load("adventure_island3.nes", "sav");
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
		System.out.println("Score of " + finalScore);

		highScore = finalScore;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests + 10000;
			setup();
			load("adventure_island3.nes", "sav");
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
			System.out.println("Score of " + finalScore);
	
			teardown();
			if (finalScore > highScore)
			{
				highScore = finalScore;
				System.out.println("New high score!");
				saveTree();
			}
			else
			{
				highScore = finalScore;
				break;
			}
		}
		
		int sceneNum = tree.numScenes() - 1;
		countWithNoImprovement = tree.getLastNoImprovementCount();
		boolean relegated = false;
		while (true)
		{
			//play screen until no deaths 
			if (!relegated)
			{
				sceneNum = tree.numScenes() - 1;
			}
			else
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
			numControllerRequests += 10000;
			setup();
			load("adventure_island3.nes", "sav");
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
			System.out.println("Score of " + finalScore);
			numControllerRequests = usedControllerRequests + 10000;
			highScore = finalScore;
	
			teardown();
			saveTree();
		}
	}
	
	private boolean workOnScene(int sceneNum)
	{
		countWithNoImprovement = 0;
		double hs = 0;
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
			System.out.println("Score of " + finalScore);
			
			HashSet<Integer> addressesAndValues = tree.getAddressesAndValues();
	
			teardown();
			
			if (finalScore > hs && confirm(sceneNum, hs))
			{
				countWithNoImprovement = 0;
				hs = finalScore;
				
				System.out.println("New high score with scene num = " + sceneNum);
				tree.persist();
				saveTree();
			} else if (finalScore == hs)
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
	
	private boolean confirm(int sceneNum, double hs)
	{
		double minHighScore = finalScore;
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
		System.out.println("Score of " + finalScore);
		
		if (finalScore < minHighScore)
		{
			minHighScore = finalScore;
		}

		teardown();
		
		finalScore = minHighScore;
		return (finalScore > hs);
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("adventure_island3_scenes.tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			try
			{
				tree = (MultiDecisionTreeWithScenes)i.readObject();
			}
			catch(Exception e)
			{
				i.close();
				f.close();
				f = new FileInputStream(file);
				i = new ObjectInputStream(f);
				MultiDecisionTree temp = (MultiDecisionTree)i.readObject();
				tree = new MultiDecisionTreeWithScenes(temp);
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
			File file = new File("adventure_island3_scenes.tree");
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
		levels.clear();
		livesLost = 0;
		score = 0;
		done = false;
		startedDone = false;
		prevOffset = 0;
		prevLevel = 0;
		
		long[] startOnOffTimes = new long[] {14926749, 15876563, 21252721, 22065701, 31540991,
				32753013, 41099303, 42326987};
		clock = new Clock();
		gui = new MultiTreeGuiWithScenes(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
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
		cpu.setController(controller);
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
		levels.clear();
		livesLost = 0;
		score = 0;
		done = false;
		startedDone = false;
		prevOffset = 0;
		prevLevel = 0;
		
		clock = snap.clock;
		long[] startOnOffTimes = new long[] {};
		gui = new MultiTreeGuiWithScenes(10000, 0, controller, startOnOffTimes, clock);
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		
		ppuMem = snap.ppuMem;
		ppu = snap.ppu;
		cpuMem = snap.cpuMem;
		cpu = snap.cpu;
		apu = snap.apu;
		cpu.setApu(apu);
		ppu.setCPU(cpu);
		gui.setCpu(cpu);
		gui.setClock(clock);
		cpu.setController(controller);
		
		apu.setGui(gui);
		cpu.setGui(gui);
		ppuMem.setGui(gui);
		cpuMem.setGui(gui);
		ppu.setGui(gui);
		
		gui.setAgent(this);
		clock.periodNanos = 1.0;
	
		((NotifyChangesPort)cpu.getMem().getLayout()[0x76]).setAgent(this); 
		((NotifyChangesPort)cpu.getMem().getLayout()[0xb3]).setAgent(this); 
		((Register4016)cpu.getMem().getLayout()[0x4016]).setGui(gui);
		((Register4017)cpu.getMem().getLayout()[0x4017]).setGui(gui);
		progress(0);
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
		System.out.println("Game score = " + gameScore());
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
		clock.periodNanos = 1.0;
		//cpu.getMem().getLayout()[0x394] = new NotifyChangesPort(this, clock); //Lives remaining
		cpu.getMem().getLayout()[0x76] = new NotifyChangesPort(this, clock); //dying status
		cpu.getMem().getLayout()[0xb3] = new NotifyChangesPort(this, clock); 
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			++livesLost;
			score += gameScore();
			finalScore = score;
			done = true;
			usedControllerRequests = ((MultiTreeGuiWithScenes)gui).getRequests();
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
	
	public synchronized void progress(long cycle)
	{
		pause();
		
		if (cycle >= firstUsableCycle)
		{
			if (cpu.getMem().getLayout()[0x76].read() == 1)
			{
				setDone(cycle);
				return;
			}
			
			int world = Byte.toUnsignedInt(cpu.getMem().getLayout()[0xd0].read());
			int level = Byte.toUnsignedInt(cpu.getMem().getLayout()[0xd1].read());
			int offset = Byte.toUnsignedInt(cpu.getMem().getLayout()[0xb3].read());
			int flag = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x99].read());
			level += (world << 8) + (offset << 16) + (flag << 24);
			
			if (levels.add(level) && (offset > prevOffset || prevLevel != level))
			{
				score += fullScore(cycle);
				prevOffset = offset;
				prevLevel = level;
			}
		}
		
		cont();
	}
	
	private long fullScore(long cycle)
	{
		long life = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x390].read());
		long seconds = (long)((cycle - previousCycle) / 5369317.5);
		if (seconds > 255)
		{
			seconds = 255;
		}
		
		previousCycle = cycle;
		
		return (levels.size() << 40) + (life << 32) + ((255 - seconds) << 24) + gameScore();
	}
	
	private long gameScore()
	{
		long retval = 0;
		int val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x391].read());
		retval += val;
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x392].read());
		retval += val * 256;
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x393].read());
		retval += val * 256 * 256;
		return retval;
	}
	
	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
}
