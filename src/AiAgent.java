
public interface AiAgent {
	public void setDone(long totalTime);
	public void setDeath(long cycle);
	public void progress(long cycle);
}
