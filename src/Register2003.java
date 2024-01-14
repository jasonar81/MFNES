
public class Register2003 implements MemoryPort {
	private PPU ppu;

	public Register2003(PPU ppu)
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
		ppu.setOamAddr(Byte.toUnsignedInt(val));
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2003");
	}

}
