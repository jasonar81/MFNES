
public class Register2002 implements MemoryPort {
	private PPU ppu;

	public Register2002(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		int temp = ppu.getStatus() & 0xe0;
		int val = (0x1f & ppu.getBus());
		val |= temp;
		ppu.setBus(val);
		ppu.clearLowHigh();
		ppu.statusAnd(0x7f);
		ppu.setJustReadVbl();
		//System.out.println("Read " + String.format("0x%02X", val) + " from 0x2002");
		return (byte)val;
	}

	@Override
	public void write(byte val) {
		ppu.setBus(Byte.toUnsignedInt(val));
	}

}
