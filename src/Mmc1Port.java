
public class Mmc1Port implements MemoryPort {
	private Memory mem;
	private int address;
	private MemoryPort delegate;
	private Clock clock;
	private Mmc1Data mmc1Data;
	
	public Mmc1Port(Memory mem, int address, MemoryPort delegate, Mmc1Data mmc1Data)
	{
		this.mem = mem;
		this.address = address;
		this.delegate = delegate;
		this.clock = mem.getPpu().getClock();
		this.mmc1Data = mmc1Data;
		
		for (int i = 0; i < 2048; ++i)
		{
			mmc1Data.nametableData[i] = new DefaultMemoryPort();
		}
	}

	@Override
	public byte read() {
		return delegate.read();
	}

	@Override
	public void write(byte val) {
		//System.out.println("Wrote " + String.format("0x%02X", val) + " to MMC1 control");
		int x = Byte.toUnsignedInt(val);
		if (Utils.getBit(x, 7))
		{
			//System.out.println("Cleared control");
			mem.writeControl(0);
			mmc1Data.writeNumber = 0;
			mmc1Data.previousWriteCycle = 0;
			return;
		}
		
		if (clock.getCpuExpectedCycle() - mmc1Data.previousWriteCycle > 1)
		{
			if (Utils.getBit(x, 0))
			{
				mem.writeControl((mem.getControl() >> 1) + 0x10);
			} else
			{
				mem.writeControl(mem.getControl() >> 1);
			}
		
			++mmc1Data.writeNumber;
			mmc1Data.previousWriteCycle = clock.getCpuExpectedCycle();
			
			if (mmc1Data.writeNumber == 5)
			{
				//Uses bits 13 and 14 of this address
				//System.out.println("Processing control value of " + String.format("0x%02X", mem.getControl()));
				doMmc1Register(mem.getControl(), mem, address, mem.getPpu().getMem());
				mem.writeControl(0);
				mmc1Data.writeNumber = 0;
			}
		}
		else
		{
			mmc1Data.previousWriteCycle = clock.getCpuExpectedCycle();
			//System.out.println("Skipped write of " + val + " because of consecutive cycles");
		}
	}
	
	private void doMmc1Register(int val, Memory cpuMem, int address, Memory ppuMem)
	{
		if (address >= 0xa000 && address < 0xc000)
		{
			//System.out.println("Setting ppu bank 1");
			if (mmc1Data.twoBanks)
			{
				ppuMem.loadRamAtAddress(ppuMem.getCart().chrData(), 0, val * 4096, 4096);
			}
			else
			{
				ppuMem.loadRamAtAddress(ppuMem.getCart().chrData(), 0, (val & 0xfe) * 4096, 8192);
			}
		} else if (address >= 0xc000 && address < 0xe000)
		{
			//System.out.println("Setting ppu bank 2");
			if (mmc1Data.twoBanks)
			{
				ppuMem.loadRamAtAddress(ppuMem.getCart().chrData(), 0x1000, val * 4096, 4096);
			}
		} else if (address >= 0xe000)
		{
			//System.out.println("Setting cpu bank");
			val &= 0x0f;
			if (mmc1Data.prgBankMode < 2)
			{
				val &= 0xfe;
				cpuMem.loadRomAtAddressMapper1(cpuMem.getCart().prgData(), 0x8000, val * 16384, 32768);
			} else if (mmc1Data.prgBankMode == 2)
			{
				cpuMem.loadRomAtAddressMapper1(cpuMem.getCart().prgData(), 0x8000, 0, 16384);
				cpuMem.loadRomAtAddressMapper1(cpuMem.getCart().prgData(), 0xc000, val * 16384, 16384);
			} else
			{
				cpuMem.loadRomAtAddressMapper1(cpuMem.getCart().prgData(), 0x8000, val * 16384, 16384);
				cpuMem.loadRomAtAddressMapper1(cpuMem.getCart().prgData(), 0xc000, cpuMem.getCart().prgData().length - 16384, 16384);
			}
		} else
		{
			mmc1Data.twoBanks = Utils.getBit(val, 4);
			mmc1Data.prgBankMode = ((val & 0x0c) >> 2);
			mmc1Data.nametableMode = (val & 0x03);
			//System.out.println("Two banks = " + twoBanks + ", prg mode = " + prgBankMode + ", nametable mode = " + nametableMode);
			if (mmc1Data.nametableMode == 0)
			{
				for (int i = 0x2000; i < 0x3000; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress((i & 0x3ff));
				}
			} else if (mmc1Data.nametableMode == 1)
			{
				for (int i = 0x2000; i < 0x3000; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
				}
			} else if (mmc1Data.nametableMode == 2)
			{
				//System.out.println("Vertical");
				for (int i = 0x2000; i < 0x2400; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
				}
				
				for (int i = 0x2400; i < 0x2800; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
				}
				
				for (int i = 0x2800; i < 0x2c00; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
				}
				
				for (int i = 0x2c00; i < 0x3000; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
				}
			} else
			{
				//System.out.println("Horizontal");
				for (int i = 0x2000; i < 0x2400; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
				}
				
				for (int i = 0x2400; i < 0x2800; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x000 + (i & 0x3ff));
				}
				
				for (int i = 0x2800; i < 0x2c00; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
				}
				
				for (int i = 0x2c00; i < 0x3000; ++i)
				{
					((Mmc1NametablePort)ppuMem.getLayout()[i]).setAddress(0x400 + (i & 0x3ff));
				}
			}
		}
	}
	
	public void updateDelegate(byte val)
	{
		((RomMemoryPort)delegate).forceWrite(val);
	}
	
	public Mmc1Data getMmc1Data()
	{
		return mmc1Data;
	}
}
