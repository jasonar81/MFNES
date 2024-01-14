
public class Clock {
	public static double periodNanos = 1000000000.0 / 5369317.5;
	private long start;
	private volatile long ppuExpected = 0;
	private volatile long cpuExpected = 0;
	
	public Clock()
	{
		start = System.nanoTime();
	}
	
	public long cycle()
	{
		return (long)((System.nanoTime() - start) / periodNanos);
	}
	
	public void setPpuExpectedCycle(long val)
	{
		ppuExpected = val;
	}
	
	public long getPpuExpectedCycle()
	{
		return ppuExpected;
	}
	
	public void setCpuExpectedCycle(long val)
	{
		cpuExpected = val;
	}
	
	public long getCpuExpectedCycle()
	{
		return cpuExpected;
	}
}
