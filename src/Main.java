import java.util.Scanner;

public class Main {
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
	private static String previousCommand = "";
	private static boolean on = false;
	
	public static void main(String[] args)
	{
		initialSetup();
		Scanner in = new Scanner(System.in);
		while (true)
		{
			synchronized(System.out) 
			{
				System.out.print("NES> ");
			}
			
			processCommand(in.nextLine());
		}
	}
	
	private static void processCommand(String command)
	{
		if (command.equals("") && !previousCommand.equals(""))
		{
			command = previousCommand;
		}
		
		previousCommand = command;
		if (command.equalsIgnoreCase("on"))
		{
			on();
		} else if (command.equalsIgnoreCase("run"))
		{
			run();
		} else if (command.equalsIgnoreCase("off"))
		{
			off();
		} else if (command.startsWith("load ")) 
		{
			load(command.substring(5).trim());
		} else if (command.equalsIgnoreCase("pause"))
		{
			pause();
		} else if (command.equalsIgnoreCase("c"))
		{
			cont();
		} else if (command.startsWith("b 0x"))
		{
			b(command.substring(4));
		} else if (command.equalsIgnoreCase("n"))
		{
			next();
		} else if (command.equalsIgnoreCase("print"))
		{
			print();
		} else if (command.startsWith("read 0x"))
		{
			read(command.substring(7));
		} else if (command.startsWith("write 0x"))
		{
			write(command.substring(8));
		}
		else if (command.startsWith("readp 0x"))
		{
			readp(command.substring(8));
		} else if (command.startsWith("writep 0x"))
		{
			writep(command.substring(9));
		}
		else if (command.startsWith("seta 0x"))
		{
			seta(command.substring(7));
		}
		else if (command.startsWith("setx 0x"))
		{
			setx(command.substring(7));
		}
		else if (command.startsWith("sety 0x"))
		{
			sety(command.substring(7));
		}
		else if (command.startsWith("sets 0x"))
		{
			sets(command.substring(7));
		}
		else if (command.startsWith("setp 0x"))
		{
			setp(command.substring(7));
		}
		else if (command.startsWith("setpc 0x"))
		{
			setpc(command.substring(8));
		} else if (command.equalsIgnoreCase("reset"))
		{
			reset();
		} else if (command.equalsIgnoreCase("nmi"))
		{
			nmi();
		} else if (command.equalsIgnoreCase("clear"))
		{
			clear();
		} else if (command.equalsIgnoreCase("quit")) {
			System.exit(0);;
		} else if (command.equalsIgnoreCase("irq")) {
			irq();
		} else if (command.startsWith("record ")) {
			record(command.substring(7));
		} else if (command.equalsIgnoreCase("stop")) {
			stopRecording();
		} else if (command.startsWith("play ")) {
			playRecording(command.substring(5));
		} else if (command.equalsIgnoreCase("log"))
		{
			log();
		}
		else
		{
			synchronized(System.out) 
			{
				System.out.println("Unknown command");
			}
		}
	}
	
	private static void log()
	{
		CPU.LOG = !CPU.LOG;
	}
	
	private static void stopRecording()
	{
		gui.stopRecording();
	}
	
	private static void record(String filename)
	{
		gui.record(filename);
	}
	
	private static void playRecording(String filename)
	{
		gui.playRecording(filename);
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
	
	private static void next()
	{
		cpu.step();
		
		while (cpu.stepInProgress())
		{}
		
		cpu.printInstructionToScreen();
	}
	
	private static void nmi()
	{
		cpu.setNmi();
	}
	
	private static void irq()
	{
		cpu.setIrq();
	}
	
	private static void reset()
	{
		apu.setReset();
		cpu.setReset();
		ppu.setReset();
	}
	
	private static void write(String remainder)
	{
		String address = remainder.substring(0, remainder.indexOf(' '));
		String value = remainder.substring(remainder.indexOf(' ')).trim().substring(2);
		int addr = Integer.parseInt(address, 16);
		int val = Integer.parseInt(value, 16);
		cpu.writeAddress(addr, val);
	}
	
	private static void read(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.readAddress(addr);
	}
	
	private static void writep(String remainder)
	{
		String address = remainder.substring(0, remainder.indexOf(' '));
		String value = remainder.substring(remainder.indexOf(' ')).trim().substring(2);
		int addr = Integer.parseInt(address, 16);
		int val = Integer.parseInt(value, 16);
		ppu.writeAddress(addr, val);
	}
	
	private static void readp(String address)
	{
		int addr = Integer.parseInt(address, 16);
		ppu.readAddress(addr);
	}
	
	private static void print()
	{
		cpu.print();
		ppu.print();
	}
	
	private static void b(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.setBreakAddress(addr);
	}
	
	private static void seta(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.seta(addr);
	}
	
	private static void setx(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.setx(addr);
	}
	
	private static void sety(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.sety(addr);
	}
	
	private static void sets(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.sets(addr);
	}
	
	private static void setp(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.setp(addr);
	}
	
	private static void setpc(String address)
	{
		int addr = Integer.parseInt(address, 16);
		cpu.setpc(addr);
	}
	
	private static void clear()
	{
		cpu.setBreakAddress(0x10000);
	}
	
	private static void cont()
	{
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private static void pause()
	{
		cpu.debugHold(true);
		ppu.debugHold(true);
	}
	
	private static void off()
	{
		if (on)
		{
			on = false;
			apu.terminate();
			cpu.terminate();
			ppu.terminate();
			gui.terminate();
			
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception e) {}
			
			initialSetup();
		}
	}
	
	private static void run()
	{
		on();
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private static void on()
	{
		if (!on)
		{
			on = true;
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
	}
	
	private static void initialSetup()
	{
		gui = new DefaultGUI();
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
