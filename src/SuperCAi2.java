import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class SuperCAi2 implements AiAgent {
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
	private volatile long livesLost;
	private volatile long totalTime;
	
	private static SuperCAi2 instance;
	
	private long firstUsableCycle = 56477303;
	private ControllerNeuralNet net;
	private long numControllerRequests = 200000;
	private int layerSize = 8;
	private int numLayers = 1;
	
	public static void main(String[] args)
	{
		instance = new SuperCAi2();
		instance.main();
	}
	
	private void main()
	{
		boolean fileExists = false;
		if (!loadNet())
		{
			 net = new ControllerNeuralNet(false, layerSize, numLayers, true);
		}
		else
		{
			fileExists = true;
			System.out.println("Successfully load a " + layerSize + "x" + numLayers + " net with up to " + numControllerRequests + " events");
		}
		
		setup();
		load("super_c.nes", "sav");
		makeModifications();
		net.reset();
		net.setCpuMem(cpuMem);
		run();
		
		while (!done) {}
		
		printResults();
		System.out.println("Score of " + finalScore);

		highScore = finalScore;
		System.out.println("New high score!");
		
		teardown();
		
		if (!fileExists)
		{
			System.out.println("File did not exist, so saving newly created network");
			saveNet();
		}
		
		while (true)
		{
			net.updateParameters();
			setup();
			load("super_c.nes", "sav");
			makeModifications();
			net.reset();
			net.setCpuMem(cpuMem);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
	
			teardown();
			if (finalScore > highScore)
			{
				highScore = finalScore;
				System.out.println("New high score!");
				saveNet();
				if (numControllerRequests < 300000000)
				{
					numControllerRequests *= 2;
				}
			}
			else
			{
				net.revertParameters();
			}
		}
	}
	
	private void saveNet()
	{
		try
		{
			FileWriter file = new FileWriter("superc.net");
			PrintWriter out = new PrintWriter(file);
			out.println(layerSize);
			out.println(numLayers);
			out.println(numControllerRequests);
			
			int numParameters = net.numParameters();
			for (int i = 0; i < numParameters; ++i)
			{
				out.println(net.getParameter(i));
			}
			
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean loadNet()
	{
		try
		{
			File file = new File("superc.net");
			if (!file.exists())
			{
				return false;
			}
			
			Scanner in = new Scanner(file);
			String line = in.nextLine();
			layerSize = Integer.parseInt(line);
			line = in.nextLine();
			numLayers = Integer.parseInt(line);
			line = in.nextLine();
			numControllerRequests = Long.parseLong(line);
			net = new ControllerNeuralNet(false, layerSize, numLayers, false);
			
			int paramNum = 0;
			while (in.hasNextLine() && net.hasMoreSetup())
			{
				line = in.nextLine();
				int val = Integer.parseInt(line);
				net.setParameter(val, paramNum++);
			}
			
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private void setup()
	{
		livesLost = 0;
		score = 0;
		totalTime = 0;
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {9669252, 10395586, 12313900, 13330955};
		clock = new Clock();
		gui = new NetGui(false, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock);
		guiThread = new Thread(gui);
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
	
	private void printResults()
	{
		System.out.println("Game score = " + gameScore());
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
		cpu.getMem().getLayout()[0x53] = new NotifyChangesPort(this, clock); //Lives remaining
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			this.totalTime = totalTime;
			++livesLost;
			score = gameScore();
			finalScore = (score * 1.0) / livesLost;
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
			if (cpu.getMem().getLayout()[0x53].read() == 0)
			{
				setDone(cycle);
			}
			
			++livesLost;
			System.out.println("Died");
		}
		
		cont();
	}
	
	private long gameScore()
	{
		long retval = 0;
		long val = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x07e3].read());
		retval += val;
		val = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x07e4].read());
		retval += val * 100;
		val = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x07e5].read());
		retval += val * 10000;
		val = Byte.toUnsignedLong(cpu.getMem().getLayout()[0x50].read());
		retval += val * 1000000;
		
		return retval;
	}

	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
}
