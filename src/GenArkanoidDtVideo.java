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

public class GenArkanoidDtVideo implements AiAgent {
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
	
	private static GenArkanoidDtVideo instance;
	
	private long firstUsableCycle = 12109347;
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private long numControllerRequests = 6000000;
	
	private static int A = 0x80;
	private static int B = 0x40;
	private static int UP = 0x20;
	private static int DOWN = 0x10;
	private static int LEFT = 0x08;
	private static int RIGHT = 0x04;
	private static int SELECT = 0x02;
	private static int START = 0x01;
	
	private static String dir;
	private static String ts;
	
	public static void main(String[] args)
	{
		dir = args[0];
		ts = args[1];
		
		if (!dir.endsWith("/"))
		{
			dir += "/";
		}
		
		instance = new GenArkanoidDtVideo();
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
		
		teardown();
	}
	
	private boolean loadTree()
	{
		try
		{
			File file = new File(dir + "arkanoid" + ts + ".tree");
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
	
	private void setup()
	{
		blocks = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {11333218, 12109346};
		clock = new Clock();
		gui = new RecordingDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock, dir + "arkanoid_dt" + ts + ".mp4");
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
			System.out.println("Blocks = " + blocks);
			finalScore = (blocks << 24) + score;
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
