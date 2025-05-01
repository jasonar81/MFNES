import java.io.Serializable;

public class Mmc3Data implements Serializable {
	public boolean init = false;
	public boolean prgMode = false;
	public boolean chrMode = false;
	public int command = 0;
	public boolean mirroring = false;
	public volatile int irqReloadValue = 0;
	public volatile boolean needsReload = false;
	public volatile boolean irqEnabled = false;
	public volatile int counter = 0;
	public MemoryPort[] nametableData = new MemoryPort[2048];
}
