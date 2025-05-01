
//Memory address that is backed by 2 bytes of storage and can be switched between them

public class TwoStateMemoryPort implements MemoryPort {
	Memory ppuMem;
	int flag;
	byte val1;
	byte val2;
	boolean writable;
	
	TwoStateMemoryPort(Memory ppuMem, int flag, boolean writable)
	{
		this.ppuMem = ppuMem;
		this.flag = flag;
		this.writable = writable;
	}
	
	@Override
	public byte read() {		
		if ((ppuMem.getControl() & flag) == 0)
		{
			return val1;
		}
		else
		{
			return val2;
		}
	}

	@Override
	public void write(byte val) {
		if (writable)
		{
			if ((ppuMem.getControl() & flag) == 0)
			{
				val1 = val;
			}
			else
			{
				val2 = val;
			}
		}
	}

	public void setValue1(byte val)
	{
		val1 = val;
	}
	
	public void setValue2(byte val)
	{
		val2 = val;
	}
}
