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
		else if (command.startsWith("genie "))
		{
			genie(command.substring(6).trim());
		}
		else if (command.startsWith("train ")) {
			train(command.substring(6));
		}
		else if (command.startsWith("search 0x"))
		{
			search(command.substring(9));
		}
		else
		{
			synchronized(System.out) 
			{
				System.out.println("Unknown command");
			}
		}
	}
	
	private static void genie(String code)
	{
		if (code.length() == 6)
		{
			int[] bytes = decode6(code);
			if (bytes.length == 6)
			{
				int address = 0x8000 + 
		              ((bytes[3] & 7) << 12)
		              | ((bytes[5] & 7) << 8) | ((bytes[4] & 8) << 8)
		              | ((bytes[2] & 7) << 4) | ((bytes[1] & 8) << 4)
		              |  (bytes[4] & 7)       |  (bytes[3] & 8);
				int data =
		             ((bytes[1] & 7) << 4) | ((bytes[0] & 8) << 4)
		             | (bytes[0] & 7)       |  (bytes[5] & 8);
				cpu.getMem().addGenie(address, true, (byte)0, (byte)data);
			}
			else
			{
				System.out.println("Invalid code");
			}
		} else if (code.length() == 7)
		{
			if (code.startsWith("F") || code.startsWith("f"))
			{
				int[] bytes = decode7(code);
				if (bytes.length == 6)
				{
					int address = (bytes[0] << 12) + (bytes[1] << 8) + (bytes[2] << 4) + bytes[3];
					int data = (bytes[4] << 4) + bytes[5];
					cpu.getMem().addGenie(address, true, (byte)0, (byte)data);
				}
				else
				{
					System.out.println("Invalid code");
				}
			}
			else
			{
				System.out.println("Invalid code");
			}
		}
		else if (code.length() == 8)
		{
			int[] bytes = decode8(code);
			if (bytes.length == 8)
			{
				int address = 0x8000 + 
		              ((bytes[3] & 7) << 12)
		              | ((bytes[5] & 7) << 8) | ((bytes[4] & 8) << 8)
		              | ((bytes[2] & 7) << 4) | ((bytes[1] & 8) << 4)
		              |  (bytes[4] & 7)       |  (bytes[3] & 8);
				int data =
		             ((bytes[1] & 7) << 4) | ((bytes[0] & 8) << 4)
		            | (bytes[0] & 7)       |  (bytes[7] & 8);
				int compare =
		             ((bytes[7] & 7) << 4) | ((bytes[6] & 8) << 4)
		             | (bytes[6] & 7)       |  (bytes[5] & 8);
				cpu.getMem().addGenie(address, false, (byte)compare, (byte)data);
			}
			else
			{
				System.out.println("Invalid code");
			}
		} else if (code.length() == 9)
		{
			if (code.startsWith("F") || code.startsWith("f"))
			{
				int[] bytes = decode9(code);
				if (bytes.length == 8)
				{
					int address = (bytes[0] << 12) + (bytes[1] << 8) + (bytes[2] << 4) + bytes[3];
					int data = (bytes[4] << 4) + bytes[5];
					int compare = (bytes[6] << 4) + bytes[7];
					cpu.getMem().addGenie(address, false, (byte)compare, (byte)data);
				}
				else
				{
					System.out.println("Invalid code");
				}
			}
			else
			{
				System.out.println("Invalid code");
			}
		} 
		else
		{
			System.out.println("Invalid code");
		}
	}
	
	private static int[] decode7(String code)
	{
		if (code.length() != 7)
		{
			return new int[0];
		}
		
		int[] retval = new int[6];
		for (int i = 1; i < 7; ++i)
		{
			if (code.charAt(i) == '0')
			{
				retval[i-1] = 0;
			} else if (code.charAt(i) == '1')
			{
				retval[i-1] = 1;
			} else if (code.charAt(i) == '2')
			{
				retval[i-1] = 2;
			} else if (code.charAt(i) == '3')
			{
				retval[i-1] = 3;
			} else if (code.charAt(i) == '4')
			{
				retval[i-1] = 4;
			} else if (code.charAt(i) == '5')
			{
				retval[i-1] = 5;
			} else if (code.charAt(i) == '6')
			{
				retval[i-1] = 6;
			} else if (code.charAt(i) == '7')
			{
				retval[i-1] = 7;
			} else if (code.charAt(i) == '8')
			{
				retval[i-1] = 8;
			} else if (code.charAt(i) == '9')
			{
				retval[i-1] = 9;
			} else if (code.charAt(i) == 'a' || code.charAt(i) == 'A')
			{
				retval[i-1] = 10;
			} else if (code.charAt(i) == 'b' || code.charAt(i) == 'B')
			{
				retval[i-1] = 11;
			} else if (code.charAt(i) == 'c' || code.charAt(i) == 'C')
			{
				retval[i-1] = 12;
			} else if (code.charAt(i) == 'd' || code.charAt(i) == 'D')
			{
				retval[i-1] = 13;
			} else if (code.charAt(i) == 'e' || code.charAt(i) == 'E')
			{
				retval[i-1] = 14;
			} else if (code.charAt(i) == 'f' || code.charAt(i) == 'F')
			{
				retval[i-1] = 15;
			} else
			{
				return new int[0];
			}
		}
		
		return retval;
	}
	
	private static int[] decode9(String code)
	{
		if (code.length() != 9)
		{
			return new int[0];
		}
		
		int[] retval = new int[8];
		for (int i = 1; i < 9; ++i)
		{
			if (code.charAt(i) == '0')
			{
				retval[i-1] = 0;
			} else if (code.charAt(i) == '1')
			{
				retval[i-1] = 1;
			} else if (code.charAt(i) == '2')
			{
				retval[i-1] = 2;
			} else if (code.charAt(i) == '3')
			{
				retval[i-1] = 3;
			} else if (code.charAt(i) == '4')
			{
				retval[i-1] = 4;
			} else if (code.charAt(i) == '5')
			{
				retval[i-1] = 5;
			} else if (code.charAt(i) == '6')
			{
				retval[i-1] = 6;
			} else if (code.charAt(i) == '7')
			{
				retval[i-1] = 7;
			} else if (code.charAt(i) == '8')
			{
				retval[i-1] = 8;
			} else if (code.charAt(i) == '9')
			{
				retval[i-1] = 9;
			} else if (code.charAt(i) == 'a' || code.charAt(i) == 'A')
			{
				retval[i-1] = 10;
			} else if (code.charAt(i) == 'b' || code.charAt(i) == 'B')
			{
				retval[i-1] = 11;
			} else if (code.charAt(i) == 'c' || code.charAt(i) == 'C')
			{
				retval[i-1] = 12;
			} else if (code.charAt(i) == 'd' || code.charAt(i) == 'D')
			{
				retval[i-1] = 13;
			} else if (code.charAt(i) == 'e' || code.charAt(i) == 'E')
			{
				retval[i-1] = 14;
			} else if (code.charAt(i) == 'f' || code.charAt(i) == 'F')
			{
				retval[i-1] = 15;
			} else
			{
				return new int[0];
			}
		}
		
		return retval;
	}
	
	private static int[] decode6(String code)
	{
		if (code.length() != 6)
		{
			return new int[0];
		}
		
		int[] retval = new int[6];
		for (int i = 0; i < 6; ++i)
		{
			if (code.charAt(i) == 'a' || code.charAt(i) == 'A')
			{
				retval[i] = 0;
			} else if (code.charAt(i) == 'p' || code.charAt(i) == 'P')
			{
				retval[i] = 1;
			} else if (code.charAt(i) == 'z' || code.charAt(i) == 'Z')
			{
				retval[i] = 2;
			} else if (code.charAt(i) == 'l' || code.charAt(i) == 'L')
			{
				retval[i] = 3;
			} else if (code.charAt(i) == 'g' || code.charAt(i) == 'G')
			{
				retval[i] = 4;
			} else if (code.charAt(i) == 'i' || code.charAt(i) == 'I')
			{
				retval[i] = 5;
			} else if (code.charAt(i) == 't' || code.charAt(i) == 'T')
			{
				retval[i] = 6;
			} else if (code.charAt(i) == 'y' || code.charAt(i) == 'Y')
			{
				retval[i] = 7;
			} else if (code.charAt(i) == 'e' || code.charAt(i) == 'E')
			{
				retval[i] = 8;
			} else if (code.charAt(i) == 'o' || code.charAt(i) == 'O')
			{
				retval[i] = 9;
			} else if (code.charAt(i) == 'x' || code.charAt(i) == 'X')
			{
				retval[i] = 10;
			} else if (code.charAt(i) == 'u' || code.charAt(i) == 'U')
			{
				retval[i] = 11;
			} else if (code.charAt(i) == 'k' || code.charAt(i) == 'K')
			{
				retval[i] = 12;
			} else if (code.charAt(i) == 's' || code.charAt(i) == 'S')
			{
				retval[i] = 13;
			} else if (code.charAt(i) == 'v' || code.charAt(i) == 'V')
			{
				retval[i] = 14;
			} else if (code.charAt(i) == 'n' || code.charAt(i) == 'N')
			{
				retval[i] = 15;
			} else
			{
				return new int[0];
			}
		}
		
		return retval;
	}
	
	private static int[] decode8(String code)
	{
		if (code.length() != 8)
		{
			return new int[0];
		}
		
		int[] retval = new int[8];
		for (int i = 0; i < 8; ++i)
		{
			if (code.charAt(i) == 'a' || code.charAt(i) == 'A')
			{
				retval[i] = 0;
			} else if (code.charAt(i) == 'p' || code.charAt(i) == 'P')
			{
				retval[i] = 1;
			} else if (code.charAt(i) == 'z' || code.charAt(i) == 'Z')
			{
				retval[i] = 2;
			} else if (code.charAt(i) == 'l' || code.charAt(i) == 'L')
			{
				retval[i] = 3;
			} else if (code.charAt(i) == 'g' || code.charAt(i) == 'G')
			{
				retval[i] = 4;
			} else if (code.charAt(i) == 'i' || code.charAt(i) == 'I')
			{
				retval[i] = 5;
			} else if (code.charAt(i) == 't' || code.charAt(i) == 'T')
			{
				retval[i] = 6;
			} else if (code.charAt(i) == 'y' || code.charAt(i) == 'Y')
			{
				retval[i] = 7;
			} else if (code.charAt(i) == 'e' || code.charAt(i) == 'E')
			{
				retval[i] = 8;
			} else if (code.charAt(i) == 'o' || code.charAt(i) == 'O')
			{
				retval[i] = 9;
			} else if (code.charAt(i) == 'x' || code.charAt(i) == 'X')
			{
				retval[i] = 10;
			} else if (code.charAt(i) == 'u' || code.charAt(i) == 'U')
			{
				retval[i] = 11;
			} else if (code.charAt(i) == 'k' || code.charAt(i) == 'K')
			{
				retval[i] = 12;
			} else if (code.charAt(i) == 's' || code.charAt(i) == 'S')
			{
				retval[i] = 13;
			} else if (code.charAt(i) == 'v' || code.charAt(i) == 'V')
			{
				retval[i] = 14;
			} else if (code.charAt(i) == 'n' || code.charAt(i) == 'N')
			{
				retval[i] = 15;
			} else
			{
				return new int[0];
			}
		}
		
		return retval;
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
	
	private static void train(String filename)
	{
		((Register4016)cpu.getMem().getLayout()[0x4016]).enableLogging(filename);
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
		byte value = cpu.getMem().getLayout()[addr].read();
		System.out.println("Value is " + String.format("0x%02X", value));
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
	
	private static void search(String value)
	{
		int val = Integer.parseInt(value, 16);
		byte v = (byte)val;
		
		for (int i = 0; i < 0x800; ++i)
		{
			if (cpu.getMem().getLayout()[i].read() == v)
			{
				System.out.println("Match at " + String.format("0x%04X", i));
			}
		}
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
			apuThread.start();
			ppuThread.start();
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
