
public class ClearElsewhereMaxValuePort implements MemoryPort {
	private byte val;
	private ClearablePort other;
	private boolean onZero;
	
	public ClearElsewhereMaxValuePort(MemoryPort other, boolean onZero)
	{
		val = 0;
		this.other = (ClearablePort)other;
		this.onZero = onZero;
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		if (val != this.val)
		{
			if (onZero || val != 0)
			{
				other.clearMaxValue();
			}
		}
		
		this.val = val;
	}
	
}