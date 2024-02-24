//More flexible than the first version of decision trees
//which used CompleteSplitDecision objects as nodes

import java.io.Serializable;
import java.util.Objects;

public class IfElseNode implements Serializable {
	private static final long serialVersionUID = 6635695383037681711L;
	int address;
	int checkValue;
	boolean terminal;
	IfElseNode left = null;
	IfElseNode right = null;
	int terminalValue;
	IfElseNode parent = null;
	int myNum;
	int comparisonType = 0 ;
	int address2;
	int leftCount = 0;
	int rightCount = 0;
	
	public int run(int[] row)
	{
		if (terminal)
		{
			return terminalValue;
		}
		
		if (comparisonType == 0)
		{
			if (row[address] == checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		}
		else if (comparisonType == 1)
		{
			if (row[address] >= checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 2)
		{
			if (row[address] <= checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 3)
		{
			if (row[address] == row[address2])
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 4)
		{
			if (row[address] >= row[address2])
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 5)
		{
			if (row[address] <= row[address2])
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 6)
		{
			if (Byte.toUnsignedInt((byte)row[address]) - Byte.toUnsignedInt((byte)row[address2]) == checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 7)
		{
			if (Byte.toUnsignedInt((byte)row[address]) - Byte.toUnsignedInt((byte)row[address2]) >= checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		} else if (comparisonType == 8)
		{
			if (Byte.toUnsignedInt((byte)row[address]) - Byte.toUnsignedInt((byte)row[address2]) <= checkValue)
			{
				++leftCount;
				return left.run(row);
			}
		}
		
		++rightCount;
		return right.run(row);
	}
	
	public IfElseNode clone()
	{
		IfElseNode retval = new IfElseNode();
		retval.address = address;
		retval.address2 = address2;
		retval.checkValue = checkValue;
		retval.terminal = terminal;
		retval.comparisonType = comparisonType;
		
		if (left != null)
		{
			retval.left = left.clone();
			retval.left.parent = retval;
		}
		else
		{
			retval.left = null;
		}
		
		if (right != null)
		{
			retval.right = right.clone();
			retval.right.parent = retval;
		}
		else
		{
			retval.right = null;
		}
		
		retval.terminalValue = terminalValue;
		retval.myNum = myNum;
		retval.leftCount = leftCount;
		retval.rightCount = rightCount;
		
		return retval;
	}
	
	public int hashCode()
	{
		int retval = 0;
		if (terminal)
		{
			retval = Objects.hash(terminal, terminalValue);
		}
		else
		{
			retval = Objects.hash(address, terminal, left, right, comparisonType);
			if (comparisonType <= 2)
			{
				retval = Objects.hash(retval, checkValue);
			}
			else if (comparisonType <= 5)
			{
				retval = Objects.hash(retval, address2);
			} else 
			{
				retval = Objects.hash(retval, address2, checkValue);
			}
		}
		
		return retval;
	}
	
	public boolean equals(Object rhs)
	{
		if (rhs == null)
		{
			return false;
		}
		
		if (!(rhs instanceof IfElseNode))
		{
			return false;
		}
		
		IfElseNode r = (IfElseNode)rhs;
		if (terminal && r.terminal)
		{
			return terminalValue == r.terminalValue;
		}
		
		boolean ok = (terminal == r.terminal && address == r.address && comparisonType == r.comparisonType && left.equals(r.left) && right.equals(r.right));
		if (!ok)
		{
			return false;
		}
		
		if (comparisonType <= 2)
		{
			return checkValue == r.checkValue;
		} else if (comparisonType <= 5)
		{
			return address2 == r.address2;
		}
		
		return checkValue == r.checkValue && address2 == r.address2;
	}
	
	public void clearCounts()
	{
		leftCount = 0;
		rightCount = 0;
	}
	
	public int getLeftCount()
	{
		return leftCount;
	}
	
	public int getRightCount()
	{
		return rightCount;
	}
}
