//Used by mappers
import java.io.Serializable;

public class Latch0Port extends TwoStateMemoryPort implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private Memory mem;
	private int addr;
	boolean setting;
	
	public Latch0Port(Memory mem, int addr, boolean setting)
	{
		super(mem, 1, false);
		this.addr = addr;
		this.mem = mem;
		this.setting = setting;
	}
	
	@Override
	public byte read() {
		byte retval = super.read();
		
		if (setting)
		{
			mem.writeControl(Utils.setBit(mem.getControl(), 0));
		}
		else
		{
			mem.writeControl(Utils.clearBit(mem.getControl(), 0));
		}
		
		return retval;
	}
	
}
