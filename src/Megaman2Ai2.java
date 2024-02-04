import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class Megaman2Ai2 implements AiAgent {
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
	
	private static Megaman2Ai2 instance;
	
	private long firstUsableCycle = 30020224;
	private ControllerNeuralNet net;
	private long numControllerRequests = 100000;
	private int layerSize = 18;
	private int numLayers = 3;
	
	private int previousLives = 0;
	private int previousBossHp = 0;
	
	public static void main(String[] args)
	{
		instance = new Megaman2Ai2();
		instance.main();
	}
	
	private void main()
	{
		boolean fileExists = false;
		if (!loadNet())
		{
			 net = new ControllerNeuralNet(true, layerSize, numLayers, true);
			 net.randomInit();
		}
		else
		{
			fileExists = true;
			System.out.println("Successfully load a " + layerSize + "x" + numLayers + " net with up to " + numControllerRequests + " events");
		}
		
		setup();
		load("megaman2.nes", "sav");
		makeModifications();
		net.reset();
		net.setCpuMem(cpuMem);
		run();
		
		while (!done) {}
		
		System.out.println("Score of " + score);
		highScore = score;
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
			load("megaman2.nes", "sav");
			makeModifications();
			net.reset();
			net.setCpuMem(cpuMem);
			run();
			
			while (!done) {}
			
			System.out.println("Score of " + score);
			teardown();
			if (score > highScore)
			{
				highScore = score;
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
				saveNet();
			}
		}
	}
	
	private void saveNet()
	{
		try
		{
			FileWriter file = new FileWriter("megaman2.net");
			PrintWriter out = new PrintWriter(file);
			out.println(net.getParamNumToUpdate());
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
			File file = new File("megaman2.net");
			if (!file.exists())
			{
				return false;
			}
			
			Scanner in = new Scanner(file);
			String line = in.nextLine();
			int paramNumToUpdate = Integer.parseInt(line);
			line = in.nextLine();
			layerSize = Integer.parseInt(line);
			line = in.nextLine();
			numLayers = Integer.parseInt(line);
			line = in.nextLine();
			numControllerRequests = Long.parseLong(line);
			net = new ControllerNeuralNet(true, layerSize, numLayers, false);
			net.setParamNumToUpdate(paramNumToUpdate);
			
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
		previousLives = 0;
		previousBossHp = 0;
		score = 0;
		done = false;
		startedDone = false;
		
		clock = new Clock();
		
		long[] startOnOffTimes = new long[] {10890547, 11309846, 14062612, 14921603, 29152813, 30020223};
		gui = new NetGui(true, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock);
		((NetGui)gui).setRestrictedStart();
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
