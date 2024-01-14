
public class ControllerEvent {
	private long cycle;
	private boolean down;
	private int code;

	public ControllerEvent(long cycle, boolean down, int code)
	{
		this.cycle = cycle;
		this.down = down;
		this.code = code;
	}
	
	public long getCycle()
	{
		return cycle;
	}
	
	public boolean getDown()
	{
		return down;
	}
	
	public int getCode()
	{
		return code;
	}
	
	public void swapPressed()
	{
		down = !down;
	}
	
	public void setKey(int key)
	{
		this.code = key;
	}
}
