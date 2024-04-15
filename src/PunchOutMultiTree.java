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

public class PunchOutMultiTree implements AiAgent {
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
	private volatile int currentLevel = 0;
	private volatile boolean knockOut = false;
	private volatile boolean justFinishedLevel = false;
	private volatile long previousCycle;
	
	private static PunchOutMultiTree instance;
	
	private long firstUsableCycle = 62856095;
	private volatile ArrayList<Long> screenScores;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private MultiDecisionTree tree;
	private MultiTreeController controller;
	private long numControllerRequests = 40000;
	
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
		instance = new PunchOutMultiTree();
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
			addresses.add(4);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0xa);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = DOWN;
			tree = new MultiDecisionTree(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new MultiTreeController(tree);
		tree.setValidStates(validStates);
		setup();
		load("punch_out.nes", "sav");
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
			load("punch_out.nes", "sav");
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
			load("punch_out.nes", "sav");
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
			load("punch_out.nes", "sav");
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
		load("punch_out.nes", "sav");
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
			File file = new File("punch_out_scenes.tree");
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
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x04] = new NotifyChangesPort(this, clock);
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
			if (justFinishedLevel)
			{
				justFinishedLevel = false;
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			}
		}
		
		if (cpu.getMem().getLayout()[4].read() == ((byte)0xff))
		{
			score++;
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
