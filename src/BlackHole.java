
public class BlackHole implements MemoryPort {

	public BlackHole()
	{
	}
	
	@Override
	public byte read() {
		return 0;
	}

	@Override
	public void write(byte val) {
	}
	
}
