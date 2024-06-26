//Used by mappers
import java.io.Serializable;

public class Latch1Port extends TwoStateMemoryPort implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private Memory mem;
	private int addr;
	boolean setting;
	
	public Latch1Port(Memory mem, int addr, boolean setting)
	{
		super(mem, 2, false);
		this.addr = addr;
		this.mem = mem;
		this.setting = setting;
	}
	
	@Override
	public byte read() {
		byte retval = super.read();
		if (setting)
		{
			mem.writeControl(Utils.setBit(mem.getControl(), 1));
		}
		else
		{
			mem.writeControl(Utils.clearBit(mem.getControl(), 1));
		}
		
		return retval;
	}
	
}
