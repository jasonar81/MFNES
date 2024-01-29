import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class GenSmb2Video implements AiAgent {
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
	private volatile boolean done;
	private volatile boolean startedDone;
	
	private static GenSmb2Video instance;
	
	private long firstUsableCycle = 25377919; 
	
	private ControllerNeuralNet net;
	private long numControllerRequests;
	private int layerSize;
	private int numLayers;
	
	private String dir;
	private String ts;
	
	public static void main(String[] args)
	{
		instance = new GenSmb2Video();
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
		
		//Run
		loadNet(dir + "smb" + ts + ".net");
		setup();
		load("smb.nes");
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
		done = false;
		startedDone = false;
		
		long[] startOnOffTimes = new long[] {16103188, 16979809, 24542115, 25377918};
		clock = new Clock();
		gui = new RecordingNetGui(false, numControllerRequests, firstUsableCycle, net, startOnOffTimes, clock, dir + "smb_memory" + ts + ".mp4");
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
		apuThread = new Thread(apu);
		apuThread.setPriority(10);
		cpu.debugHold(true);
		ppu.debugHold(true);
		ppuThread.start();
		apuThread.start();
		cpuThread.start();
	}
	
	private long getLevel()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x760].read());
	}
	
	private long getScreenInLevel()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x71a].read());
	}
	
	private long getDistanceIntoScreen()
	{
		return Byte.toUnsignedLong(cpu.getMem().getLayout()[0x71d].read());
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x0e] = new DoneRamPort((byte)6, this, clock); //Call it a wrap when there's a death
		cpu.getMem().getLayout()[0x7fc] = new NonZeroDoneRamPort(this, clock); //I think this increments with game completions
		cpu.getMem().getLayout()[0x71a] = new NotifyChangesPort(this, clock); //call progress when we get to a new screen
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
		cont();
	}
	
	private long getTimer()
	{
		MemoryPort[] layout = cpu.getMem().getLayout();
		return Byte.toUnsignedLong(layout[0x7fa].read()) + Byte.toUnsignedLong(layout[0x7f9].read()) * 10 + Byte.toUnsignedLong(layout[0x7f8].read()) * 100;
	}
}
