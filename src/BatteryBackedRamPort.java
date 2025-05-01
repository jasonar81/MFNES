import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

//Use for cartridges that have save abilities

public class BatteryBackedRamPort implements MemoryPort, Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	transient RandomAccessFile raf;
	int offset;
	byte val;
	
	public BatteryBackedRamPort(RandomAccessFile raf, int offset)
	{
		this.raf = raf;
		this.offset = offset;
		try {
			val = Utils.readByte(raf, offset);
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	@Override
	public byte read() {
		return val;
	}

	@Override
	public void write(byte val) {
		this.val = val;
		
		if (raf != null)
		{
			try
			{
				Utils.writeByte(raf, offset, val);
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
	}

}
