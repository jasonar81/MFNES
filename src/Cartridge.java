import java.nio.file.Files;
import java.nio.file.Paths;

public class Cartridge {
	private int numPrg;
	private int numChr;
	private int mapper;
	private boolean vertMirroring;
	private boolean batteryBacked;
	private boolean trainer;
	private boolean fourScreen;
	private byte[] trainerData;
	private byte[] prgRom;
	private byte[] chrRom;
	
	public int numPrg()
	{
		return numPrg;
	}
	
	public int numChr()
	{
		return numChr;
	}
	
	public int mapper()
	{
		return mapper;
	}
	
	public boolean vertMirroring()
	{
		return vertMirroring;
	}
	
	public boolean batteryBacked()
	{
		return batteryBacked;
	}
	
	public boolean hasTrainer()
	{
		return trainer;
	}
	
	public boolean fourScreen()
	{
		return fourScreen;
	}
	
	public byte[] trainerData()
	{
		return trainerData;
	}
	
	public byte[] prgData()
	{
		return prgRom;
	}
	
	public byte[] chrData()
	{
		return chrRom;
	}
	
	static Cartridge loadCart(String filename) 
	{
		try
		{
			Cartridge retval = new Cartridge();
			byte[] bytes = Files.readAllBytes(Paths.get(filename));
			if (bytes[0] != 0x4e || bytes[1] != 0x45 || bytes[2] != 0x53 || bytes[3] != 0x1a)
			{
				throw new Exception("Invalid file format");
			}
			
			//number of 16KB PRG-ROM chunks
			retval.numPrg = Byte.toUnsignedInt(bytes[4]);
			retval.prgRom = new byte[16384 * retval.numPrg];
			
			//number of 8KB CHR-ROM chunks
			retval.numChr = Byte.toUnsignedInt(bytes[5]);
			retval.chrRom = new byte[8192 * retval.numChr];
			
			int flag6 = Byte.toUnsignedInt(bytes[6]);
			int flag7 = Byte.toUnsignedInt(bytes[7]);
			//int flag8 = Byte.toUnsignedInt(bytes[8]);
			//int flag9 = Byte.toUnsignedInt(bytes[9]);
			//int flag10 = Byte.toUnsignedInt(bytes[10]);
			
			retval.mapper = (flag7 & 0xf0) + (flag6 >> 4);
			retval.vertMirroring = Utils.getBit(flag6, 0);
			
			//Are the 60-7f pages battery backed RAM?
			retval.batteryBacked = Utils.getBit(flag6, 1);
			
			//Is there a 512 byte trainer at 0x7000
			retval.trainer = Utils.getBit(flag6, 2);
			
			retval.fourScreen = Utils.getBit(flag6, 3);
			
			int offset = 16;
			if (retval.trainer)
			{
				retval.trainerData = new byte[512];
				System.arraycopy(bytes, offset, retval.trainerData, 0, 512);
				offset += 512;
			}
			
			System.arraycopy(bytes, offset, retval.prgRom, 0, 16384 * retval.numPrg);
			offset += 16384 * retval.numPrg;
			System.arraycopy(bytes, offset, retval.chrRom, 0, 8192 * retval.numChr);
			offset += 8192 * retval.numChr;
			return retval;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private Cartridge()
	{}
}
