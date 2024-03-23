//Clock for syncing PPU and CPU
//The APU isn't synced as tightly
//It can run multiple cycles at once if it is behind
//The CPU and PPU never wait for the APU
//Whereas they both make sure they never get too far ahead of each other
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Clock implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	public static double periodNanos = 1000000000.0 / 5369317.5;
	private volatile long start;
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
	
	public void sync1()
	{
		
	}
}
