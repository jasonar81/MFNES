
public class Register2006 implements MemoryPort {
	private PPU ppu;
	private int partial;

	public Register2006(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		return (byte)ppu.getBus();
	}

	@Override
	public void write(byte val) {
		ppu.setBus(Byte.toUnsignedInt(val));
		
		int temp = 0;
		if (!ppu.getLowHigh())
		{
			temp = ppu.getPpuAddr();
			temp &= 0xff;
			temp = (Byte.toUnsignedInt(val) << 8) + temp;
			partial = temp;
			ppu.flipLowHigh();
			
			temp = ppu.getScroll();
			if (Utils.getBit(val, 1))
			{
				temp = Utils.setBit(temp, 7);
			}
			else
			{
				temp = Utils.clearBit(temp, 7);
			}
			
			if (Utils.getBit(val, 0))
			{
				temp = Utils.setBit(temp, 6);
			}
			else
			{
				temp = Utils.clearBit(temp, 6);
			}
			
			if (Utils.getBit(val, 6))
			{
				temp = Utils.setBit(temp, 2);
			}
			else
			{
				temp = Utils.clearBit(temp, 2);
			}
			
			if (Utils.getBit(val, 5))
			{
				temp = Utils.setBit(temp, 1);
			}
			else
			{
				temp = Utils.clearBit(temp, 1);
			}
			
			if (Utils.getBit(val, 4))
			{
				temp = Utils.setBit(temp, 0);
			}
			else
			{
				temp = Utils.clearBit(temp, 0);
			}
			
			temp = Utils.clearBit(temp, 2);
			
			int nametableSelect = ((val & 0x0c) >> 2);
			ppu.setNametableSelect(nametableSelect);
		}
		else
		{
			temp = partial;
			temp &= 0xff00;
			temp = Byte.toUnsignedInt(val) + temp;
			ppu.setPpuAddr(temp);
			ppu.flipLowHigh();
			
			//System.out.println("PPU address set to " + String.format("0x%04X", temp));
			
			temp = ppu.getScroll();
			if (Utils.getBit(val, 7))
			{
				temp = Utils.setBit(temp, 5);
			}
			else
			{
				temp = Utils.clearBit(temp, 5);
			}
			
			if (Utils.getBit(val, 6))
			{
				temp = Utils.setBit(temp, 4);
			}
			else
			{
				temp = Utils.clearBit(temp, 4);
			}
			
			if (Utils.getBit(val, 5))
			{
				temp = Utils.setBit(temp, 3);
			}
			else
			{
				temp = Utils.clearBit(temp, 3);
			}
			
			if (Utils.getBit(val, 4))
			{
				temp = Utils.setBit(temp, 15);
			}
			else
			{
				temp = Utils.clearBit(temp, 15);
			}
			
			if (Utils.getBit(val, 3))
			{
				temp = Utils.setBit(temp, 14);
			}
			else
			{
				temp = Utils.clearBit(temp, 14);
			}
			
			if (Utils.getBit(val, 2))
			{
				temp = Utils.setBit(temp, 13);
			}
			else
			{
				temp = Utils.clearBit(temp, 13);
			}
			
			if (Utils.getBit(val, 1))
			{
				temp = Utils.setBit(temp, 12);
			}
			else
			{
				temp = Utils.clearBit(temp, 12);
			}
			
			if (Utils.getBit(val, 0))
			{
				temp = Utils.setBit(temp, 11);
			}
			else
			{
				temp = Utils.clearBit(temp, 11);
			}
			
			ppu.setFrameScroll(temp);
		}
		
		//System.out.println("After $2006 write X scroll = " + ((temp & 0xff00) >> 8));
		//System.out.println("After $2006 write Y scroll = " + (temp & 0xff));
		ppu.setScroll(temp);
		
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2006");
		//System.out.println("Address is " + String.format("0x%04X", temp));
	}

}