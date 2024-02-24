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

public class ArkanoidDecisionTree implements AiAgent {
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
	private volatile double highScore = 0;
	private volatile double finalScore;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile long blocks;
	
	private static ArkanoidDecisionTree instance;
	
	private long firstUsableCycle = 12109347;
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private long numControllerRequests = 5000;
	
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
		instance = new ArkanoidDecisionTree();
		instance.main();
	}
	
	private void main()
	{
		ArrayList<Integer> validStates = new ArrayList<Integer>();
		validStates.add(0);
		validStates.add(LEFT);
		validStates.add(RIGHT);
		validStates.add(A);
		
		if (!loadTree())
		{	
			tree = new NewMutatingDecisionTree(validStates);
			/*
			IfElseNode root = tree.getRoot();
			root.terminal = false;
			root.address = 0x11a;
			root.address2 = 0x38;
			root.comparisonType = 7;
			root.checkValue = 0;
			
			IfElseNode left = new IfElseNode();
			left.terminal = true;
			left.terminalValue = 0x08;
			root.left = left;
			left.parent = root;
			
			IfElseNode right = new IfElseNode();
			right.terminal = false;
			right.address = 0x38;
			right.address2 = 0x11f;
			right.comparisonType = 7;
			right.checkValue = 0;
			root.right = right;
			right.parent = root;
			
			IfElseNode rightLeft = new IfElseNode();
			rightLeft.terminal = true;
			rightLeft.terminalValue = 0x04;
			right.left = rightLeft;
			rightLeft.parent = right;
			
			IfElseNode rightRight = new IfElseNode();
			rightRight.terminal = true;
			rightRight.terminalValue = 0;
			right.right = rightRight;
			rightRight.parent = right;
			
			tree.reindexTree();
			*/
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("arkanoid.nes");
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
			numControllerRequests *= 3;
			setup();
			load("arkanoid.nes");
			makeModifications();
			controller.reset();
			controller.setCpuMem(cpuMem);
			controller.setTree(tree.getRoot());
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + finalScore);
			
			addressesAndValues = ((Register4016)cpu.getMem().getLayout()[0x4016]).getTracking();
	
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
		
		while (true)
		{
			tree.mutate(addressesAndValues);
			previous = addressesAndValues;
			setup();
			load("arkanoid.nes");
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
			if (finalScore > highScore)
			{
				highScore = finalScore;
				System.out.println("New high score!");
				saveTree();
				if (numControllerRequests < 300000000)
				{
					numControllerRequests *= 2;
				}
				
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
		}
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File("arkanoid.tree");
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
			File file = new File("arkanoid.tree");
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
		
		long[] startOnOffTimes = new long[] {11333218, 12109346};
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
		apuThread = new Thread (apu);
		apuThread.setPriority(10);
		cpu.debugHold(true);
		ppu.debugHold(true);
		ppuThread.start();
		apuThread.start();
		cpuThread.start();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x0d] = new NotifyChangesPort(this, clock); //Lives remaining
		cpu.getMem().getLayout()[0x145] = new NotifyChangesPort(this, clock);
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
		}
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
		pause();
		
		if (cycle >= firstUsableCycle)
		{
			if (cpu.getMem().getLayout()[0x145].read() == 1)
			{
				++blocks;
			}
			
			if (cpu.getMem().getLayout()[0x0d].read() == 0)
			{
				setDone(cycle);
				return;
			}
		}
		
		cont();
	}
	
	private long gameScore()
	{
		long retval = 0;
		retval += cpu.getMem().getLayout()[0x0375].read();
		retval += cpu.getMem().getLayout()[0x0374].read() * 10;
		retval += cpu.getMem().getLayout()[0x0373].read() * 100;
		retval += cpu.getMem().getLayout()[0x0372].read() * 1000;
		retval += cpu.getMem().getLayout()[0x0371].read() * 10000;
		retval += cpu.getMem().getLayout()[0x0370].read() * 100000;
		return retval;
	}
	
	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
}
