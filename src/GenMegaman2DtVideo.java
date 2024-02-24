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

public class GenMegaman2DtVideo implements AiAgent {
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
	private volatile boolean done;
	private volatile boolean startedDone;
	private volatile long score;
	
	private static GenMegaman2DtVideo instance;
	
	private long firstUsableCycle = 41020224;
	private NewMutatingDecisionTree tree;
	private DecisionTreeController controller;
	private static long numControllerRequests;
	
	private int previousLives = 0;
	private int previousBossHp = 0;
	
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
		numControllerRequests = Integer.parseInt(args[2]);
		
		if (!dir.endsWith("/"))
		{
			dir += "/";
		}
		
		instance = new GenMegaman2DtVideo();
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
			tree = new NewMutatingDecisionTree(validStates);
			controller = new DecisionTreeController(tree.getRoot());
		}
		
		tree.setValidStates(validStates);
		setup();
		load("megaman2.nes", "sav");
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
			File file = new File(dir + "megaman2" + ts + ".tree");
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
		previousLives = 0;
		previousBossHp = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		
		long[] startOnOffTimes = new long[] {10890547, 11309846, 14062612, 14921603, 29152813, 30020223, 40020223, 41020223};
		gui = new RecordingDecisionTreeGui(numControllerRequests, firstUsableCycle, controller, startOnOffTimes, clock, dir + "megaman2_dt" + ts + ".mp4");
		long[] rightTimes = new long[] {37020223, 38020223};
		((DecisionTreeGui)gui).setRightTimes(rightTimes);
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
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0xa8] = new NotifyChangesPort(this, clock); //Detect a death
		cpu.getMem().getLayout()[0x6c1] = new NotifyChangesPort(this, clock); //Boss HP
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableTracking(firstUsableCycle);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			startedDone = true;
			score += getScoreAtDeath();
			done = true;
		}
	}
	
	public synchronized void setDeath(long cycle)
	{

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
		
		int bossHp = cpu.getMem().getLayout()[0x6c1].read();
		int lives = cpu.getMem().getLayout()[0xa8].read();
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
			if (lives == 0)
			{
				setDone(cycle);
				return;
			}
			
			score += getScoreAtDeath();
		}
		
		previousBossHp = bossHp;
		previousLives = lives;
		
		cont();
	}
	
	private long getScoreAtDeath()
	{
		long screen = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x20].read());
		long offset = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x1f].read());
		long bossHp = cpu.getMem().getLayout()[0x6c1].read();
		long bossDamage = 0;
		if (bossHp != 0)
		{
			bossDamage = 0x7f - bossHp;
		}
		
		if (bossDamage < 0)
		{
			bossDamage = 0;
		}
		
		return (screen << 16) + (bossDamage << 8) + offset;
	}
}
