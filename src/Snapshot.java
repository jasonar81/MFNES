import java.io.Serializable;

public class Snapshot implements Serializable {
	public Clock clock;
	public Memory ppuMem;
	public PPU ppu;
	public Memory cpuMem;
	public CPU cpu;
	public APU apu;
	public int count;
	public int state;
}
