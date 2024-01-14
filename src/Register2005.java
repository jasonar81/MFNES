
public class Register2005 implements MemoryPort {
	private PPU ppu;

	public Register2005(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		return (byte)ppu.getBus();
	}

	@Override
	public void write(byte val) {
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2005");
		ppu.setBus(Byte.toUnsignedInt(val));
		
		if (!ppu.getLowHigh())
		{
			//System.out.println("X scroll = " + Byte.toUnsignedInt(val) + " at scanline " + ppu.getScanline());
			int temp = ppu.getScroll();
			temp &= 0xff;
			temp = (Byte.toUnsignedInt(val) << 8) + temp;
			ppu.setScroll(temp);
			ppu.flipLowHigh();
		}
		else
		{
			//System.out.println("Y scroll = " + Byte.toUnsignedInt(val));
			int temp = ppu.getScroll();
			temp &= 0xff00;
			temp = Byte.toUnsignedInt(val) + temp;
			ppu.setScroll(temp);
			ppu.flipLowHigh();
		}
	}

}