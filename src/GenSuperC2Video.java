import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class GenSuperC2Video implements AiAgent {
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
	private volatile boolean done = false;
	private volatile boolean startedDone;
	
	private static GenSuperC2Video instance;
	
	private long firstUsableCycle = 56477303;
	
	private ControllerNeuralNet net;
	private long numControllerRequests;
	private int layerSize;
	private int numLayers;
	
	private String dir;
	private String ts;
	
	public static void main(String[] args)
	{
		instance = new GenSuperC2Video();
		String dir = args[0];
		String ts = args[1];
		instance.main(dir, ts);
	}
	
	public void main(String dir, String ts)
	{	
		if (!dir.endsWith("/"))
		{
			dir = (dir + "/");
		}
		
		this.dir = dir;
		this.ts = ts;

		loadNet(dir + "superc" + ts + ".net");
		setup();
		load("super_c.nes", "sav");
		makeModifications();
		net.setCpuMem(cpuMem);
		run();
		
		while (!done) {}
	
		teardown();
	}
	
	private boolean loadNet(String filename)
	{
		try
		{
			File file = new File(filename);
			
			Scanner in = new Scanner(file);
			String line = in.nextLine();
			int paramNumToUpdate = Integer.parseInt(line);
			line = in.nextLine();
			layerSize = Integer.parseInt(line);
			line = in.nextLine();
			numLayers = Integer.parseInt(line);
			line = in.nextLine();
			numControllerRequests = Long.parseLong(line);
			net = new ControllerNeuralNet(false, layerSize, numLayers, false);
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
		done = false;
		startedDone = false;
		long[] startOnOffTimes = new long[] {9669252, 10395586, 12313900, 13330955};
		clock = new Clock();
		gui = new RecordingNetGui(false, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock, dir + "superc_memory" + ts + ".mp4");
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
			startedDone = true;
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
		if (cycle > firstUsableCycle)
		{
			if (cpu.getMem().getLayout()[0x53].read() == 0)
			{
				setDone(cycle);
				return;
			}
		}
		cont();
	}

	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
}
