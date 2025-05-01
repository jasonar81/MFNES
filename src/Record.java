import java.util.Scanner;

public class Record {
	private static Clock clock;
	private static CPU cpu;
	private static PPU ppu;
	private static APU apu;
	private static Memory ppuMem;
	private static Memory cpuMem;
	private static Thread cpuThread;
	private static Thread ppuThread;
	private static Thread apuThread;
	private static GUI gui;
	private static Thread guiThread;
	private static boolean on = false;
	
	public static void main(String[] args)
	{
		initialSetup(args[2]);
		load(args[0]);
		run();
		
		try
		{
			Thread.sleep(Integer.parseInt(args[1]) * 1000);
		}
		catch(Exception e) {}
		off();
	}
	
	private static void load(String filename)
	{
		off();
		Cartridge cart = Cartridge.loadCart(filename);
		
		if (cart != null)
		{
			cpu.setupCart(cart);
			ppu.setupCart(cart);
		}
	}
	
	private static void off()
	{
		if (on)
		{
			on = false;
			
			cpu.terminate();
			
			gui.terminate();
			
			try
			{
				Thread.sleep(60000);
			}
			catch(Exception e) {}
		}
	}
	
	private static void run()
	{
		on();
		cpu.debugHold(false);
		
	}
	
	private static void on()
	{
		if (!on)
		{
			on = true;
			
			
			cpuThread = new Thread(cpu);
			cpuThread.setPriority(10);
			
			
			cpu.debugHold(true);
			
			
			
			cpuThread.start();
		}
	}
	
	private static void initialSetup(String filename)
	{
		gui = new AVRecordGui(filename);
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		
		clock = new Clock();
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
}
