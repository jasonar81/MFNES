//Either the CPU or PPU memory

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class Memory {
	public static int CPU = 0;
	public static int PPU = 1;
	public static int MEMSIZE = 65536;
	
	private MemoryPort[] layout;
	private PPU ppu;
	private int type;
	private Cartridge cart;
	private RandomAccessFile raf;
	private CPU cpu;
	private GUI gui;
	private volatile int control = 0;
	private String filename = "";
	
	private ArrayList<Integer> genieAddresses = new ArrayList<Integer>();
	private ArrayList<Byte> genieReturnValues = new ArrayList<Byte>();
	private ArrayList<Boolean> genieUnconditional = new ArrayList<Boolean>();
	private ArrayList<Byte> genieCompare = new ArrayList<Byte>();
	
	public Memory(int type, PPU ppu, GUI gui)
	{
		this.type = type;
		this.ppu = ppu;
		this.gui = gui;
		layout = new MemoryPort[MEMSIZE];
		for (int i = 0; i < MEMSIZE; ++i)
		{
			layout[i] = new DefaultMemoryPort();
		}
	}
	
	public MemoryPort[] getLayout()
	{
		return layout;
	}
	
	public void writeControl(int val)
	{
		//System.out.println("Control = " + val);
		control = val;
	}
	
	public int getControl()
	{
		return control;
	}
	
	public PPU getPpu()
	{
		return ppu;
	}
	
	public void setCpu(CPU cpu)
	{
		this.cpu = cpu;
	}
	
	public synchronized int read(int address)
	{
		byte temp = layout[address].read();
		
		for (int i = 0; i < genieAddresses.size(); ++i)
		{
			if (address == genieAddresses.get(i))
			{
				if (genieUnconditional.get(i))
				{
					temp = genieReturnValues.get(i);
				}
				else if (temp == genieCompare.get(i))
				{
					temp = genieReturnValues.get(i);
				}
			}
		}
		
		return Byte.toUnsignedInt(temp);
	}
	
	public void addGenie(int address, boolean unconditional, byte compare, byte retval)
	{
		genieAddresses.add(address);
		genieUnconditional.add(unconditional);
		genieCompare.add(compare);
		genieReturnValues.add(retval);
		if (unconditional)
		{
			System.out.println("Always read " + String.format("0x%02X", retval) + " from " + String.format("0x%04X", address));
		}
		else
		{
			System.out.println("Read " + String.format("0x%02X", retval) + " from " + String.format("0x%04X", address) + " if current value is " + String.format("0x%02X", compare));
		}
	}
	
	public synchronized int[] getAllMemory()
	{
		int[] retval = new int[0x10000 - 0x8000 + 0x800];
		for (int i = 0; i < 0x800; ++i)
		{
			retval[i] = layout[i].read();
		}
		
		int x = 0x800;
		for (int i = 0x8000; i < MEMSIZE; ++i)
		{
			retval[x++] = layout[i].read();
		}
		
		return retval;
	}
	
	public synchronized int[] getAllRam()
	{
		int[] retval = new int[0x800];
		for (int i = 0; i < 0x800; ++i)
		{
			retval[i] = layout[i].read();
		}
		
		return retval;
	}
	
	public synchronized void write(int address, int value)
	{
		layout[address].write((byte)value);
	}
	
	public void saveRam()
	{
		if (raf != null)
		{
			try
			{
				raf.getChannel().force(false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void setupCart(Cartridge cart, String filename)
	{
		this.filename = filename;
		setupCart(cart);
	}
	
	public void setupCart(Cartridge cart)
	{
		this.cart = cart;
		//System.out.println("Mapper is #" + cart.mapper());
		
		if (type == CPU)
		{
			configureCpuMem();
		}
		else
		{
			configurePpuMem();
		}
	}
	
	private void configurePpuMem()
	{
		if (cart.mapper() == 0)
		{
			mapper0Ppu();
		} else if (cart.mapper() == 1)
		{
			mapper1Ppu();
		}
		else if (cart.mapper() == 2 || cart.mapper() == 3 || cart.mapper() == 66 || cart.mapper() == 71 || cart.mapper() == 206)
		{
			mapper3Ppu();
		} else if (cart.mapper() == 4)
		{
			mapper4Ppu();
		}
		else if (cart.mapper() == 7)
		{
			mapper7Ppu();
		} else if (cart.mapper() == 9)
		{
			mapper9Ppu();
		} else if (cart.mapper() == 10)
		{
			mapper10Ppu();
		} else if (cart.mapper() == 0xfffe)
		{
			mapperfffePpu();
		} 
		else if (cart.mapper() == 0xffff)
		{
			mapperffffPpu();
		}
		
		for (int i = 0x3000; i < 0x3f00; ++i)
		{
			layout[i] = new RedirectMemoryPort(i - 0x1000, this);
		}
		
		layout[0x3f10] = layout[0x3f00];
		layout[0x3f14] = layout[0x3f04];
		layout[0x3f18] = layout[0x3f08];
		layout[0x3f1c] = layout[0x3f0c];
		
		for (int i = 0x3f20; i < 0x4000; ++i)
		{
			layout[i] = new RedirectMemoryPort(0x3f00 + (i & 0x001f), this);
		}
		
		for (int i = 0x4000; i < MEMSIZE; ++i)
		{
			layout[i] = new RedirectMemoryPort(i & 0x3fff, this);
		}
	}
	
	private void configureCpuMem()
	{
		for (int i = 0x800; i < 0x2000; ++i)
		{
			layout[i] = new RedirectMemoryPort(i & 0x7ff, this);
		}
		
		layout[0x2000] = new Register2000(ppu);
		layout[0x2001] = new Register2001(ppu);
		layout[0x2002] = new Register2002(ppu);
		layout[0x2003] = new Register2003(ppu);
		layout[0x2004] = new Register2004(ppu);
		layout[0x2005] = new Register2005(ppu);
		layout[0x2006] = new Register2006(ppu);
		layout[0x2007] = new Register2007(ppu);
		
		for (int i = 0x2008; i < 0x4000; ++i)
		{
			layout[i] = new RedirectMemoryPort(0x2000 + (i & 0x07), this);
		}
		
		layout[0x4000] = new Register4000(cpu);
		layout[0x4001] = new Register4001(cpu);
		layout[0x4002] = new Register4002(cpu);
		layout[0x4003] = new Register4003(cpu);
		layout[0x4004] = new Register4004(cpu);
		layout[0x4005] = new Register4005(cpu);
		layout[0x4006] = new Register4006(cpu);
		layout[0x4007] = new Register4007(cpu);
		layout[0x4008] = new Register4008(cpu);
		layout[0x400a] = new Register400a(cpu);
		layout[0x400b] = new Register400b(cpu);
		layout[0x400c] = new Register400c(cpu);
		layout[0x400e] = new Register400e(cpu);
		layout[0x400f] = new Register400f(cpu);
		layout[0x4010] = new Register4010(cpu);
		layout[0x4011] = new Register4011(cpu);
		layout[0x4012] = new Register4012(cpu);
		layout[0x4013] = new Register4013(cpu);
		layout[0x4014] = new Register4014(cpu, ppu);
		layout[0x4015] = new Register4015(cpu);
		layout[0x4016] = new Register4016(gui, cpu);
		layout[0x4017] = new Register4017(gui, (Register4016)layout[0x4016], cpu);
		
		for (int i = 0x4018; i < MEMSIZE; ++i)
		{
			layout[i] = new BlackHole();
		}
		
		if (cart.mapper() == 0)
		{
			mapper0();
		} else if (cart.mapper() == 1)
		{
			mapper1();
		}
		else if (cart.mapper() == 2)
		{
			mapper2();
		}
		else if (cart.mapper() == 3)
		{
			mapper3(); 
		} else if (cart.mapper() == 4)
		{
			mapper4();
		}
		else if (cart.mapper() == 7)
		{
			mapper7();
		} else if (cart.mapper() == 9)
		{
			mapper9();
		}
		else if (cart.mapper() == 10)
		{
			mapper10();
		}
		else if (cart.mapper() == 66)
		{
			mapper66();
		}
		else if (cart.mapper() == 71)
		{
			mapper71();
		} else if (cart.mapper() == 206)
		{
			mapper206();
		} else if (cart.mapper() == 0xfffe)
		{
			mapperfffe();
		} 
		else if (cart.mapper() == 0xffff)
		{
			mapperffff();
		}
		else
		{
			synchronized(System.out) 
			{
				System.out.println("Mapper " + cart.mapper() + " is not yet supported");
			}
		}
		
		//layout[0x459] = new MonitorMemoryPort(0x459);
		//layout[0x45a] = new MonitorMemoryPort(0x45a);
		//layout[0x45b] = new MonitorMemoryPort(0x45b);
	}
	
	private void mapper0()
	{
		String filename;
		if (this.filename.equals(""))
		{
			synchronized(System.out) 
			{
				System.out.print("Enter save file to use: ");
			}
		
			Scanner in = new Scanner(System.in);
			filename = in.nextLine();
		}
		else
		{
			filename = this.filename;
		}
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			if (raf.length() != 4096)
			{
				byte[] b = new byte[4096];
				raf.write(b);
			}
			
			for (int i = 0x6000; i < 0x7000; ++i)
			{
				layout[i] = new BatteryBackedRamPort(raf, i - 0x6000);
			}
			
			for (int i = 0x7000; i < 0x8000; ++i)
			{
				layout[i] = new RedirectMemoryPort(i - 0x1000, this);
			}
			
			if (cart.prgData().length > 0)
			{
				loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			}
			
			if (cart.prgData().length > 16384)
			{
				loadRomAtAddress(cart.prgData(), 0xc000, 16384, 16384);
			}
			else
			{
				for (int i = 0xc000; i < MEMSIZE; ++i)
				{
					layout[i] = new RedirectMemoryPort((i & 0x3fff) + 0x8000, this);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mapper0();
			return;
		}
	}
	
	private void mapper1()
	{
		String filename;
		if (this.filename.equals(""))
		{
			synchronized(System.out) 
			{
				System.out.print("Enter save file to use: ");
			}
		
			Scanner in = new Scanner(System.in);
			filename = in.nextLine();
		}
		else
		{
			filename = this.filename;
		}
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			if (raf.length() != 8192)
			{
				byte[] b = new byte[8192];
				raf.write(b);
			}
			
			for (int i = 0x6000; i < 0x8000; ++i)
			{
				layout[i] = new BatteryBackedRamPort(raf, i - 0x6000);
			}
			
			if (cart.prgData().length > 0)
			{
				loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			}
			
			if (cart.prgData().length > 16384)
			{
				loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
			}
			
			for (int i = 0x8000; i < MEMSIZE; ++i)
			{
				layout[i] = new Mmc1Port(this, i, layout[i]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mapper9();
			return;
		}
	}
	
	private void mapper2()
	{
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
		}
		
		for (int i = 0x8000; i < MEMSIZE; ++i)
		{
			layout[i] = new Mapper2BankSelect(this, layout[i]);
		}
	}
	
	private void mapper3()
	{		
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
		}
		
		if (cart.prgData().length > 16384)
		{
			loadRomAtAddress(cart.prgData(), 0xc000, 16384, 16384);
		}
		else
		{
			for (int i = 0xc000; i < MEMSIZE; ++i)
			{
				layout[i] = new RedirectMemoryPort(i & 0x3fff, this);
			}
		}
		
		for (int i = 0x8000; i < MEMSIZE; ++i)
		{
			layout[i] = new Mapper3BankSelect(this, layout[i]);
		}
	}
	
	private void mapper4()
	{
		String filename;
		if (this.filename.equals(""))
		{
			synchronized(System.out) 
			{
				System.out.print("Enter save file to use: ");
			}
		
			Scanner in = new Scanner(System.in);
			filename = in.nextLine();
		}
		else
		{
			filename = this.filename;
		}
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			if (raf.length() != 8192)
			{
				byte[] b = new byte[8192];
				raf.write(b);
			}
			
			for (int i = 0x6000; i < 0x8000; ++i)
			{
				layout[i] = new BatteryBackedRamPort(raf, i - 0x6000);
			}
			
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 8192);
			loadRomAtAddress(cart.prgData(), 0xa000, 8192, 8192);
			loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 8192);
			loadRomAtAddress(cart.prgData(), 0xe000, cart.prgData().length - 8192, 8192);
			
			for (int i = 0x8000; i < MEMSIZE; ++i)
			{
				layout[i] = new Mmc3Port(this, i, layout[i], cart);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mapper9();
			return;
		}
	}
	
	private void mapper7()
	{		
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 32768);
		}
		
		for (int i = 0x8000; i < MEMSIZE; ++i)
		{
			layout[i] = new Mapper7BankSelect(this, layout[i]);
		}
	}
	
	private void mapper9()
	{
		String filename;
		if (this.filename.equals(""))
		{
			synchronized(System.out) 
			{
				System.out.print("Enter save file to use: ");
			}
		
			Scanner in = new Scanner(System.in);
			filename = in.nextLine();
		}
		else
		{
			filename = this.filename;
		}
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			if (raf.length() != 8192)
			{
				byte[] b = new byte[8192];
				raf.write(b);
			}
			
			for (int i = 0x6000; i < 0x8000; ++i)
			{
				layout[i] = new BatteryBackedRamPort(raf, i - 0x6000);
			}
			
			if (cart.prgData().length > 0)
			{
				loadRomAtAddress(cart.prgData(), 0x8000, 0, 8192);
			}
			
			if (cart.prgData().length > 8192)
			{
				loadRomAtAddress(cart.prgData(), 0xa000, cart.prgData().length - 24 * 1024, 24 * 1024);
			}
			
			for (int i = 0xa000; i < 0xb000; ++i)
			{
				layout[i] = new Mapper9BankSelect1(this, layout[i]);
			}
			
			for (int i = 0xb000; i < 0xc000; ++i)
			{
				layout[i] = new Mapper9BankSelect2(this, layout[i]);
			}
			
			for (int i = 0xc000; i < 0xd000; ++i)
			{
				layout[i] = new Mapper9BankSelect3(this, layout[i]);
			}
			
			for (int i = 0xd000; i < 0xe000; ++i)
			{
				layout[i] = new Mapper9BankSelect4(this, layout[i]);
			}
			
			for (int i = 0xe000; i < 0xf000; ++i)
			{
				layout[i] = new Mapper9BankSelect5(this, layout[i]);
			}
			
			for (int i = 0xf000; i < MEMSIZE; ++i)
			{
				layout[i] = new Mapper9MirroringSelect(this, layout[i]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mapper9();
			return;
		}
	}
	
	private void mapper10()
	{
		String filename;
		if (this.filename.equals(""))
		{
			synchronized(System.out) 
			{
				System.out.print("Enter save file to use: ");
			}
		
			Scanner in = new Scanner(System.in);
			filename = in.nextLine();
		}
		else
		{
			filename = this.filename;
		}
		
		try
		{
			raf = new RandomAccessFile(filename, "rw");
			if (raf.length() != 8192)
			{
				byte[] b = new byte[8192];
				raf.write(b);
			}
			
			for (int i = 0x6000; i < 0x8000; ++i)
			{
				layout[i] = new BatteryBackedRamPort(raf, i - 0x6000);
			}
			
			if (cart.prgData().length > 0)
			{
				loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			}
			
			if (cart.prgData().length > 16384)
			{
				loadRomAtAddress(cart.prgData(), 0xa000, cart.prgData().length - 16384, 16384);
			}
			
			for (int i = 0xa000; i < 0xb000; ++i)
			{
				layout[i] = new Mapper10BankSelect(this, layout[i]);
			}
			
			for (int i = 0xb000; i < 0xc000; ++i)
			{
				layout[i] = new Mapper9BankSelect2(this, layout[i]);
			}
			
			for (int i = 0xc000; i < 0xd000; ++i)
			{
				layout[i] = new Mapper9BankSelect3(this, layout[i]);
			}
			
			for (int i = 0xd000; i < 0xe000; ++i)
			{
				layout[i] = new Mapper9BankSelect4(this, layout[i]);
			}
			
			for (int i = 0xe000; i < 0xf000; ++i)
			{
				layout[i] = new Mapper9BankSelect5(this, layout[i]);
			}
			
			for (int i = 0xf000; i < MEMSIZE; ++i)
			{
				layout[i] = new Mapper9MirroringSelect(this, layout[i]);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mapper9();
			return;
		}
	}
	
	private void mapper66()
	{
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
		}
		
		if (cart.prgData().length > 16384)
		{
			loadRomAtAddress(cart.prgData(), 0xc000, 16384, 16384);
		}
		
		for (int i = 0x8000; i < MEMSIZE; ++i)
		{
			layout[i] = new Mapper66BankSelect(this, layout[i]);
		}
	}
	
	private void mapper71()
	{
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
		}
		
		for (int i = 0xC000; i < MEMSIZE; ++i)
		{
			layout[i] = new Mapper71BankSelect(this, layout[i]);
		}
	}
	
	private void mapper206()
	{
		if (cart.prgData().length > 0)
		{
			loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
			loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
		}
		
		for (int i = 0x8000; i < 0xa000; i += 2)
		{
			layout[i] = new Mapper206BankSelect1(this, layout[i]);
			layout[i + 1] = new Mapper206BankSelect2(this, layout[i+1]);
		}
	}
	
	private void mapperfffe()
	{
		loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
		loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
		
		for (int i = 0x8000; i < 0xa000; ++i)
		{
			layout[i] = new MapperfffeBankSelect1(this, layout[i], false);
		}
		
		for (int i = 0xa000; i < 0xc000; ++i)
		{
			layout[i] = new MapperfffeBankSelect1(this, layout[i], true);
		}
		
		for (int i = 0xc000; i < 0xe000; ++i)
		{
			layout[i] = new MapperfffeBankSelect2(this, layout[i], false);
		}
		
		for (int i = 0xe000; i < MEMSIZE; ++i)
		{
			layout[i] = new MapperfffeBankSelect2(this, layout[i], true);
		}
	}
	
	private void mapperffff()
	{
		loadRomAtAddress(cart.prgData(), 0x8000, 0, 16384);
		loadRomAtAddress(cart.prgData(), 0xc000, cart.prgData().length - 16384, 16384);
		
		for (int i = 0x8000; i < 0xa000; ++i)
		{
			layout[i] = new MapperffffBankSelect1(this, layout[i], false);
		}
		
		for (int i = 0xa000; i < 0xc000; ++i)
		{
			layout[i] = new MapperffffBankSelect1(this, layout[i], true);
		}
		
		for (int i = 0xc000; i < 0xe000; ++i)
		{
			layout[i] = new MapperffffBankSelect2(this, layout[i], false);
		}
		
		for (int i = 0xe000; i < MEMSIZE; ++i)
		{
			layout[i] = new MapperffffBankSelect2(this, layout[i], true);
		}
	}
	
	private void mapper0Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRamAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		if (!cart.fourScreen())
		{
			if (cart.vertMirroring())
			{
				for (int i = 0x2800; i < 0x3000; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x800, this);
				}
			}
			else
			{
				for (int i = 0x2400; i < 0x2800; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x400, this);
				}
				
				for (int i = 0x2c00; i < 0x3000; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x400, this);
				}
			}
		}
	}
	
	private void mapper1Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRamAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		for (int i = 0x2000; i < 0x3000; ++i)
		{
			layout[i] = new Mmc1NametablePort((i - 0x2000) & 0x7ff);
		}
	}
	
	private void mapper3Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRomAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		if (!cart.fourScreen())
		{
			if (cart.vertMirroring())
			{
				for (int i = 0x2800; i < 0x3000; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x800, this);
				}
			}
			else
			{
				for (int i = 0x2400; i < 0x2800; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x400, this);
				}
				
				for (int i = 0x2c00; i < 0x3000; ++i)
				{
					layout[i] = new RedirectMemoryPort(i - 0x400, this);
				}
			}
		}
	}
	
	private void mapper4Ppu()
	{
		loadRomAtAddress(cart.chrData(), 0, 0, 2048);
		loadRomAtAddress(cart.chrData(), 0x800, 2048, 2048);
		loadRomAtAddress(cart.chrData(), 0x1000, 4096, 1024);
		loadRomAtAddress(cart.chrData(), 0x1400, 0x1400, 1024);
		loadRomAtAddress(cart.chrData(), 0x1800, 0x1800, 1024);
		loadRomAtAddress(cart.chrData(), 0x1c00, 0x1c00, 1024);
		
		for (int i = 0x2000; i < 0x3000; ++i)
		{
			layout[i] = new Mmc3NametablePort((i - 0x2000) & 0x7ff);
		}
	}
	
	private void mapper7Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRomAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		for (int i = 0x2000; i < 0x2400; ++i)
		{
			layout[i] = new TwoStateMemoryPort(this, 1, true);
		}
		
		for (int i = 0x2400; i < 0x3000; ++i)
		{
			layout[i] = new RedirectMemoryPort(0x2000 + (i & 0x3ff), this);
		}
	}
	
	private void mapper9Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRomAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		for (int i = 0; i < 0x1000; ++i)
		{
			layout[i] = new TwoStateMemoryPort(this, 1, false);
		}
		
		for (int i = 0x1000; i < 0x2000; ++i)
		{
			layout[i] = new TwoStateMemoryPort(this, 2, false);
		}
		
		layout[0x0fd8] = new Latch0Port(this, 0x0fd8, false);
		layout[0x0fe8] = new Latch0Port(this, 0x0fe8, true);
		
		for (int i = 0x01fd8; i <= 0x1fdf; ++i)
		{
			layout[i] = new Latch1Port(this, i, false);
		}
		
		for (int i = 0x01fe8; i <= 0x1fef; ++i)
		{
			layout[i] = new Latch1Port(this, i, true);
		}
		
		for (int i = 0x2400; i < 0x2800; ++i)
		{
			layout[i] = new RealOrRedirectable(this, true, i - 0x400); //starts real
		}
		
		for (int i = 0x2800; i < 0x2c00; ++i)
		{
			layout[i] = new RealOrRedirectable(this, false, i - 0x800);
		}
		
		for (int i = 0x2c00; i < 0x3000; ++i)
		{
			layout[i] = new SwitchableRedirect(this, true, i - 0x400, i - 0x800);
		}
		
		control = 3;
	}
	
	private void mapper10Ppu()
	{
		if (cart.chrData().length > 0)
		{
			loadRomAtAddress(cart.chrData(), 0, 0, 8192);
		}
		
		for (int i = 0; i < 0x1000; ++i)
		{
			layout[i] = new TwoStateMemoryPort(this, 1, false);
		}
		
		for (int i = 0x1000; i < 0x2000; ++i)
		{
			layout[i] = new TwoStateMemoryPort(this, 2, false);
		}
		
		for (int i = 0x0fd8; i <= 0x0fdf; ++i)
		{
			layout[i] = new Latch0Port(this, i, false);
		}
		
		for (int i = 0x0fe8; i <= 0x0fef; ++i)
		{
			layout[i] = new Latch0Port(this, i, true);
		}
		
		for (int i = 0x1fd8; i <= 0x1fdf; ++i)
		{
			layout[i] = new Latch1Port(this, i, false);
		}
		
		for (int i = 0x1fe8; i <= 0x1fef; ++i)
		{
			layout[i] = new Latch1Port(this, i, true);
		}
		
		for (int i = 0x2400; i < 0x2800; ++i)
		{
			layout[i] = new RealOrRedirectable(this, true, i - 0x400); //starts real
		}
		
		for (int i = 0x2800; i < 0x2c00; ++i)
		{
			layout[i] = new RealOrRedirectable(this, false, i - 0x800);
		}
		
		for (int i = 0x2c00; i < 0x3000; ++i)
		{
			layout[i] = new SwitchableRedirect(this, true, i - 0x400, i - 0x800);
		}
		
		control = 3;
	}
	
	private void mapperfffePpu()
	{
		if (cart.chrData().length > 0)
		{
			loadRamAtAddress(cart.chrData(), 0, 0, 16384);
		}
	}
	
	private void mapperffffPpu()
	{
		if (cart.chrData().length > 0)
		{
			loadRamAtAddress(cart.chrData(), 0, 0, 8192);
		}
	}
	
	public synchronized void loadRomAtAddress(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				layout[addr + i] = new RomMemoryPort(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRamAtAddress(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				layout[addr + i] = new DefaultMemoryPort(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper1(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mmc1Port)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper2(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mapper2BankSelect)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper4(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mmc3Port)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper7(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mapper7BankSelect)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper10(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mapper10BankSelect)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper66(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mapper66BankSelect)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper71(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((Mapper71BankSelect)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapper206(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; i += 2)
			{
				((Mapper206BankSelect1)layout[addr + i]).updateDelegate(data[offset + i]);
				((Mapper206BankSelect2)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapperfffe(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((MapperfffeBankSelect2)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadRomAtAddressMapperffff(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((MapperffffBankSelect1)layout[addr + i]).updateDelegate(data[offset + i]);
			}
		}
	}
	
	public Cartridge getCart()
	{
		return cart;
	}
	
	public synchronized void loadState1AtAddress(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((TwoStateMemoryPort)layout[addr + i]).setValue1(data[offset + i]);
			}
		}
	}
	
	public synchronized void loadState2AtAddress(byte[] data, int addr, int offset, int len)
	{
		if (data.length > 0)
		{
			offset %= data.length;
			for (int i = 0; i < len; ++i)
			{
				((TwoStateMemoryPort)layout[addr + i]).setValue2(data[offset + i]);
			}
		}
	}
}
