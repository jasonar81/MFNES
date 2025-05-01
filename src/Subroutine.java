import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Subroutine 
{
	private String name;
	private ArrayList<Byte> data = new ArrayList<Byte>();
	private int address = 0;
	private ArrayList<String> referenceLabels = new ArrayList<String>();
	private ArrayList<Integer> referenceOffsets = new ArrayList<Integer>();
	private HashMap<String, Integer> labelOffsets = new HashMap<String, Integer>();
	private HashMap<Integer, String> localRelocations = new HashMap<Integer, String>();
	
	public Subroutine(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public ArrayList<Byte> getData()
	{
		return data;
	}
	
	public int getAddress()
	{
		return address;
	}
	
	public void setAddress(int address)
	{
		this.address = address;
	}
	
	public void addReference(String label, int offset)
	{
		referenceLabels.add(label);
		referenceOffsets.add(offset);
	}
	
	public ArrayList<String> getReferenceLabels()
	{
		return referenceLabels;
	}
	
	public ArrayList<Integer> getReferenceOffsets()
	{
		return referenceOffsets;
	}
	
	public void callRoutine(String name)
	{
		data.add((byte)0x08); //PHP
		data.add((byte)0x48); //PHA
		data.add((byte)0x8a); //TXA
		data.add((byte)0x48); //PHA
		data.add((byte)0x98); //TYA
		data.add((byte)0x48); //PHA
		
		data.add((byte)0x20); //JSR
		addReference(name, data.size());
		data.add((byte)0);
		data.add((byte)0);
		data.add((byte)0x68); //PLA
		data.add((byte)0xa8); //TAY
		data.add((byte)0x68); //PLA
		data.add((byte)0xaa); //TAX
		data.add((byte)0x68); //PLA
		data.add((byte)0x28); //PLP
	}
	
	public void writeEpilogue()
	{
		data.add((byte)0x60); //RTS;
	}
	
	public void jump(String name)
	{
		data.add((byte)0x4c); //JMP
		addReference(name, data.size());
		data.add((byte)0);
		data.add((byte)0);
	}
	
	public void setLabel(String label)
	{
		if (labelOffsets.containsKey(label))
		{
			System.out.println("Duplicate label " + label + " defined in subroutine " + name);
			System.exit(-1);
		}
		
		labelOffsets.put(label, data.size());
	}
	
	public void branchTo(String label)
	{
		int offset = data.size();
		data.add((byte)0); //Needs to be fixed up later
		localRelocations.put(offset, label);
	}
	
	public void doLocalRelocations()
	{
		for (Map.Entry<Integer, String> entry : localRelocations.entrySet())
		{
			int offset = entry.getKey();
			String label = entry.getValue();
			if (!labelOffsets.containsKey(label))
			{
				System.out.println("Undefined label " + label + " in subroutine " + name);
				System.exit(-1);
			}
			
			int target = labelOffsets.get(label);
			int relative = target - (offset + 1);
			if (relative > 127 || relative < -128)
			{
				System.out.println("Relative branch out of range for label " + label + ". Offset = " + relative + " in subroutine " + name);
				System.exit(-1);
			}
			
			data.set(offset, (byte)relative);
		}
	}
}
