
public class Register4014 implements MemoryPort {
	private CPU cpu;

	public Register4014(CPU cpu)
	{
		this.cpu = cpu;
	}
	
	@Override
	public byte read() {
		return (byte)cpu.getPpu().getBus();
	}

	@Override
	public void write(byte val) {
		//System.out.println("Wrote " + String.format("0x%02X", Byte.toUnsignedInt(val)) + " to 0x4014");
		cpu.getPpu().setBus(Byte.toUnsignedInt(val));
		
		int oamAddress = cpu.getPpu().getOamAddr();
		int bytesToTransfer = 256 - oamAddress;
		int cpuAddress = Byte.toUnsignedInt(val) << 8;
		int cycles = 0;
		cpu.incrementCycle(2);
		cycles += 2;
		
		for (int i = 0; i < bytesToTransfer; ++i)
		{
			cpu.getPpu().setOamByte(oamAddress++, (byte)cpu.memRead(cpuAddress++));
			cpu.incrementCycle();
			cycles += 2;
		}
		
		cpu.incrementCycle(514 - cycles);
	}

}
