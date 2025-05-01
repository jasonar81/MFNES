//The interface for controller, display, and audio providers

import java.util.ArrayList;

public interface GUI extends Runnable {
	public void write(int x, int y, int rgb);
	public void swapBuffers();
	public boolean getA();
	public boolean getB();
	public boolean getSelect();
	public boolean getStart();
	public boolean getUp();
	public boolean getDown();
	public boolean getLeft();
	public boolean getRight();
	public boolean getA2();
	public boolean getB2();
	public boolean getSelect2();
	public boolean getStart2();
	public boolean getUp2();
	public boolean getDown2();
	public boolean getLeft2();
	public boolean getRight2();
	public void writeAudioData(double data);
	public void setCpu(CPU cpu);
	public void stopRecording();
	public void record(String filename);
	public void playRecording(String filename);
	public void setClock(Clock clock);
	public void terminate();
	public void setEventList(ArrayList<ControllerEvent> events);
	public void setAgent(AiAgent agent);
	public long getTotalBPresses();
	public int[] getDisplayData();
}
