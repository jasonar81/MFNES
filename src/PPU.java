//The Picture Processing Unit
//Generate the data from every frame in the video and sends it to the GUI

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PPU implements Runnable {
	private Clock clock;
	private Memory mem;
	
	private volatile int ctrl;
	private volatile int mask;
	private AtomicInteger status;
	private AtomicInteger oamaddr;
	private volatile int scroll;
	private volatile int ppuAddr;
	private volatile int bus;
	private volatile boolean oddFrameFlag;
	private volatile boolean lowHighFlag;
	private int frameScroll; //Fixes the vertical scroll at the start of the frame
	private long frameStart;
	private CPU cpu;
	private GUI gui;
	private byte[] nameTableBytes = new byte[272];
	private byte[] attributeBytes = new byte[272];
	private byte[] patternBytes = new byte[272];
	private int[] spriteColors = new int[256];
	private boolean sentNmi;
	int savedX;
	int savedY;
	private volatile byte[] oamMem = new byte[256];
	private byte[] internalOam = new byte[32];
	private byte[] savedInternalOam = new byte[32];
	private int lineOamAddr;
	private int oamIndex;
	private int internalOamIndex;
	private boolean spriteZeroFlag = false;
	private AtomicBoolean justReadVbl;
	
	private AtomicBoolean debugHold;
	private AtomicBoolean terminate;
	private AtomicBoolean reset;
	private AtomicInteger stepFlag;
	private AtomicBoolean pendingOamWrite;
	private volatile byte pendingOamWriteValue;
	private volatile int pendingOamWriteAddress;
	
	private long cycle;
	private long overage = 0;
	private int spriteIndex;
	private volatile boolean rendering = false;
	private volatile byte oamByte;
	
	private int[] colorLookup = new int[64];
	private volatile int nametableSelect = -1;
	private volatile int externallyReportedScanline;
	
	public PPU(Clock clock, Memory mem, GUI gui)
	{
		this.clock = clock;
		this.mem = mem;
		this.gui = gui;
		status = new AtomicInteger(0);
		oamaddr = new AtomicInteger(0);
		debugHold = new AtomicBoolean(false);
		terminate = new AtomicBoolean(false);
		reset = new AtomicBoolean(false);
		stepFlag = new AtomicInteger(0);
		pendingOamWrite = new AtomicBoolean(false);
		justReadVbl = new AtomicBoolean(false);
		ctrl = 0;
		mask = 0;
		scroll = 0;
		ppuAddr = 0;
		oddFrameFlag = false;
		lowHighFlag = false;
		bus = 0;
		sentNmi = false;
		
		colorLookup[0x00] = makeRGB(0x6b, 0x6b, 0x6b);
		colorLookup[0x01] = makeRGB(0, 0x1b, 0x87);
		colorLookup[0x02] = makeRGB(0x21, 0, 0x9a);
		colorLookup[0x03] = makeRGB(0x40, 0, 0x8c);
		colorLookup[0x04] = makeRGB(0x60, 0, 0x67);
		colorLookup[0x05] = makeRGB(0x64, 0, 0x1e);
		colorLookup[0x06] = makeRGB(0x59, 0x08, 0);
		colorLookup[0x07] = makeRGB(0x46, 0x16, 0);
		colorLookup[0x08] = makeRGB(0x26, 0x36, 0);
		colorLookup[0x09] = makeRGB(0, 0x45, 0);
		colorLookup[0x0a] = makeRGB(0, 0x47, 0x08);
		colorLookup[0x0b] = makeRGB(0, 0x42, 0x1d);
		colorLookup[0x0c] = makeRGB(0, 0x36, 0x59);
		colorLookup[0x0d] = makeRGB(0, 0, 0);
		colorLookup[0x0e] = makeRGB(0, 0, 0);
		colorLookup[0x0f] = makeRGB(0, 0, 0);
		colorLookup[0x10] = makeRGB(0xb4, 0xb4, 0xb4);
		colorLookup[0x11] = makeRGB(0x15, 0x55, 0xce);
		colorLookup[0x12] = makeRGB(0x43, 0x37, 0xea);
		colorLookup[0x13] = makeRGB(0x71, 0x24, 0xda);
		colorLookup[0x14] = makeRGB(0x9c, 0x1a, 0xb6);
		colorLookup[0x15] = makeRGB(0xaa, 0x11, 0x64);
		colorLookup[0x16] = makeRGB(0xa8, 0x2e, 0);
		colorLookup[0x17] = makeRGB(0x87, 0x4b, 0);
		colorLookup[0x18] = makeRGB(0x66, 0x6b, 0);
		colorLookup[0x19] = makeRGB(0x21, 0x83, 0);
		colorLookup[0x1a] = makeRGB(0, 0x8a, 0);
		colorLookup[0x1b] = makeRGB(0, 0x81, 0x44);
		colorLookup[0x1c] = makeRGB(0, 0x76, 0x91);
		colorLookup[0x1d] = makeRGB(0, 0, 0);
		colorLookup[0x1e] = makeRGB(0, 0, 0);
		colorLookup[0x1f] = makeRGB(0, 0, 0);
		colorLookup[0x20] = makeRGB(0xff, 0xff, 0xff);
		colorLookup[0x21] = makeRGB(0x63, 0xaf, 0xff);
		colorLookup[0x22] = makeRGB(0x82, 0x96, 0xff);
		colorLookup[0x23] = makeRGB(0xc0, 0x7d, 0xfe);
		colorLookup[0x24] = makeRGB(0xe9, 0x77, 0xff);
		colorLookup[0x25] = makeRGB(0xf5, 0x72, 0xcd);
		colorLookup[0x26] = makeRGB(0xf4, 0x88, 0x6b);
		colorLookup[0x27] = makeRGB(0xdd, 0xa0, 0x29);
		colorLookup[0x28] = makeRGB(0xbd, 0xbd, 0x0a);
		colorLookup[0x29] = makeRGB(0x89, 0xd2, 0x0e);
		colorLookup[0x2a] = makeRGB(0x5c, 0xde, 0x3e);
		colorLookup[0x2b] = makeRGB(0x4b, 0xd8, 0x86);
		colorLookup[0x2c] = makeRGB(0x4d, 0xcf, 0xd2);
		colorLookup[0x2d] = makeRGB(0x50, 0x50, 0x50);
		colorLookup[0x2e] = makeRGB(0, 0, 0);
		colorLookup[0x2f] = makeRGB(0, 0, 0);
		colorLookup[0x30] = makeRGB(0xff, 0xff, 0xff);
		colorLookup[0x31] = makeRGB(0xbe, 0xe1, 0xff);
		colorLookup[0x32] = makeRGB(0xd4, 0xd4, 0xff);
		colorLookup[0x33] = makeRGB(0xe3, 0xca, 0xff);
		colorLookup[0x34] = makeRGB(0xf0, 0xc9, 0xff);
		colorLookup[0x35] = makeRGB(0xff, 0xc6, 0xe3);
		colorLookup[0x36] = makeRGB(0xff, 0xce, 0xc9);
		colorLookup[0x37] = makeRGB(0xf4, 0xdc, 0xaf);
		colorLookup[0x38] = makeRGB(0xeb, 0xa5, 0xa1);
		colorLookup[0x39] = makeRGB(0xd2, 0xef, 0xa2);
		colorLookup[0x3a] = makeRGB(0xbe, 0xf4, 0xb5);
		colorLookup[0x3b] = makeRGB(0xb8, 0xf1, 0xd0);
		colorLookup[0x3c] = makeRGB(0xb8, 0xed, 0xf1);
		colorLookup[0x3d] = makeRGB(0xbd, 0xbd, 0xbd);
		colorLookup[0x3e] = makeRGB(0, 0, 0);
		colorLookup[0x3f] = makeRGB(0, 0, 0);
	}
	
	public int getScanline()
	{
		return externallyReportedScanline;
	}
	
	public void setFrameScroll(int val)
	{
		frameScroll = val;
		frameScroll -= externallyReportedScanline;
	}
	
	public void setNametableSelect(int val)
	{
		nametableSelect = val;
	}
	
	public Memory getMem()
	{
		return mem;
	}
	
	public Clock getClock()
	{
		return clock;
	}
	
	public void setJustReadVbl()
	{
		justReadVbl.set(true);
	}
	
	public boolean pendingOamWrite()
	{
		return pendingOamWrite.get();
	}
	
	public void setPendingOamWrite()
	{
		pendingOamWrite.set(true);
	}
	
	public void setPendingOamWriteValue(byte val)
	{
		pendingOamWriteValue = val;
		pendingOamWriteAddress = oamaddr.get();
	}
	
	public byte getOamByte()
	{
		if (rendering)
		{
			return oamByte;
		}
		else
		{
			return oamMem[oamaddr.get()];
		}
	}
	
	public void setOamByte(int addr, byte val)
	{
		oamMem[addr] = val;
		oamMem = oamMem; //For sync
	}
	
	public int getOamAddr()
	{
		return oamaddr.get();
	}
	
	public void setCPU(CPU cpu)
	{
		this.cpu = cpu;
	}
	
	public int getBus()
	{
		return bus;
	}
	
	public void setBus(int val)
	{
		bus = val;
	}
	
	public void setCtrl(int val)
	{
		ctrl = val;
	}
	
	public int getCtrl()
	{
		return ctrl;
	}
	
	public void setMask(int val)
	{
		mask = val;
	}
	
	public int getMask()
	{
		return mask;
	}
	
	public int getStatus()
	{
		return status.get();
	}
	
	public void statusOr(int val)
	{
		int expected = status.get();
		int newVal = expected | val;
		while (!status.compareAndSet(expected, newVal))
		{
			expected = status.get();
			newVal = expected | val;
		}
	}
	
	public void statusAnd(int val)
	{
		int expected = status.get();
		int newVal = expected & val;
		while (!status.compareAndSet(expected, newVal))
		{
			expected = status.get();
			newVal = expected & val;
		}
	}
	
	public void incrementOamAddr()
	{
		int expected = oamaddr.get();
		int newVal = (expected + 1) & 0xff;
		while (!oamaddr.compareAndSet(expected, newVal))
		{
			expected = oamaddr.get();
			newVal = (expected + 1) & 0xff;
		}
	}
	
	public void flipLowHigh()
	{
		lowHighFlag = !lowHighFlag;
	}
	
	public void clearLowHigh()
	{
		lowHighFlag = false;
	}
	
	public boolean getLowHigh()
	{
		return lowHighFlag;
	}
	
	public void setOamAddr(int val)
	{
		oamaddr.set(val & 0xff);
	}
	
	public int getScroll()
	{
		return scroll;
	}
	
	public void setScroll(int val)
	{
		scroll = val;
	}
	
	public int getPpuAddr()
	{
		return ppuAddr;
	}
	
	public void setPpuAddr(int val)
	{
		ppuAddr = val;
		
		if (ppuAddr >= 0x1000 && ppuAddr < 0x2000)
		{
			if (mem.getLayout()[0x2000] instanceof Mmc3NametablePort)
			{
				if (Mmc3Port.counter == 0)
				{
					if (Mmc3Port.irqEnabled)
					{
						cpu.setIrq();
					}
					
					Mmc3Port.counter = Mmc3Port.irqReloadValue;
				}
				else if (Mmc3Port.needsReload)
				{
					Mmc3Port.counter = Mmc3Port.irqReloadValue;
				} else
				{
					--Mmc3Port.counter;
				}
				
				Mmc3Port.needsReload = false;
			}
		}
	}
	
	public void setReset()
	{
		reset.set(true);
	}

	public void debugHold(boolean val)
	{
		debugHold.set(val);
	}
	
	public void terminate()
	{
		terminate.set(true);
	}
	
	public void step(long val)
	{
		stepFlag.addAndGet((int)val);
	}
	
	public void setupCart(Cartridge cart)
	{
		mem.setupCart(cart);
	}
	
	public void readAddress(int addr)
	{
		synchronized(System.out) 
		{
			System.out.println("Value at PPU address " + String.format("0x%04X", addr) + " is " + String.format("0x%02X", mem.read(addr)));
		}
	}
	
	public void writeAddress(int addr, int val)
	{
		mem.write(addr, val);
	}
	
	public byte readMem(int addr)
	{
		return (byte)mem.read(addr);
	}
	
	public void writeMem(int addr, byte val)
	{
		mem.write(addr, Byte.toUnsignedInt(val));
	}
	
	public void print()
	{
		synchronized(System.out) 
		{
			System.out.println("CTRL = " + String.format("0x%02X", ctrl));
			System.out.println("MASK = " + String.format("0x%02X", mask));
			System.out.println("STATUS = " + String.format("0x%02X", status.get()));
			System.out.println("OAMADDR = " + String.format("0x%02X", oamaddr.get()));
			System.out.println("SCROLL = " + String.format("0x%04X", scroll));
			System.out.println("PPUADDR = " + String.format("0x%04X", ppuAddr));
			System.out.println("ODDFRAME = " + oddFrameFlag);
			System.out.println("LOWHIGH = " + lowHighFlag);
		}
	}

	@Override
	public void run() {
		cycle = 0;
		frameStart = cycle;
		while (true)
		{
			if (terminate.get())
			{
				return;
			}
			
			while (debugHold.get()) 
			{
				if (stepFlag.get() > 0)
				{
					stepFlag.addAndGet(-1);
					break;
				}
				
				try
				{
					Thread.sleep(1);
				}
				catch(Exception e) {}
			}
			
			if (reset.get())
			{
				reset();
				reset.set(false);
			}
			else
			{
				execute(cycle - frameStart);
			}
		}
	}
	
	private void reset()
	{
		mask = 0;
		lowHighFlag = false;
		scroll = 0;
		oddFrameFlag = false;
		bus = 0;
	}
	
	private void execute(long tick)
	{
		if (tick >= 340 && oddFrameFlag && Utils.getBit(mask, 3))
		{
			++tick;
		}
		
		if (tick == 0)
		{
			rendering = true;
			frameScroll = scroll;
			statusAnd(0x7f);
			sentNmi = false;
			nametableSelect = -1;
			
			//System.out.println("At frame start X scroll = " + ((frameScroll & 0xff00) >> 8));
			//System.out.println("At frame start Y scroll = " + (frameScroll & 0xff));
		}
		else if (tick == 1)
		{
			statusAnd(0x9f);
		}

		long scanline = tick / 341 - 1;
		externallyReportedScanline = (int)scanline;
		long tickInLine = tick % 341;
		
		boolean displaySprites = Utils.getBit(mask, 4);
		boolean displayBackground = Utils.getBit(mask, 3);
		if (scanline >= 240 || (!displaySprites && !displayBackground))
		{
			rendering = false;
			if (pendingOamWrite.get())
			{
				oamMem[pendingOamWriteAddress] = pendingOamWriteValue;
				incrementOamAddr();
				pendingOamWrite.set(false);
			}
		}
	
		if (scanline < 8)
		{
			doScanline(scanline, tickInLine, false);
		}
		else if (scanline < 232)
		{
			doScanline(scanline, tickInLine, true);
		} 
		else if (scanline < 240)
		{
			doScanline(scanline, tickInLine, false);
		}
		else 
		{
			if (Utils.getBit(status.get(), 7) && !sentNmi && Utils.getBit(ctrl, 7))
			{
				cpu.setNmi();
				sentNmi = true;
			}
			
			if (scanline == 241)
			{
				if (tickInLine == 0)
				{
					//Set NMI
					if (!justReadVbl.get())
					{
						statusOr(0x80);
						if (Utils.getBit(ctrl, 7))
						{
							cpu.setNmi();
							sentNmi = true;
						}
					}
				} 
			}
			else if (scanline == 242)
			{
				if (tickInLine == 0)
				{
					gui.swapBuffers();
				}
			}
			else if (tick == 341 * 262 - 1)
			{
				oddFrameFlag = !oddFrameFlag;
				frameStart = cycle + 1;
			}
		}
		
		justReadVbl.set(false);
		incrementCycle();
	}
	
	private void doScanline(long scanline, long tickInScanline, boolean visible)
	{
		if (tickInScanline < 64)
		{
			oamByte = (byte)0xff;
		}
		else if (tickInScanline < 256)
		{
			oamByte = (byte)0;
		} else if (tickInScanline < 320)
		{
			oamByte = (byte)0xff;
		}
		else
		{
			oamByte = savedInternalOam[0];
		}
		
		if (tickInScanline < 256)
		{	
			int offset = (int)(tickInScanline + 16);
			int nameTableAddress = 0;
			nameTableAddress = getNameTableAddress(scanline, offset);
			nameTableBytes[offset] = (byte)mem.read(nameTableAddress);
			attributeBytes[offset] = getAttributeByte(nameTableAddress);
			patternBytes[offset] = getBgPatternByte(Byte.toUnsignedInt(nameTableBytes[offset]));
			
			if (tickInScanline == 64)
			{
				lineOamAddr = oamaddr.get();
				oamIndex = -1; //we haven't processed 0 yet
				internalOamIndex = 0; //we are writing to first entry of internal (secondary) oam
				
				if (lineOamAddr >= 8)
				{
					int sourceAddress = lineOamAddr & 0xf8;
					System.arraycopy(oamMem, sourceAddress, oamMem, 0, 8);
					oamMem = oamMem; //For thread visibility
				}
			}
			else if (tickInScanline > 64)
			{
				processOam((tickInScanline - 64) / 3, scanline, lineOamAddr);
			}
			
			if (visible)
			{
				displayPixel(scanline, tickInScanline);
			}
			else
			{
				calculatePixel(scanline, tickInScanline);
			}
		} else if (tickInScanline == 256)
		{
			finalizeOam();
			System.arraycopy(internalOam, 0, savedInternalOam, 0, 32);
			spriteIndex = -1;
		} else if (tickInScanline == 260)
		{
			if (mem.getLayout()[0x2000] instanceof Mmc3NametablePort)
			{
				if (Utils.getBit(ctrl, 3) && !Utils.getBit(ctrl, 4))
				{
					if (Mmc3Port.counter == 0)
					{
						if (Mmc3Port.irqEnabled)
						{
							cpu.setIrq();
						}
						
						Mmc3Port.counter = Mmc3Port.irqReloadValue;
					}
					else if (Mmc3Port.needsReload)
					{
						Mmc3Port.counter = Mmc3Port.irqReloadValue;
					} else
					{
						--Mmc3Port.counter;
					}
					
					Mmc3Port.needsReload = false;
				}
			}
		}
		else if (tickInScanline == 320)
		{
			if (scanline < 240)
			{
				oamaddr.set(0);
			}
		} else if (tickInScanline == 324)
		{
			if (mem.getLayout()[0x2000] instanceof Mmc3NametablePort)
			{
				if (!Utils.getBit(ctrl, 3) && Utils.getBit(ctrl, 4))
				{
					if (Mmc3Port.counter == 0)
					{
						if (Mmc3Port.irqEnabled)
						{
							cpu.setIrq();
						}
						
						Mmc3Port.counter = Mmc3Port.irqReloadValue;
					}
					else if (Mmc3Port.needsReload)
					{
						Mmc3Port.counter = Mmc3Port.irqReloadValue;
					} else
					{
						--Mmc3Port.counter;
					}
					
					Mmc3Port.needsReload = false;
				}
			}
		}
		
		if (tickInScanline >= 257 && tickInScanline < 321)
		{
			int y = (int)(scanline + 1);	
			prepSpritesForNextLine(y, (int)((tickInScanline - 257) / 8));
		}
		else if (tickInScanline >= 321 && tickInScanline < 337)
		{
			int offset = (int)(tickInScanline - 321);
			int nameTableAddress = 0;
			nameTableAddress = getNameTableAddress(scanline + 1, offset);
			nameTableBytes[offset] = (byte)mem.read(nameTableAddress);
			attributeBytes[offset] = getAttributeByte(nameTableAddress);
			patternBytes[offset] = getBgPatternByte(Byte.toUnsignedInt(nameTableBytes[offset]));
		} 
	}
	
	private void prepSpritesForNextLine(int y, int index)
	{	
		if (index <= spriteIndex)
		{
			//Already processed
			return;
		}
		
		spriteIndex = (int)index;
		if (index == 0)
		{
			for (int i = 0; i < 256; ++i)
			{
				spriteColors[i] = -1;
			}
		}
			
		long activeSprite = getSprite(index);
		if (activeSprite != 0x00000000ffffffffL)
		{
			int xMin = (int)(activeSprite & 0xff);
			int xMax = (xMin + 7) & 0xff;
			
			if (xMin < xMax)
			{
				int spriteAddress = getSpriteAddress(activeSprite, xMin, y);
				int spriteByte1 = mem.read(spriteAddress);
				int spriteByte2 = mem.read(spriteAddress + 8);
				for (int i = xMin; i <= xMax; ++i)
				{
					int temp = getSpriteColor(activeSprite, i, y, spriteByte1, spriteByte2);
					if (spriteColors[i] == -1)
					{
						spriteColors[i] = temp;
					}
				}
			}
			else
			{
				int spriteAddress = getSpriteAddress(activeSprite, xMin, y);
				int spriteByte1 = mem.read(spriteAddress);
				int spriteByte2 = mem.read(spriteAddress + 8);
				for (int i = xMin; i < 256; ++i)
				{
					int temp = getSpriteColor(activeSprite, i, y, spriteByte1, spriteByte2);
					if (spriteColors[i] == -1)
					{
						spriteColors[i] = temp;
					}
				}
				
				for (int i = 0; i <= xMax; ++i)
				{
					int temp = getSpriteColor(activeSprite, i, y, spriteByte1, spriteByte2);
					if (spriteColors[i] == -1)
					{
						spriteColors[i] = temp;
					}
				}
			}
		}
	}
	
	private int calculatePixel(long scanline, long tickInScanline)
	{
		int x = (int)tickInScanline;
		int y = (int)scanline;
		
		int color = getBgFillColor();
		boolean displaySprites = Utils.getBit(mask, 4);
		boolean displayBackground = Utils.getBit(mask, 3);
		boolean spritesInLeft8 = Utils.getBit(mask, 2);
		boolean backgroundInLeft8 = Utils.getBit(mask, 1);
		
		if (!spritesInLeft8 && x < 8)
		{
			displaySprites = false;
		}
		
		if (!backgroundInLeft8 && x < 8)
		{
			displayBackground = false;
		}
		
		if (displaySprites)
		{
			long activeSprite = getActiveSprite(x, y);
			
			if (activeSprite != 0x00000000ffffffffL)
			{
				if ((activeSprite & 0x0000000000001000L) != 0)
				{
					spriteZeroFlag = true;
				}
				
				if ((activeSprite & 0x0000000000002000L) != 0)
				{
					//It's a background sprite
					int spriteColor = spriteColors[x];
					if (spriteColor != -1)
					{
						color = spriteColor;
					}
					
					if (displayBackground)
					{
						int bgColor = getBgColorAtCoord(x, y);
						if (bgColor != -1)
						{
							color = bgColor;
						}
						
						if (spriteZeroFlag && spriteColor != -1 && 
								(activeSprite & 0xff) != 0xff && x < 255 &&
								((activeSprite & 0x00000000ff000000L) >> 24) < 239)
						{
							statusOr(0x40);
							//System.out.println("Sprite zero flag set on scanline " + scanline);
						}
					}
				}
				else
				{
					//Foreground sprite
					int bgColor = -1;
					if (displayBackground)
					{
						bgColor = getBgColorAtCoord(x, y);
						if (bgColor != -1)
						{
							color = bgColor;
						}
					}
					
					int spriteColor = spriteColors[x];
					if (spriteColor != -1)
					{
						color = spriteColor;
						color = spriteColor;
					}
					
					if (spriteZeroFlag && spriteColor != -1 && 
							(activeSprite & 0xff) != 0xff && x < 255 &&
							((activeSprite & 0x00000000ff000000L) >> 24) < 239)
					{
						statusOr(0x40);
						//System.out.println("Sprite zero flag set on scanline " + scanline);
					}
				}
			}
			else
			{
				//No sprite, so just need to check background color
				if (displayBackground)
				{
					int bgColor = getBgColorAtCoord(x, y);
					if (bgColor != -1)
					{
						color = bgColor;
					}
				}
			}
		}
		else
		{
			//No sprite, so just need to check background color
			if (displayBackground)
			{
				int bgColor = getBgColorAtCoord(x, y);
				if (bgColor != -1)
				{
					color = bgColor;
				}
			}
		}
		
		spriteZeroFlag = false;
		return color;
	}
	
	private long getSprite(int index)
	{
		int b0 = Byte.toUnsignedInt(savedInternalOam[index * 4]);
		int b1 = Byte.toUnsignedInt(savedInternalOam[index * 4 + 1]);
		int b2 = Byte.toUnsignedInt(savedInternalOam[index * 4 + 2]);
		int b3 = Byte.toUnsignedInt(savedInternalOam[index * 4 + 3]);
		return (((long)b0) << 24) + (((long)b1) << 16) + (((long)b2) << 8) + (long)b3;
	}
	
	private long getActiveSprite(int x, int y)
	{
		for (int i = 0; i < 8; ++i)
		{
			int b0 = Byte.toUnsignedInt(savedInternalOam[i * 4]);
			int b1 = Byte.toUnsignedInt(savedInternalOam[i * 4 + 1]);
			int b2 = Byte.toUnsignedInt(savedInternalOam[i * 4 + 2]);
			int b3 = Byte.toUnsignedInt(savedInternalOam[i * 4 + 3]);
			
			if (b0 != 0xff || b1 != 0xff || b2 != 0xff || b3 != 0xff)
			{
				int xMin = b3;
				int xMax = (xMin + 7) & 0xff;
				
				if ((x >= xMin && x <= xMax) || (xMax < xMin && (x >= xMin || x <= xMax)))
				{
					return (((long)b0) << 24) + (((long)b1) << 16) + (((long)b2) << 8) + (long)b3;
				}
			}
		}
		
		return 0x00000000ffffffffL;
	}
	
	private int getSpriteAddress(long activeSprite, int x, int y)
	{
		int b0 = (int)((activeSprite >> 24) & 0xff);
		int b1 = (int)((activeSprite >> 16) & 0xff);
		int b2 = (int)((activeSprite >> 8) & 0xff);
		int b3 = (int)(activeSprite & 0xff);
		
		int yMin = (b0 + 1) & 0xff;
		int xMin = b3;
		
		int tileAddress;
		boolean doubleSprite = Utils.getBit(ctrl, 5);
		boolean hFlip = Utils.getBit(b2, 6);
		boolean vFlip = Utils.getBit(b2, 7);
		
		int tileXOffset = (x - xMin) & 0xff;
		int tileYOffset = (y - yMin) & 0xff;
		int tileNum = 0;
		
		if (doubleSprite)
		{
			tileNum = b1 & 0xfe;
			
			if (Utils.getBit(b1, 0))
			{
				tileAddress = 0x1000;
			}
			else
			{
				tileAddress = 0x0000;
			}
			
			if (tileYOffset >= 8)
			{
				++tileNum;
				tileYOffset -= 8;
			}
			
			if (vFlip)
			{
				if (Utils.getBit(tileNum, 0))
				{
					--tileNum;
				}
				else
				{
					++tileNum;
				}
				
				tileYOffset = 7 - tileYOffset;
			}
		}
		else
		{
			tileNum = b1;
			
			if (Utils.getBit(ctrl, 3))
			{
				tileAddress = 0x1000;
			}
			else
			{
				tileAddress = 0x0000;
			}
			
			if (vFlip)
			{
				tileYOffset = 7 - tileYOffset;
			}
		}
		
		if (hFlip)
		{
			tileXOffset = 7 - tileXOffset;
		}
		
		tileAddress += 16 * tileNum;
		tileAddress += tileYOffset;
		return tileAddress;
	}
	
	private int getSpriteColor(long activeSprite, int x, int y, int spriteByte1, int spriteByte2)
	{
		int b0 = (int)((activeSprite >> 24) & 0xff);
		int b1 = (int)((activeSprite >> 16) & 0xff);
		int b2 = (int)((activeSprite >> 8) & 0xff);
		int b3 = (int)(activeSprite & 0xff);
		
		int yMin = (b0 + 1) & 0xff;
		int xMin = b3;
		
		boolean doubleSprite = Utils.getBit(ctrl, 5);
		boolean hFlip = Utils.getBit(b2, 6);
		boolean vFlip = Utils.getBit(b2, 7);
		int palette = b2 & 0x03;
		
		int tileXOffset = (x - xMin) & 0xff;
		int tileYOffset = (y - yMin) & 0xff;
		int tileNum = 0;
		
		if (doubleSprite)
		{
			tileNum = b1 & 0xfe;
			
			if (tileYOffset >= 8)
			{
				++tileNum;
				tileYOffset -= 8;
			}
			
			if (vFlip)
			{
				if (Utils.getBit(tileNum, 0))
				{
					--tileNum;
				}
				else
				{
					++tileNum;
				}
				
				tileYOffset = 7 - tileYOffset;
			}
		}
		else
		{
			tileNum = b1;
			
			if (vFlip)
			{
				tileYOffset = 7 - tileYOffset;
			}
		}
		
		if (hFlip)
		{
			tileXOffset = 7 - tileXOffset;
		}
		
		int pattern = 0;
		
		if (Utils.getBit(spriteByte1, 7 - tileXOffset))
		{
			pattern = 1;
		}
		
		if (Utils.getBit(spriteByte2, 7 - tileXOffset))
		{
			pattern += 2;
		}
		
		return getSpriteColor(palette, pattern);
	}
	
	private int getBgColorAtCoord(int x, int y)
	{
		return getBgColor(Byte.toUnsignedInt(attributeBytes[x]), Byte.toUnsignedInt(patternBytes[x]));
	}
	
	private void displayPixel(long scanline, long tickInScanline)
	{
		int color = calculatePixel(scanline, tickInScanline);
		int x = (int)tickInScanline;
		int y = (int)scanline;
		gui.write(x, y, color);
	}
	
	private void finalizeOam()
	{
		for (; internalOamIndex < 8; ++internalOamIndex)
		{
			internalOam[internalOamIndex * 4] = (byte)0xff;
			internalOam[internalOamIndex * 4 + 1] = (byte)0xff;
			internalOam[internalOamIndex * 4 + 2] = (byte)0xff;
			internalOam[internalOamIndex * 4 + 3] = (byte)0xff;
		}
	}
	
	private void processOam(long num, long scanline, int oamAddr)
	{
		if (num <= oamIndex)
		{
			//Already processed
			return;
		}
		
		oamIndex = (int)num;
		++scanline;
		
		if (oamAddr + 4 * oamIndex + 3 >= 256)
		{
			return;
		}
		
		boolean spriteZero = (oamIndex == 0);
		
		long yMin = getSpriteYCoord((int)num, oamAddr);
		long yMax = (yMin + 7) & 0xff;
		
		if (Utils.getBit(ctrl, 5))
		{
			yMax += 8;
		}

		if ((scanline >= yMin && scanline <= yMax) || (yMax < yMin && (scanline >= yMin || scanline <= yMax)))
		{
			//Hit
			if (internalOamIndex < 8)
			{
				internalOam[internalOamIndex * 4] = oamMem[(int)(num * 4 + oamAddr)];
				internalOam[internalOamIndex * 4 + 1] = oamMem[(int)(num * 4 + 1 + oamAddr)];
				internalOam[internalOamIndex * 4 + 2] = oamMem[(int)(num * 4 + 2 + oamAddr)];
				internalOam[internalOamIndex * 4 + 3] = oamMem[(int)(num * 4 + 3 + oamAddr)];
				
				if (spriteZero)
				{
					internalOam[2] |= 0x10;
				}
				
				++internalOamIndex;
			} else if (internalOamIndex == 8)
			{
				//Set sprite overflow
				statusOr(0x20);
			}
		}
	}
	
	private byte getBgPatternByte(int val)
	{
		boolean bank = Utils.getBit(ctrl, 4);
		int addr = 0;
		if (bank)
		{
			addr = 0x1000;
		}
		
		addr += 16 * val;
		addr += (savedY & 0x07);
		
		int temp = 0;
		
		if (Utils.getBit(mem.read(addr), 7 - (savedX & 0x07)))
		{
			temp = 1;
		}
		
		addr += 8;
		if (Utils.getBit(mem.read(addr), 7 - (savedX & 0x07)))
		{
			temp += 2;
		}
		
		return (byte)temp;
	}
	
	private int getNameTableAddress(long scanline, long offset)
	{
		//Get base nametable
		int addr = getBaseNameTableAddress();
		
		//Apply scroll to offset and scanline
		return getNameTableAddress(addr, scanline, offset, getHorizontalScroll(), getVerticalScroll());
	}
	
	private byte getAttributeByte(int nameAddr)
	{
		int addr = getAttributeByteAddress(nameAddr);
		int temp = mem.read(addr);
		boolean left = ((savedX >> 4) & 0x01) == 0;
		boolean top = ((savedY >> 4) & 0x01) == 0;
		
		if (left)
		{
			if (top)
			{
				return (byte)(temp & 0x03);
			}
			else
			{
				return (byte)((temp >> 4) & 0x03);
			}
		}
		else
		{
			if (top)
			{
				return (byte)((temp >> 2) & 0x03);
			}
			else
			{
				return (byte)((temp >> 6) & 0x03);
			}
		}
	}
	
	private int getAttributeByteAddress(int nameAddr)
	{
		int base = nameAddr & 0xfc00;
		int offset = nameAddr & 0x3ff;
		int xTile = offset & 0x1f;
		int yTile = offset >> 5;
		int retval = base + 0x3c0 + xTile/4 +(yTile/4) * 8;
		return retval;
	}
	
	private int getBaseNameTableAddress()
	{
		int temp;
		
		if (nametableSelect == -1)
		{
			temp = ctrl & 0x03;
		}
		else
		{
			temp = nametableSelect;
		}
		
		int retval = 0x2000 + temp * 0x400;
		return retval;
	}
	
	private int getNameTableAddress(int base, long y, long x, int xScroll, int yScroll)
	{		
		x += xScroll;
		y += yScroll;
		
		int address = base;
		
		if (x >= 256)
		{
			x -= 256;
			
			if (address == 0x2000 || address == 0x2800)
			{
				address += 0x400;
			}
			else
			{
				address -= 0x400;
			}
		}
		
		if (y >= 240)
		{
			y -= 240;
			if (address == 0x2000 || address == 0x2400)
			{
				address += 0x800;
			}
			else
			{
				address -= 0x800;
			}
		}
		
		address += (int)(x/8 + (y/8) * 32);
		savedX = (int)x;
		savedY = (int)y;
		return address;
	}
	
	private int getHorizontalScroll()
	{
		//High is horizontal
		int retval = (scroll & 0xff00) >> 8;
		
		/*
		if (retval != 0)
		{
			System.out.println("Using x scroll of " + retval);
		}
		*/
		
		return retval;
	}
	
	private int getVerticalScroll()
	{
		//Low is vertical and is fixed at start of frame
		int retval = frameScroll & 0xff;
		
		/*
		if (retval != 0)
		{
			System.out.println("Using y scroll of " + retval);
		}
		*/
		
		return retval;
	}
	
	public void incrementCycle()
	{
		clock.setPpuExpectedCycle(++cycle);
		long expected = clock.getCpuExpectedCycle();
		while (expected < cycle) 
		{
			if (terminate.get())
			{
				return;
			}
			
			expected = clock.getCpuExpectedCycle();
		}
		
		if (expected - cycle > 3)
		{
			System.out.println("P" + (expected - cycle));
		}
		
		if (overage > 0)
		{
			--overage;
			return;
		}
		
		long current = clock.cycle();
		while (current < cycle) {
			current = clock.cycle();
		}
		
		overage += (current - cycle);
	}
	
	private int getBgFillColor()
	{
		int temp = colorLookup[mem.read(0x3f00) & 0x3f];
		return adjustColor(temp);
	}
	
	private int getBgColor(int palette, int color)
	{
		if (color == 0)
		{
			return -1;
		}
		
		int temp = colorLookup[mem.read(0x3f00 + palette * 0x04 + color) & 0x3f];
		return adjustColor(temp);
	}
	
	private int getSpriteColor(int palette, int color)
	{
		if (color == 0)
		{
			return -1;
		}
		
		int temp = colorLookup[mem.read(0x3f10 + palette * 0x04 + color) & 0x3f];
		return adjustColor(temp);
	}
	
	private int makeRGB(int r, int g, int b)
	{
		return (r << 16) + (g << 8) + b;
	}
	
	private int adjustColor(int color)
	{
		if (Utils.getBit(mask, 5))
		{
			int r = (color >> 16) & 0xff;
			int g = (color >> 8) & 0xff;
			int b = color & 0xff;
			g = (int)(0.63 * g);
			b = (int)(0.63 * b);
			color = makeRGB(r, g, b);
		} 
		
		if (Utils.getBit(mask, 6))
		{
			int r = (color >> 16) & 0xff;
			int g = (color >> 8) & 0xff;
			int b = color & 0xff;
			r = (int)(0.63 * g);
			b = (int)(0.63 * b);
			color = makeRGB(r, g, b);
		}
		
		if (Utils.getBit(mask, 7))
		{
			int r = (color >> 16) & 0xff;
			int g = (color >> 8) & 0xff;
			int b = color & 0xff;
			r = (int)(0.63 * g);
			g = (int)(0.63 * b);
			color = makeRGB(r, g, b);
		}
		
		return color;
	}
	
	int getSpriteYCoord(int num, int oamAddr)
	{
		return 0xff & (Byte.toUnsignedInt(oamMem[num * 4 + oamAddr]) + 1);
	}
	
	int getTileByte(int num, int oamAddr)
	{
		//Interpretation depends on 8x8 vs 8x16
		return Byte.toUnsignedInt(oamMem[num * 4 + 1 + oamAddr]);
	}
	
	int getTileMetadataByte(int num, int oamAddr)
	{
		return Byte.toUnsignedInt(oamMem[num * 4 + 2 + oamAddr]);
	}
	
	int getSpriteXCoord(int num, int oamAddr)
	{
		return Byte.toUnsignedInt(oamMem[num * 4 + 3 + oamAddr]);
	}
}
