import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class GenPunchOutVideo implements AiAgent {
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
	private volatile boolean justFinishedLevel = false;
	private volatile int currentLevel = 0;
	
	private static GenPunchOutVideo instance;
	
	private long firstUsableCycle = 62856095;
	
	private String dir;
	private String ts;
	
	public static void main(String[] args)
	{
		instance = new GenPunchOutVideo();
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
		setup();
		load("punch_out.nes", "sav");
		makeModifications();
		ArrayList<ControllerEvent> eventList = getEventList(dir + "punch_out" + ts + ".rec");
		gui.setEventList(eventList);
		run();
		
		while (!done) {}
		
		teardown();
	}
	
	private void setup()
	{
		done = false;
		startedDone = false;
		currentLevel = 0;
		
		clock = new Clock();
		gui = new RecordingGui(dir + "punch_out_" + ts + ".mp4");
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
	
	private ArrayList<ControllerEvent> getEventList(String filename)
	{
		ArrayList<ControllerEvent> queue = new ArrayList<ControllerEvent>();
		try
		{
			File file = new File(filename);
			Scanner in = new Scanner(file);
			
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				StringTokenizer tokens = new StringTokenizer(line, ",", false);
				long cycle = Long.parseLong(tokens.nextToken());
				int type = Integer.parseInt(tokens.nextToken());
				boolean down = (type == 1);
				int code = Integer.parseInt(tokens.nextToken());
				queue.add(new ControllerEvent(cycle, down, code));
			}
			
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return queue;
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

	private int enemyDamage()
	{
		return ((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).getSum();
	}
	
	private int myDamage()
	{
		return ((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).getSum();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x01] = new NotifyChangesPort(this, clock);
		cpu.getMem().getLayout()[0x391] = new TrackSumOfSubtractionsPort(this, clock, true);
		cpu.getMem().getLayout()[0x398] = new TrackSumOfSubtractionsPort(this, clock, false);
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			startedDone = true;
			cont();
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
		
		//We got pinged because of a level change or damage to us or our opponent
		//We could be knocked out
		
		if (cpu.getMem().getLayout()[0x01].read() > currentLevel)
		{
			//new level 
			++currentLevel;
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
			((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			justFinishedLevel = true;
		}
		else if (cpu.getMem().getLayout()[0x391].read() == 0)
		{
			if (!justFinishedLevel)
			{
				//Knocked out
				setDone(cycle);
			}
			else
			{
				justFinishedLevel = false;
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x391]).reset();
				((TrackSumOfSubtractionsPort)cpu.getMem().getLayout()[0x398]).reset();
			}
		}
		
		cont();
	}

	@Override
	public void setDeath(long cycle) {
		
	}
}
