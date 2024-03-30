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

public class TimeWasterDecisionTree implements AiAgent {
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
	private volatile double highScore2 = 0;
	private volatile double finalScore;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile long blocks;
	
	private static TimeWasterDecisionTree instance;
	
	private long firstUsableCycle = 7140532;
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private long numControllerRequests = 5000;
	private NewMutatingDecisionTree tree2;
	private DecisionTreeController controller2;
	private long numControllerRequests2 = 5000;
	private NewMutatingDecisionTree tree3;
	private DecisionTreeController controller3;
	
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
		instance = new TimeWasterDecisionTree();
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
			tree = new NewMutatingDecisionTree(validStates);
			
			/*
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
			
			tree.setRoot(root);
			tree.reindexTree();
			*/
			
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("time_waster.nes", "sav");
		makeModifications();
		controller.reset();
		controller.setCpuMem(cpuMem);
		controller.setTree(tree.getRoot());
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + finalScore);

		highScore = finalScore;
		System.out.println("New high score!");
		
		HashSet<Integer> addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
		HashSet<Integer> previous;
		teardown();
		
		while (true)
		{
			numControllerRequests = usedControllerRequests * 3;
			setup();
			load("time_waster.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (finalScore > highScore && confirm(1))
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
		
		if (!loadTree2())
		{
			tree2 = new NewMutatingDecisionTree(validStates);
			controller2 = new DecisionTreeController(tree2.getRoot());
		}
		
		tree2.setValidStates(validStates);
		setup2();
		load("time_waster.nes", "sav");
		makeModifications();
		controller2.reset();
		controller2.setCpuMem(cpuMem);
		controller2.setTree(tree2.getRoot());
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + finalScore);

		highScore2 = finalScore;
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
			load("time_waster.nes", "sav");
			makeModifications();
			controller2.reset();
			controller2.setCpuMem(cpuMem);
			controller2.setTree(tree2.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			addressesAndValues2 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (finalScore > highScore2 && confirm(2))
			{
				highScore2 = finalScore;
				
				if (finalScore > highScore)
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
		
		tree3 = new NewMutatingDecisionTree(validStates);
		controller3 = new DecisionTreeController(tree2.getRoot());
		
		while (true)
		{
			tree.mutate(addressesAndValues);
			previous = addressesAndValues;
			setup();
			load("time_waster.nes", "sav");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			previous = addressesAndValues;
			addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (finalScore > highScore && confirm(1))
			{
				highScore = finalScore;
				System.out.println("New high score!");
				saveTree();
				numControllerRequests = usedControllerRequests * 3;
				
				tree.persist();
			}
			else if (finalScore == highScore)
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
			load("time_waster.nes", "sav");
			makeModifications();
			controller2.reset();
			controller2.setCpuMem(cpuMem);
			controller2.setTree(tree2.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			previous2 = addressesAndValues2;
			addressesAndValues2 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
			teardown();
			if (finalScore > highScore2 && confirm(2))
			{
				if (finalScore > highScore)
				{
					System.out.println("New high score!");
					highScore2 = highScore;
					highScore = finalScore;
					HashSet<Integer> aav = addressesAndValues;
					addressesAndValues = addressesAndValues2;
					addressesAndValues2 = aav;
					tree3.setRoot(tree2.getRoot().clone());
					tree2.setRoot(tree.getRoot().clone());
					tree.setRoot(tree3.getRoot().clone());
					tree3.resetRoot();
					tree.reindexTree();
					tree2.reindexTree();
					saveTree();
					saveTree2();
					long temp = numControllerRequests;
					numControllerRequests = usedControllerRequests * 3;
					numControllerRequests2 = temp;
				}
				else
				{
					highScore2 = finalScore;
					saveTree2();
					numControllerRequests2 = usedControllerRequests * 3;
				
					tree2.persist();
				}
			}
			else if (finalScore == highScore2)
			{
				if (finalScore > highScore)
				{
					highScore2 = highScore;
					highScore = finalScore;
					HashSet<Integer> aav = addressesAndValues;
					addressesAndValues = addressesAndValues2;
					addressesAndValues2 = aav;
					tree3.setRoot(tree2.getRoot().clone());
					tree2.setRoot(tree.getRoot().clone());
					tree.setRoot(tree3.getRoot().clone());
					tree3.resetRoot();
					tree.reindexTree();
					tree2.reindexTree();
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
			
			tree3.setRoot(tree.merge(tree2, addressesAndValues, addressesAndValues2));
			tree3.reindexTree();
			setup3();
			load("time_waster.nes", "sav");
			makeModifications();
			controller3.reset();
			controller3.setCpuMem(cpuMem);
			controller3.setTree(tree3.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
	
			HashSet<Integer> addressesAndValues3 = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
			teardown();
			if (finalScore > highScore2 && finalScore > highScore && confirm(3))
			{
				highScore = finalScore;
				highScore2 = -1;
				addressesAndValues = addressesAndValues3;
				System.out.println("New high score!");
				tree.setRoot(tree3.getRoot().clone());
				tree2.resetRoot();
				tree3.resetRoot();
				tree.reindexTree();
				tree2.reindexTree();
				saveTree();
				saveTree2();
				numControllerRequests2 = 5000;
				numControllerRequests = usedControllerRequests * 3;
			}
		}
	}
	
	private boolean confirm(int num)
	{
		int NUM_CONFIRMS = 1;
		double minFinalScore = finalScore;
		for (int i = 0; i < NUM_CONFIRMS; ++i)
		{
			if (num == 1)
			{
				setup();
				load("time_waster.nes", "sav");
				makeModifications();
				controller.reset();
				controller.setCpuMem(cpuMem);
				controller.setTree(tree.getRoot());
				run();
				
				while (!done) {}
				
				System.out.println("Score of " + finalScore);
		
				teardown();
				if (!(finalScore > highScore))
				{
					return false;
				}
			} else if (num == 2)
			{
				setup2();
				load("time_waster.nes", "sav");
				makeModifications();
				controller2.reset();
				controller2.setCpuMem(cpuMem);
				controller2.setTree(tree2.getRoot());
				run();
				
				while (!done) {}
				
				System.out.println("Score of " + finalScore);
		
				teardown();
				if (!(finalScore > highScore2))
				{
					return false;
				}
			}
			else
			{
				setup3();
				load("time_waster.nes", "sav");
				makeModifications();
				controller3.reset();
				controller3.setCpuMem(cpuMem);
				controller3.setTree(tree3.getRoot());
				run();
				
				while (!done) {}
				
				System.out.println("Score of " + finalScore);
		
				teardown();
				if (!(finalScore > highScore && finalScore > highScore2))
				{
					return false;
				}
			}
			
			if (finalScore < minFinalScore)
			{
				minFinalScore = finalScore;
			}
		}
		
		finalScore = minFinalScore;
		return true;
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("time_waster.tree");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree = (NewMutatingDecisionTree)i.readObject();
			controller = new DecisionTreeController(tree.getRoot());
	
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
			File file = new File("time_waster.tree");
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
			File file = new File("time_waster.tree2");
			if (!file.exists())
			{
				return false;
			}
			
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			tree2 = (NewMutatingDecisionTree)i.readObject();
			controller2 = new DecisionTreeController(tree2.getRoot());
	
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
			File file = new File("time_waster.tree2");
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
		blocks = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {6439097, 7140531};
		clock = new Clock();
		gui = new DecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock);
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
	
	private void setup2()
	{
		blocks = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {6439097, 7140531};
		clock = new Clock();
		gui = new DecisionTreeGui(numControllerRequests2, firstUsableCycle, controller2, startOnOffTimes, clock);
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
	
	private void setup3()
	{
		blocks = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {6439097, 7140531};
		clock = new Clock();
		gui = new DecisionTreeGui(Math.max(numControllerRequests, numControllerRequests2), firstUsableCycle, controller3, startOnOffTimes, clock);
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
			usedControllerRequests = ((DecisionTreeGui)gui).getRequests();
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
			if (cpu.getMem().getLayout()[0x00].read() == 0)
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
