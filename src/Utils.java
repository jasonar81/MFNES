import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class Utils {
	static int makeUnsigned(int low, int high)
	{
		return (high << 8) + low;
	}
	
	static boolean getBit(int val, int num)
	{
		return (val & (1 << num)) != 0;
	}
	
	static int clearBit(int val, int num)
	{
		return val & (0xff - (1 << num));
	}
	
	static int setBit(int val, int num)
	{
		return val | (1 << num);
	}
	
	static void writeByte(RandomAccessFile raf, int offset, int val) throws IOException
	{
		raf.seek(offset);
		raf.writeByte(val);
	}
	
	static byte readByte(RandomAccessFile raf, int offset) throws IOException
	{
		raf.seek(offset);
		return raf.readByte();
	}
	
	static void executeCommand(String cmd)
	{
		Process p;
		
		try {
			p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();
			StreamEater stdoutEater = new StreamEater (stdout);
			StreamEater stderrEater = new StreamEater (stderr);
			stdoutEater.start();
			stderrEater.start();
			  
			while (true)
			{
				try
				{
					p.waitFor();
					break;
				}
				catch(Exception f) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
