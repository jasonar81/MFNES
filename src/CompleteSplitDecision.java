import java.io.Serializable;
import java.util.ArrayList;

public class CompleteSplitDecision implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	int address;
	boolean terminal;
	ArrayList<Integer> splits = new ArrayList<Integer>();
	ArrayList<CompleteSplitDecision> children = new ArrayList<CompleteSplitDecision>();
	int terminalValue;
	int majority;
	ClassCounts finalClassCounts;
	CompleteSplitDecision parent = null;
	int myNum;
	
	public int run(int[] row)
	{
		if (terminal)
		{
			return terminalValue;
		}
		
		for (int i = 0; i < splits.size(); ++i)
		{
			if (row[address] == splits.get(i))
			{
				return children.get(i).run(row);
			}
		}
		
		if (children.size() > splits.size())
		{
			return children.get(children.size() - 1).run(row);
		}
		
		return majority;
	}
	
	public CompleteSplitDecision clone()
	{
		CompleteSplitDecision retval = new CompleteSplitDecision();
		retval.address = address;
		retval.terminal = terminal;
		for (int x : splits)
		{
			retval.splits.add(x);
		}
		
		for (CompleteSplitDecision x : children)
		{
			retval.children.add(x.clone());
		}
		
		retval.terminalValue = terminalValue;
		retval.majority = majority;
		
		for (CompleteSplitDecision x : retval.children)
		{
			x.parent = retval;
		}
		
		return retval;
	}
}
