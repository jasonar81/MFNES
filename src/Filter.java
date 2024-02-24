import java.util.ArrayList;

public class Filter {
	ArrayList<Integer> addresses = new ArrayList<Integer>();
	ArrayList<Integer> values = new ArrayList<Integer>();
	
	public boolean filter(int[] row)
	{
		for (int i = 0; i < addresses.size(); ++i)
		{
			if (row[addresses.get(i)] != values.get(i))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void addFilter(int address, int value)
	{
		addresses.add(address);
		values.add(value);
	}
	
	public Filter clone()
	{
		Filter retval = new Filter();
		for (int i = 0; i < addresses.size(); ++i)
		{
			retval.addresses.add(addresses.get(i));
			retval.values.add(values.get(i));
		}
		
		return retval;
	}
}
