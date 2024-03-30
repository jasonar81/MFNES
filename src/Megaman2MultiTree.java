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

public class Megaman2MultiTree implements AiAgent {
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
	
	private static Megaman2MultiTree instance;
	
	private long firstUsableCycle = 41020224;
	private MultiDecisionTree tree;
	private MultiTreeController controller;
	private long numControllerRequests = 5000;
	
	private int previousLives = 0;
	private int previousBossHp = 0;
	private int previousScreen = 0;
	private int previousLevel = 0;
	
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
		instance = new Megaman2MultiTree();
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
			addresses.add(0x20);
			addresses.add(0x2a);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0xa8);
			IfElseNode defaultTree = new IfElseNode();
			defaultTree.terminal = true;
			defaultTree.terminalValue = RIGHT;
			tree = new MultiDecisionTree(validStates, addresses, defaultTree, disallow);
		}
		
		controller = new MultiTreeController(tree);
		tree.setValidStates(validStates);
		setup();
		load("megaman2.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunAllMode();
		tree.reset();
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + score);
		highScore = score;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests * 3;
			setup();
			load("megaman2.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			run();
			
			while (!done) {}
			
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
			load("megaman2.nes", "sav");
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
			load("megaman2.nes", "sav");
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
		load("megaman2.nes", "sav");
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
			File file = new File("megaman2_scenes.tree");
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
			File file = new File("megaman2_scenes.tree");
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
		previousLives = 0;
		previousBossHp = 0;
		previousScreen = 0;
		previousLevel = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		
		long[] startOnOffTimes = new long[] {10890547, 11309846, 14062612, 14921603, 29152813, 30020223, 40020223, 41020223};
		gui = new MultiTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
		long[] rightTimes = new long[] {37020223, 38020223};
		((MultiTreeGui)gui).setRightTimes(rightTimes);
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
		cpu.getMem().getLayout()[0xa8] = new NotifyChangesPort(this, clock); //Detect a death
		cpu.getMem().getLayout()[0x6c1] = new NotifyChangesPort(this, clock); //Boss HP
		cpu.getMem().getLayout()[0x20] = new NotifyChangesPort(this, clock); //screen
		cpu.getMem().getLayout()[0x2a] = new NotifyChangesPort(this, clock); //level
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			startedDone = true;
			score += getScore();
			done = true;
			usedControllerRequests = ((MultiTreeGui)gui).getRequests();
		}
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
		
		int bossHp = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x6c1].read());
		int lives = cpu.getMem().getLayout()[0xa8].read();
		int screen = cpu.getMem().getLayout()[0x20].read();
		int level = cpu.getMem().getLayout()[0x2a].read();
		if (screen > previousScreen || level != previousLevel)
		{
			score += getScore();
		}
		
		if (previousBossHp != bossHp)
		{
			if (previousBossHp > 0 && bossHp <= 0)
			{
				//Defeated boss
				score += 0xffffff;
			}
		}
		
		if (previousLives != 0)
		{
			//Died
			if (lives == 2)
			{
				setDone(cycle);
				return;
			}
		}
		
		previousBossHp = bossHp;
		previousLives = lives;
		previousScreen = screen;
		previousLevel = level;
		
		cont();
	}
	
	private long getScore()
	{
		long screen = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x20].read());
		long xPos = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x460].read());
		long yPos = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x4a0].read());
		long level = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x2a].read());
		long bossHp = cpu.getMem().getLayout()[0x6c1].read();
		long myHp = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x6c0].read());
		long bossDamage = 0;
		if (bossHp != 0)
		{
			bossDamage = 0x7f - bossHp;
		}
		
		if (bossDamage < 0)
		{
			bossDamage = 0;
		}
		
		long offset = 0;
		
		if (level == 2)
		{
			if (screen == 5 || screen == 10 || screen == 16 || screen == 18)
			{
				offset = 256 - xPos;
			}
			else
			{
				offset = xPos;
			}
			
			if (screen == 9 || screen == 10 || screen == 11)
			{
				offset += (256 - yPos);
			}
			else if (screen == 4 || screen == 5 || screen == 15 || screen == 16 || screen == 17 ||
					screen == 18 || screen == 19)
			{
				offset += yPos;
			}
		}
		else
		{
			offset = xPos;
		}
		
		return (screen << 25) + (bossDamage << 17) + (myHp << 8) + offset;
	}
}
