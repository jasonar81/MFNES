import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Register4016 implements MemoryPort {
	private GUI gui;
	public boolean hold = false;
	public int counter = 0;
	public int counter2 = 0;
	private CPU cpu;
	
	public Register4016(GUI gui, CPU cpu)
	{
		this.gui = gui;
		this.cpu = cpu;
	}
	
	@Override
	public byte read() {
		boolean read = false;
		switch (counter)
		{
		case 0:
			read = gui.getA();
			break;
		case 1:
			read = gui.getB();
			break;
		case 2:
			read = gui.getSelect();
			break;
		case 3:
			read = gui.getStart();
			break;
		case 4:
			read = gui.getUp();
			break;
		case 5:
			read = gui.getDown();
			break;
		case 6:
			read = gui.getLeft();
			break;
		case 7:
			read = gui.getRight();
			break;
		default:
			read = true;
		}
		
		if (!hold)
		{
			++counter;
		}
		
		if (read)
		{
			return 0x41;
		}
		
		return 0x40;
	}

	@Override
	public void write(byte val) {
		hold = ((val & 0x01) != 0);
		if (hold)
		{
			counter = 0;
			counter2 = 0;
		}
	}
}
