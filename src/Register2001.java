
public class Register2001 implements MemoryPort {
	private PPU ppu;

	public Register2001(PPU ppu)
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
		ppu.setMask(Byte.toUnsignedInt(val));
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2001");
	}

}
