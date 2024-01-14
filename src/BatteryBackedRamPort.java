import java.io.IOException;
import java.io.RandomAccessFile;

public class BatteryBackedRamPort implements MemoryPort {
	RandomAccessFile raf;
	int offset;
	byte val;
	
	public BatteryBackedRamPort(RandomAccessFile raf, int offset)
	{
		this.raf = raf;
		this.offset = offset;
		try {
			val = Utils.readByte(raf, offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		this.val = val;
		
		try
		{
			Utils.writeByte(raf, offset, val);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
