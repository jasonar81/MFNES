import java.io.Serializable;

public class Mmc1Data implements Serializable {
	public long previousWriteCycle = 0;
	public int writeNumber = 0;
	public boolean twoBanks = false;
	public int prgBankMode = 0;
	public int nametableMode = 0;
	public MemoryPort[] nametableData = new MemoryPort[2048];
}
