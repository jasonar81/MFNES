import java.io.IOException;
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
}
