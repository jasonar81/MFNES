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

public class PunchOutMultiTreeScenes implements AiAgent {
	private Clock clock;
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	private Memory ppuMem;
	private Memory cpuMem;
	private Thread cpuThread;
	private ArrayList<Snapshot> snapshots;
	
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
	
	private static PunchOutMultiTreeScenes instance;
	
	private long firstUsableCycle = 62856095;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private MultiDecisionTreeWithScenes tree;
	private MultiTreeControllerWithScenes controller;
	private long numControllerRequests = 200000;
	
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
		instance = new PunchOutMultiTreeScenes();
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
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			addresses.add(1);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0xa);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = DOWN;
			tree = new MultiDecisionTreeWithScenes(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new MultiTreeControllerWithScenes(tree);
		tree.setValidStates(validStates);
		setup();
		load("punch_out.nes", "sav");
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
			numControllerRequests = usedControllerRequests + 400000;
			setup();
			load("punch_out.nes", "sav");
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
			numControllerRequests += 400000;
			setup();
			load("punch_out.nes", "sav");
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
			numControllerRequests = usedControllerRequests + 400000;
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
			File file = new File("punch_out_scenes.tree");
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
			File file = new File("punch_out_scenes.tree");
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
		previousCycle = firstUsableCycle;
		knockOut = false;
		currentLevel = 0;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {61779177, 62856094};
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
		previousCycle = firstUsableCycle;
		knockOut = false;
		currentLevel = 0;
		screenScores = new ArrayList<Long>();
		score = 0;
		done = false;
		startedDone = false;
		
		clock = snap.clock;
		long[] startOnOffTimes = new long[] {};
		gui = new MultiTreeGuiWithScenes(800000, 0, controller, startOnOffTimes, clock);
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
	
		((NotifyChangesPort)cpu.getMem().getLayout()[0x01]).setAgent(this); 
		((NotifyChangesPort)cpu.getMem().getLayout()[0x0a]).setAgent(this); 
		((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).setAgent(this); 
		((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).setAgent(this); 
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
		System.out.println("Level = " + cpu.getMem().read(0x01));
		System.out.println("Total damage delivered " + enemyDamage());
		System.out.println("Total damage sustained " + myDamage());
	}
	
	private void on()
	{
		
		
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		
		
		cpu.debugHold(true);
		
		
		
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
		clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x01] = new NotifyChangesPort(this, clock);
		cpu.getMem().getLayout()[0x0a] = new NotifyChangesPort(this, clock);
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
			long screenScore = partialScore(knockOut, totalTime);
			screenScores.add(screenScore);
			score += screenScore;
			cont();
			System.out.println("Screen scores size is " + screenScores.size());
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
		
		//We got pinged because of a level change or damage to us or our opponent
		//We could be knocked out, or a loss
		
		if (cpu.getMem().getLayout()[0x0a].read() == 1)
		{
			//Knocked out
			knockOut = true;
			System.out.println("Knocked out");
			setDone(cycle);
			return;
		}
		
		int level = cpu.getMem().getLayout()[0x01].read();
		if (level > currentLevel)
		{
			//new level 
			System.out.println("Finished level");
			currentLevel = level;
			long screenScore = finishedScreenScore(cycle);
			screenScores.add(screenScore);
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			score += screenScore;
			justFinishedLevel = true;
		}
		else if (cpu.getMem().getLayout()[0x391].read() == 0)
		{
			if (justFinishedLevel)
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
	
	private long partialScore(boolean ko, long cycle)
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
		
		long seconds = (long)((cycle - previousCycle) / 5369317.5);
		if (seconds > 2550)
		{
			seconds = 2550;
		}
		
		return (enemyDamage << 24) + ((256 * 9 - myDamage) << 8); 
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
