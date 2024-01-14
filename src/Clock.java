//Clock for syncing PPU and CPU
//The APU isn't synced as tightly
//It can run multiple cycles at once if it is behind
//The CPU and PPU never wait for the APU
//Whereas they both make sure they never get too far ahead of each other

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
