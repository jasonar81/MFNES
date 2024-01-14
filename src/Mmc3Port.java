
public class Mmc3Port implements MemoryPort {
	private Memory mem;
	private int address;
	private MemoryPort delegate;
	private Clock clock;
	private Cartridge cart;
	private static boolean init = false;
	private static boolean prgMode = false;
	private static boolean chrMode = false;
	private static int command = 0;
	private static boolean mirroring = false;
	public static volatile int irqReloadValue = 0;
	public static volatile boolean needsReload = false;
	public static volatile boolean irqEnabled = false;
	public static volatile int counter = 0;
	
	public static MemoryPort[] nametableData = new MemoryPort[2048];
	
	static
	{
		for (int i = 0; i < 2048; ++i)
		{
			nametableData[i] = new DefaultMemoryPort();
		}
	}
	
	public Mmc3Port(Memory mem, int address, MemoryPort delegate, Cartridge cart)
	{
		this.mem = mem;
		this.address = address;
		this.delegate = delegate;
		this.clock = mem.getPpu().getClock();
		this.cart = cart;
	}

	@Override
	public byte read() {
		if (!init)
		{
			if (!cart.fourScreen())
			{
				setVerticalMirror(mem.getPpu().getMem());
			}
			
			init = true;
		}
		
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		if (!init)
		{
			if (!cart.fourScreen())
			{
				setVerticalMirror(mem.getPpu().getMem());
			}
			
			init = true;
		}
		
		int x = Byte.toUnsignedInt(val);
		if (address >= 0x8000 && address < 0xa000)
		{
			if ((address & 0x01) == 0)
			{
				chrMode = Utils.getBit(x, 7);
				prgMode = Utils.getBit(x, 6);
				command = (x & 0x07);
			}
			else
			{
				processCommand(x, mem, mem.getPpu().getMem());
			}
		}
		else if (address >= 0xa000 && address < 0xc000)
		{
			if ((address & 0x01) == 0)
			{
				if (!cart.fourScreen())
				{
					boolean newMirroring = Utils.getBit(x, 0);
					if (newMirroring != mirroring)
					{
						if (newMirroring)
						{
							setHorizontalMirror(mem.getPpu().getMem());
						}
						else
						{
							setVerticalMirror(mem.getPpu().getMem());
						}
						
						mirroring = newMirroring;
					}
				}
			}
		}
		else if (address >= 0xc000 && address < 0xe000)
		{
			if ((address & 0x01) == 0)
			{
				irqReloadValue = x;
			}
			else
			{
				needsReload = true;
			}
		}
		else
		{
			if ((address & 0x01) == 0)
			{
				irqEnabled = false;
			}
			else
			{
				irqEnabled = true;
			}
		}
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}
	
	private static void setVerticalMirror(Memory ppuMem)
	{
		//System.out.println("Vertical");
		for (int i = 0x2000; i < 0x2400; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
		}
		
		for (int i = 0x2400; i < 0x2800; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
		}
		
		for (int i = 0x2800; i < 0x2c00; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
		}
		
		for (int i = 0x2c00; i < 0x3000; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
		}
	}
	
	private static void setHorizontalMirror(Memory ppuMem)
	{
		//System.out.println("Horizontal");
		for (int i = 0x2000; i < 0x2400; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
		}
		
		for (int i = 0x2400; i < 0x2800; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
		}
		
		for (int i = 0x2800; i < 0x2c00; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
		}
		
		for (int i = 0x2c00; i < 0x3000; ++i)
		{
			((Mmc3NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
		}
	}
	
	private static void processCommand(int val, Memory cpuMem, Memory ppuMem)
	{
		if (command == 0)
		{
			if (!chrMode)
			{
				//Swap in 2k at 0
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0, (val & 0xfe) * 1024, 2048);
			}
			else
			{
				//Swap in 2k at 0x1000
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1000, (val & 0xfe) * 1024, 2048);
			}
		}
		else if (command == 1)
		{
			if (!chrMode)
			{
				//Swap in 2k at 0x800
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x800, (val & 0xfe) * 1024, 2048);
			}
			else
			{
				//Swap in 2k at 0x1800
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1800, (val & 0xfe) * 1024, 2048);
			}
		}
		else if (command == 2)
		{
			if (!chrMode)
			{
				//Swap in 1k at 0x1000
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1000, val * 1024, 1024);
			}
			else
			{
				//Swap in 1k at 0x0
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0, val * 1024, 1024);
			}
		}
		else if (command == 3)
		{
			if (!chrMode)
			{
				//Swap in 1k at 0x1400
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1400, val * 1024, 1024);
			}
			else
			{
				//Swap in 1k at 0x400
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x400, val * 1024, 1024);
			}
		}
		else if (command == 4)
		{
			if (!chrMode)
			{
				//Swap in 1k at 0x1800
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1800, val * 1024, 1024);
			}
			else
			{
				//Swap in 1k at 0x800
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x800, val * 1024, 1024);
			}
		}
		else if (command == 5)
		{
			if (!chrMode)
			{
				//Swap in 1k at 0x1c00
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0x1c00, val * 1024, 1024);
			}
			else
			{
				//Swap in 1k at 0xc00
				ppuMem.loadRomAtAddress(ppuMem.getCart().chrData(), 0xc00, val * 1024, 1024);
			}
		}
		else if (command == 6)
		{
			if (!prgMode)
			{
				//swap in at 0x8000
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0x8000, val * 8192, 8192);
				//0xc000 = -2
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xc000, cpuMem.getCart().prgData().length - 16384, 8192);
				//0xe000 = -1
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xe000, cpuMem.getCart().prgData().length - 8192, 8192);
			}
			else
			{
				//swap in at 0xc000
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xc000, val * 8192, 8192);
				//0x8000 = -2
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0x8000, cpuMem.getCart().prgData().length - 16384, 8192);
				//0xe000 = -1
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xe000, cpuMem.getCart().prgData().length - 8192, 8192);
			}
		} else if (command == 7)
		{
			if (!prgMode)
			{
				//swap in at 0xa000
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xa000, val * 8192, 8192);
				//0xc000 = -2
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xc000, cpuMem.getCart().prgData().length - 16384, 8192);
				//0xe000 = -1
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xe000, cpuMem.getCart().prgData().length - 8192, 8192);
			}
			else
			{
				//swap in at 0xa000
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xa000, val * 8192, 8192);
				//0x8000 = -2
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0x8000, cpuMem.getCart().prgData().length - 16384, 8192);
				//0xe000 = -1
				cpuMem.loadRomAtAddressMapper4(cpuMem.getCart().prgData(), 0xe000, cpuMem.getCart().prgData().length - 8192, 8192);
			}
		}
	}
}
