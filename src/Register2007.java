
public class Register2007 implements MemoryPort {
	private PPU ppu;
	byte buffer;

	public Register2007(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		int addr = ppu.getPpuAddr();
		if (Utils.getBit(ppu.getCtrl(), 2))
		{
			ppu.setPpuAddr((addr + 32) & 0xffff);
		}
		else
		{
			ppu.setPpuAddr((addr + 1) & 0xffff);
		}
		
		if (addr < 0x3f00)
		{
			byte temp = buffer;
			buffer = ppu.readMem(addr);
			ppu.setBus(Byte.toUnsignedInt(temp));
			//System.out.println("Read " + String.format("0x%02X", Byte.toUnsignedInt(temp)) + " from 0x2007");
			return temp;
		}
		
		byte retval = ppu.readMem(addr);
		ppu.setBus(Byte.toUnsignedInt(retval));
		buffer = ppu.readMem(addr - 0x1000);
		
		if (Utils.getBit(ppu.getMask(), 0))
		{
			return (byte)(retval & 0x30);
		}
		
		//System.out.println("Read " + String.format("0x%02X", Byte.toUnsignedInt(buffer)) + " from 0x2007");
		return retval;
	}

	@Override
	public void write(byte val) {
		ppu.setBus(Byte.toUnsignedInt(val)); 
		int addr = ppu.getPpuAddr();	
		if (Utils.getBit(ppu.getCtrl(), 2))
		{
			ppu.setPpuAddr((addr + 32) & 0xffff);
		}
		else
		{
			ppu.setPpuAddr((addr + 1) & 0xffff);
		}
		
		ppu.writeMem(addr, val);
		
		/*
		if (addr >= 0x2400 && addr <= 0x4000)
		{
			System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2007");
			System.out.println("Address is " + String.format("0x%04X", addr));
		}
		*/
	}

}
