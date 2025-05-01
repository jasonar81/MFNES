
public class Register2000 implements MemoryPort {
	private PPU ppu;
	private int val;

	public Register2000(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		return (byte)ppu.getBus();
	}

	@Override
	public void write(byte val) {
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2000");
		ppu.setBus(Byte.toUnsignedInt(val));
		ppu.setCtrl(Byte.toUnsignedInt(val));
		
		/*
		if ((val & 0x03) != this.val)
		{
			System.out.println("Nametable select = " + (val & 0x03) + " at scanline " + ppu.getScanline());
			System.out.println("Full value is " + val);
			this.val = (val & 0x03);
		}
		*/
	}

}
