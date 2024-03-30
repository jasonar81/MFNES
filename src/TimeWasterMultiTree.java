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

public class TimeWasterMultiTree implements AiAgent {
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
	private volatile long blocks;
	
	private static TimeWasterMultiTree instance;
	
	private long firstUsableCycle = 7140532;
	private MultiDecisionTree tree;
	private MultiTreeController controller;
	private long numControllerRequests = 5000;
	
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
		instance = new TimeWasterMultiTree();
		instance.main();
	}
	
	private void main()
	{
		ArrayList<Integer> validStates = new ArrayList<Integer>();
		validStates.add(0);
		validStates.add(LEFT);
		validStates.add(RIGHT);
		validStates.add(UP);
		validStates.add(DOWN);
		
		if (!loadTree())
		{
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			addresses.add(0x01);
			ArrayList<Integer> disallow = new ArrayList<Integer>();
			disallow.add(0x00);
			
			IfElseNode root = new IfElseNode();
			root.address = 9;
			root.address2 = 35;
			root.comparisonType = 13;
			root.terminal = false;
			
			IfElseNode l = new IfElseNode();
			l.address = 9;
			l.address2 = 4;
			l.comparisonType = 14;
			l.terminal = false;
			
			root.left = l;
			l.parent = root;
			
			IfElseNode ll = new IfElseNode();
			ll.address = 7;
			ll.address2 = 2;
			ll.comparisonType = 14;
			ll.terminal = false;
			
			l.left = ll;
			ll.parent = l;
			
			IfElseNode lll = new IfElseNode();
			lll.terminal = true;
			lll.terminalValue = LEFT;
			
			ll.left = lll;
			lll.parent = ll;
			
			IfElseNode llr = new IfElseNode();
			llr.address = 7;
			llr.address2 = 3;
			llr.comparisonType = 13;
			llr.terminal = false;
			
			ll.right = llr;
			llr.parent = ll;
			
			IfElseNode llrl = new IfElseNode();
			llrl.terminal = true;
			llrl.terminalValue = RIGHT;
			
			llr.left = llrl;
			llrl.parent = llr;
			
			IfElseNode llrr = new IfElseNode();
			llrr.terminal = true;
			llrr.terminalValue = 0;
			
			llr.right = llrr;
			llrr.parent = llr;
			
			IfElseNode lr = elseTree();
			l.right = lr;
			lr.parent = l;
			
			IfElseNode r = elseTree();
			root.right = r;
			r.parent = root;
			
			tree = new MultiDecisionTree(validStates, addresses, root, disallow);
		}
		
		controller = new MultiTreeController(tree);
		tree.setValidStates(validStates);
		setup();
		load("time_waster.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunAllMode();
		tree.reset();
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + finalScore);

		highScore = finalScore;
		System.out.println("New high score!");
		
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests * 3;
			setup();
			load("time_waster.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			run();
			
			while (!done) {}
			
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
			load("time_waster.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunAllMode();
			tree.reset();
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
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
			load("time_waster.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree);
			tree.setRunSceneMode(sceneNum, countWithNoImprovement);
			tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			HashSet<Integer> addressesAndValues = tree.getAddressesAndValues();
	
			teardown();
			
			if (finalScore > highScore && confirm(sceneNum))
			{
				countWithNoImprovement = 0;
				highScore = finalScore;
				System.out.println("New high score with scene num = " + sceneNum);
				tree.persist();
				saveTree();
			} else if (finalScore == highScore)
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
		double minHighScore = finalScore;
		setup();
		load("time_waster.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree);
		tree.setRunSceneMode(sceneNum);
		tree.setRegister4016(((Register4016)cpu.getMem().getLayout()[0x4016]));
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + finalScore);
		
		if (finalScore < minHighScore)
		{
			minHighScore = finalScore;
		}

		teardown();
		
		finalScore = minHighScore;
		return (finalScore > highScore);
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("time_waster_scenes.tree");
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
			File file = new File("time_waster_scenes.tree");
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
		blocks = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {6439097, 7140531};
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
		cpu.getMem().getLayout()[0x00] = new NotifyChangesPort(this, clock); //Lives remaining
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			score = gameScore();
			System.out.println("Game score = " + score);
			finalScore = score;
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
		
		if (cycle >= firstUsableCycle)
		{
			if (cpu.getMem().getLayout()[0x00].read() == 2)
			{
				setDone(cycle);
				return;
			}
		}
		
		cont();
	}
	
	private long gameScore()
	{
		long retval = Byte.toUnsignedLong(cpu.getMem().getLayout()[59].read());
		retval += 256 * Byte.toUnsignedLong(cpu.getMem().getLayout()[60].read());
		return retval;
	}
	
	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
	
	private IfElseNode elseTree()
	{
		IfElseNode root = new IfElseNode();
		root.address = 33;
		root.address2 = 2;
		root.comparisonType = 14;
		root.terminal = false;
		
		IfElseNode l = new IfElseNode();
		l.terminal = true;
		l.terminalValue = LEFT;
		
		root.left = l;
		l.parent = root;
		
		IfElseNode r = new IfElseNode();
		r.address = 33;
		r.address2 = 3;
		r.comparisonType = 13;
		r.terminal = false;
		
		root.right = r;
		r.parent = root;
		
		IfElseNode rl = new IfElseNode();
		rl.terminal = true;
		rl.terminalValue = RIGHT;
		
		r.left = rl;
		rl.parent = r;
		
		IfElseNode rr = new IfElseNode();
		rr.terminal = true;
		rr.terminalValue = 0;
		
		r.right = rr;
		rr.parent = r;
		
		return root;
	}
}
