
public class Register2004 implements MemoryPort {
	private PPU ppu;

	public Register2004(PPU ppu)
	{
		this.ppu = ppu;
	}
	
	@Override
	public byte read() {
		byte val = ppu.getOamByte();
		ppu.setBus(Byte.toUnsignedInt(val));
		//System.out.println("Read " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " from 0x2004");
		return val;
	}

	@Override
	public void write(byte val) {
		ppu.setBus(Byte.toUnsignedInt(val));
		
		while (ppu.pendingOamWrite())
		{}
		
		ppu.setPendingOamWriteValue(val);
		ppu.setPendingOamWrite();
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x2004");
	}

}
