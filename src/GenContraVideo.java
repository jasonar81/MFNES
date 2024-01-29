//Generates a video from ContraAi2 parameters

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class GenContraVideo implements AiAgent {
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
	
	private long firstUsableCycle = 62407559;
	
	private String dir;
	private String ts;
	
	private static GenContraVideo instance;
	
	public static void main(String[] args)
	{
		instance = new GenContraVideo();
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
		load("contra.nes");
		makeModifications();
		ArrayList<ControllerEvent> eventList = getEventList(dir + "contra" + ts + ".rec");
		gui.setEventList(eventList);
		run();
		
		while (!done) {}
		
		teardown();
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
	
	private void setup()
	{
		done = false;
		startedDone = false;
		
		clock = new Clock();
		gui = new RecordingGui(dir + "contra" + ts + ".mp4");
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
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x34] = new RomMemoryPort((byte)0); //Fix the randomizer value
		//cpu.getMem().getLayout()[0x32] = new RomMemoryPort((byte)63); //Always report back 63 lives remaining
		cpu.getMem().getLayout()[0x3a] = new DoneRamPort((byte)2, this, clock); //When continues decrements to 2, call it a wrap
		cpu.getMem().getLayout()[0x65] = new SaveMaxValuePort(); //Distance into current screen
		cpu.getMem().getLayout()[0x64] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x65], false, true, this, clock); //Screen number in level
		cpu.getMem().getLayout()[0x30] = new SaveMaxValueAndClearElsewherePort(cpu.getMem().getLayout()[0x64], false, true, this, clock); //Level
		cpu.getMem().getLayout()[0xb4] = new DeathPort((byte)1, this, clock); //Detect a death
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			startedDone = true;
			done = true;
		}
	}
	
	public synchronized void setDeath(long cycle)
	{
		pause();
		cont();
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
}
