import java.io.Serializable;

public class Snapshot implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	public Clock clock;
	public Memory ppuMem;
	public PPU ppu;
	public Memory cpuMem;
	public CPU cpu;
	public APU apu;
	public MultiTreeController controller;
}
