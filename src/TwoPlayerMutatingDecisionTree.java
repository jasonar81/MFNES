import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TwoPlayerMutatingDecisionTree implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private ArrayList<Integer> validStates;
	private IfElseNode root1;
	private int treeSize1;
	private IfElseNode backup1;
	private IfElseNode root2;
	private int treeSize2;
	private IfElseNode backup2;
	
	public TwoPlayerMutatingDecisionTree(ArrayList<Integer> validStates)
	{
		this.validStates = validStates;
		root1 = new IfElseNode();
		root1.terminal = true;
		root1.terminalValue = 0;
		treeSize1 = 1;
		root1.myNum = 0;
		
		root2 = new IfElseNode();
		root2.terminal = true;
		root2.terminalValue = 0;
		treeSize2 = 1;
		root2.myNum = 0;
	}
	
	public TwoPlayerMutatingDecisionTree(ArrayList<Integer> validStates, NewMutatingDecisionTree tree1, NewMutatingDecisionTree tree2)
	{
		this.validStates = validStates;
		root1 = tree1.getRoot().clone();
		root2 = tree2.getRoot().clone();
		reindex();
	}
	
	public void resetRoots()
	{
		root1 = new IfElseNode();
		root1.terminal = true;
		root1.terminalValue = 0;
		treeSize1 = 1;
		root1.myNum = 0;
		
		root2 = new IfElseNode();
		root2.terminal = true;
		root2.terminalValue = 0;
		treeSize2 = 1;
		root2.myNum = 0;
	}
	
	public void setValidStates(ArrayList<Integer> validStates)
	{
		this.validStates = validStates;
	}
	
	public IfElseNode[] getRootsClones()
	{
		IfElseNode[] retval = new IfElseNode[2];
		retval[0] = root1.clone();
		retval[1] = root2.clone();
		return retval;
	}
	
	public void reindex()
	{
		treeSize1 = recomputeTreeSize(root1, 0);
		treeSize2 = recomputeTreeSize(root2, 0);
	}
	
	private void cleanseAav(HashSet<Integer> addressesAndValues)
	{
		HashMap<Integer, Integer> addressesAndCounts = new HashMap<Integer, Integer>();
		for (int x : addressesAndValues)
		{
			int address = (x >> 8);
			if (addressesAndCounts.containsKey(address))
			{
				addressesAndCounts.put(address, addressesAndCounts.get(address) + 1);
			}
			else
			{
				addressesAndCounts.put(address, 1);
			}
		}
		
		for (Map.Entry<Integer, Integer> entry : addressesAndCounts.entrySet())
		{
			if (entry.getValue() == 1)
			{
				int address = entry.getKey();
				for (int x : addressesAndValues)
				{
					int address2 = (x >> 8);
					if (address2 == address)
					{
						addressesAndValues.remove(x);
						break;
					}
				}
			}
		}
	}
	
	public void mutate(HashSet<Integer> addressesAndValues)
	{
		//Remove addresses that only have one value
		cleanseAav(addressesAndValues);
		
		System.out.println("We have " + addressesAndValues.size() + " address/value pairs");
		backup1 = root1.clone();
		backup2 = root2.clone();
		root1.clearCounts();
		root2.clearCounts();
		
		int numChanges = (Math.abs(ThreadLocalRandom.current().nextInt()) % 2) + 1;
		int changes = 0;
		while (true)
		{
			int type = Math.abs(ThreadLocalRandom.current().nextInt()) % 7;
			if (type == 0)
			{
				//Change a terminal value
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				IfElseNode node = getNode(num);
				while (!node.terminal)
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % validStates.size();
				int newValue = validStates.get(num);
				while (otherChildOfParentOf(node) != null && otherChildOfParentOf(node).terminal && otherChildOfParentOf(node).terminalValue == newValue)
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % validStates.size();
					newValue = validStates.get(num);
				}
				
				System.out.println("Changing terminal value to " + String.format("0x%02X", validStates.get(num)));
				node.terminalValue = newValue;
				System.out.println("Tree size is now " + (treeSize1 + treeSize2));
				
				++changes;
				if (changes != numChanges)
				{
					continue;
				}
			
				return;
			} 
			else if (type == 1)
			{
				//Convert a terminal node into a decision
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				IfElseNode node = getNode(num);
				while (!node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				//Pick an address and a value to use
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
				int i = 0;
				for (int x : addressesAndValues)
				{
					if (i == num)
					{
						int terminalValue = node.terminalValue;
						node.address = (x >> 8);
						node.checkValue = (int)((byte)(x & 0xff));
						node.terminal = false;
						IfElseNode right = new IfElseNode();
						right.terminal = true;
						right.terminalValue = terminalValue;
						right.parent = node;
						node.right = right;
						IfElseNode left = new IfElseNode();
						left.terminal = true;
						left.parent = node;
						node.left = left;
						
						num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
						node.comparisonType = num;
						
						if (num >= 3)
						{
							node.address2 = getOtherRandomAddress(node.address, addressesAndValues);
							node.checkValue = 0;
							
							if (num >= 6)
							{
								node.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
								if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
								{
									node.checkValue *= -1;
								}
							}
						}
						
						if (num < 6)
						{
							boolean flip = ThreadLocalRandom.current().nextBoolean();
							if (flip)
							{
								node.comparisonType += 9;
							}
						}
						
						left.terminalValue = terminalValue;
						reindex();
						System.out.println("Changing terminal node to if/else");
						System.out.println("Tree size is now " + (treeSize1 + treeSize2));
						
						++changes;
						if (changes != numChanges)
						{
							break;
						}
						
						return;
					}
					
					++i;
				}
			}
			else if (type == 2)
			{
				//Convert a non-terminal node to a terminal node
				if (treeSize1 == 1 || treeSize2 == 1)
				{
					continue;
				}
				
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				IfElseNode node = getNode(num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				int terminalValue = getElseTerminalValue(node);
				node.address = 0;
				node.terminal = true;
				node.checkValue = 0;
				node.left.parent = null;
				node.left = null;
				node.right.parent = null;
				node.right = null;
				node.terminalValue = terminalValue;
				node.address2 = 0;
				node.comparisonType = 0;
				System.out.println("Converting non-terminal node to terminal with value " + String.format("0x%02X", terminalValue));
				reindex();
				System.out.println("Tree size is now " + (treeSize1 + treeSize2));
				
				++changes;
				if (changes != numChanges)
				{
					continue;
				}
				
				return;
			} else if (type == 3)
			{
				//Insert non-terminal node			
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				IfElseNode node = getNode(num);
				
				//Insert node as parent
				if (node.parent == null && num < treeSize1)
				{
					IfElseNode newNode = new IfElseNode();
					newNode.right = root1;
					root1.parent = newNode;
					root1 = newNode;
					root1.terminal = false;
					
					//Pick an address and a value to use
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
					int i = 0;
					for (int x : addressesAndValues)
					{
						if (i == num)
						{
							root1.address = (x >> 8);
							root1.checkValue = (int)((byte)(x & 0xff));
							
							IfElseNode left = root1.right.clone();
							root1.left = left;
							left.parent = root1;
							
							num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
							root1.comparisonType = num;
							
							if (num >= 3)
							{
								root1.address2 = getOtherRandomAddress(root1.address, addressesAndValues);
								root1.checkValue = 0;
								if (num >= 6)
								{
									root1.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
									if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
									{
										root1.checkValue *= -1;
									}
								}
							}
							else
							{
								root1.address2 = 0;
							}
							
							if (num < 6)
							{
								boolean flip = ThreadLocalRandom.current().nextBoolean();
								if (flip)
								{
									root1.comparisonType += 9;
								}
							}
							
							reindex();
							System.out.println("Adding new node at root");
							System.out.println("Tree size is now " + (treeSize1 + treeSize2));
							
							++changes;
							if (changes != numChanges)
							{
								break;
							}

							return;
						}
						
						++i;
					}
				}
				else if (node.parent == null)
				{
					IfElseNode newNode = new IfElseNode();
					newNode.right = root2;
					root2.parent = newNode;
					root2 = newNode;
					root2.terminal = false;
					
					//Pick an address and a value to use
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
					int i = 0;
					for (int x : addressesAndValues)
					{
						if (i == num)
						{
							root2.address = (x >> 8);
							root2.checkValue = (int)((byte)(x & 0xff));
							
							IfElseNode left = root2.right.clone();
							root2.left = left;
							left.parent = root2;
							
							num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
							root2.comparisonType = num;
							
							if (num >= 3)
							{
								root2.address2 = getOtherRandomAddress(root2.address, addressesAndValues);
								root2.checkValue = 0;
								if (num >= 6)
								{
									root2.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
									if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
									{
										root2.checkValue *= -1;
									}
								}
							}
							else
							{
								root2.address2 = 0;
							}
							
							if (num < 6)
							{
								boolean flip = ThreadLocalRandom.current().nextBoolean();
								if (flip)
								{
									root2.comparisonType += 9;
								}
							}
							
							reindex();
							System.out.println("Adding new node at root");
							System.out.println("Tree size is now " + (treeSize1 + treeSize2));
							
							++changes;
							if (changes != numChanges)
							{
								break;
							}

							return;
						}
						
						++i;
					}
				}
				else
				{
					IfElseNode newNode = new IfElseNode();
					IfElseNode parent = node.parent;
					
					newNode.right = node;
					newNode.left = node.clone();
					newNode.left.parent = newNode;
					node.parent = newNode;
					newNode.terminal = false;
					
					//Pick an address and a value to use
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
					int i = 0;
					for (int x : addressesAndValues)
					{
						if (i == num)
						{
							newNode.address = (x >> 8);
							newNode.checkValue = (int)((byte)(x & 0xff));
							
							num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
							newNode.comparisonType = num;
							
							if (num >= 3)
							{
								newNode.address2 = getOtherRandomAddress(newNode.address, addressesAndValues);
								newNode.checkValue = 0;
								if (num >= 6)
								{
									newNode.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
									if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
									{
										newNode.checkValue *= -1;
									}
								}
							}
							else
							{
								newNode.address2 = 0;
							}
							
							if (num < 6)
							{
								boolean flip = ThreadLocalRandom.current().nextBoolean();
								if (flip)
								{
									newNode.comparisonType += 9;
								}
							}
							
							if (parent.left == node)
							{
								parent.left = newNode;
								newNode.parent = parent;
							}
							else
							{
								parent.right = newNode;
								newNode.parent = parent;
							}
							
							System.out.println("Inserted a new node");
							reindex();
							System.out.println("Tree size is now " + (treeSize1 + treeSize2));
							
							++changes;
							if (changes != numChanges)
							{
								break;
							}
							
							return;
						}
						
						++i;
					}
				}
			} else if (type == 4)
			{
				//Change the comparison type on a non-terminal node
				if (treeSize1 == 1 || treeSize2 == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;

				num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				node = getNode(num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				while (num == node.comparisonType)
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				}
				
				if (node.comparisonType <= 2 && num <= 2)
				{
					node.comparisonType = num;
				} else if (node.comparisonType <= 2 && num >= 3)
				{
					node.address2 = getOtherRandomAddress(node.address, addressesAndValues);
					node.comparisonType = num;
					node.checkValue = 0;
					if (num >= 6)
					{
						node.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
						if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
						{
							node.checkValue *= -1;
						}
					}
				} else if (node.comparisonType >= 3 && num <= 2)
				{
					node.address2  = 0;
					node.comparisonType = num;
					Integer newCheckValue = getRandomCheckValueForAddress(node.address, addressesAndValues);
					if (newCheckValue == null)
					{
						revert();
						changes = 0;
						continue;
					}
					
					node.checkValue = newCheckValue;
				} else
				{
					node.comparisonType = num;
					if (num >= 6)
					{
						node.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
						if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
						{
							node.checkValue *= -1;
						}
					}
				}
				
				if (num < 6)
				{
					boolean flip = ThreadLocalRandom.current().nextBoolean();
					if (flip)
					{
						node.comparisonType += 9;
					}
				}
				
				System.out.println("Changing comparison type");
				System.out.println("Tree size is now " + (treeSize1 + treeSize2));
				
				++changes;
				if (changes != numChanges)
				{
					continue;
				}
				
				return;
			} else if (type == 5)
			{
				//Change address on non-terminal node
				if (treeSize1 == 1 || treeSize2 == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;
				
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				node = getNode(num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				if (node.comparisonType <= 2)
				{
					Integer newAddr = getNewAddressForCheckValue(node.address, node.checkValue, addressesAndValues);
					if (newAddr == null)
					{
						continue;
					}
					
					node.address = newAddr;
					System.out.println("Changing address");
					System.out.println("Tree size is now " + (treeSize1 + treeSize2));
					
					++changes;
					if (changes != numChanges)
					{
						continue;
					}
					
					return;
				}
				else
				{
					int whichAddr = Math.abs(ThreadLocalRandom.current().nextInt()) % 2;
					if (whichAddr == 0)
					{
						Integer newAddr = getNewAddressForTwoAddresses(node.address, node.address2, addressesAndValues);
						if (newAddr == null)
						{
							continue;
						}
						
						node.address = newAddr;
						System.out.println("Changing address");
						System.out.println("Tree size is now " + (treeSize1 + treeSize2));
						
						++changes;
						if (changes != numChanges)
						{
							continue;
						}
						
						return;
					}
					else
					{
						Integer newAddr = getNewAddressForTwoAddresses(node.address, node.address2, addressesAndValues);
						if (newAddr == null)
						{
							continue;
						}
						
						node.address2 = newAddr;
						System.out.println("Changing address");
						System.out.println("Tree size is now " + (treeSize1 + treeSize2));
						
						++changes;
						if (changes != numChanges)
						{
							continue;
						}
						
						return;
					}
				}
			} else if (type == 6)
			{
				//Change check value for internal node
				if (treeSize1 == 1 || treeSize2 == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;
				
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
				node = getNode(num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % (treeSize1 + treeSize2);
					node = getNode(num);
				}
				
				if (node.comparisonType <= 2)
				{
					Integer checkValue = getRandomCheckValueForAddressThatsNot(node.address, addressesAndValues, node.checkValue);
					if (checkValue == null)
					{
						continue;
					}
					
					node.checkValue = checkValue;
					System.out.println("Changing check value");
					System.out.println("Tree size is now " + (treeSize1 + treeSize2));
					
					++changes;
					if (changes != numChanges)
					{
						continue;
					}
					
					return;
				} else if (node.comparisonType >= 6)
				{
					node.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
					if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
					{
						node.checkValue *= -1;
					}
					
					System.out.println("Changing check value");
					System.out.println("Tree size is now " + (treeSize1 + treeSize2));
					
					++changes;
					if (changes != numChanges)
					{
						continue;
					}
					
					return;
				}
				else
				{
					continue;
				}
			}
		}
	}
	
	public void revert()
	{
		root1 = backup1.clone();
		root2 = backup2.clone();
		reindex();
	}
	
	public void persist()
	{
		backup1 = root1.clone();
		backup2 = root2.clone();
	}
	
	private int recomputeTreeSize(IfElseNode node, int nodeNum)
	{
		int retval = 1;
		node.myNum = nodeNum;
		if (node.terminal) 
		{
			return retval;
		}
		
		int subtreeSize = recomputeTreeSize(node.left, nodeNum + 1);
		nodeNum += subtreeSize;
		retval += subtreeSize;
		
		subtreeSize = recomputeTreeSize(node.right, nodeNum + 1);
		nodeNum += subtreeSize;
		retval += subtreeSize;
		return retval;
	}
	
	private IfElseNode getNode(IfElseNode node, int nodeNum)
	{
		if (nodeNum == node.myNum)
		{
			return node;
		}
		
		if (node.terminal)
		{
			return null;
		}
		
		IfElseNode result = getNode(node.left, nodeNum);
		if (result != null)
		{
			return result;
		}
		
		result = getNode(node.right, nodeNum);
		return result;
	}
	
	private IfElseNode getNode(int nodeNum)
	{
		IfElseNode node = null;
		if (nodeNum < treeSize1)
		{
			node = root1;
		}
		else
		{
			node = root2;
			nodeNum -= treeSize1;
		}
		
		return getNode(node, nodeNum);
	}
	
	private int getElseTerminalValue(IfElseNode node)
	{
		while (!node.terminal)
		{
			node = node.right;
		}
		
		return node.terminalValue;
	}
	
	private IfElseNode otherChildOfParentOf(IfElseNode node)
	{
		if (node.parent == null)
		{
			return null;
		}
		
		IfElseNode parent = node.parent;
		if (parent.right == node)
		{
			return parent.left;
		}
		
		return parent.right;
	}
	
	private int getOtherRandomAddress(int address, HashSet<Integer> addressesAndValues)
	{
		while (true)
		{
			int num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
			int i = 0;
			for (Integer x : addressesAndValues)
			{
				if (i == num)
				{
					int address2 = (x >> 8);
					if (address2 != address)
					{
						return address2;
					}
					
					break;
				}
				++i;
			}
		}
	}
	
	private Integer getRandomCheckValueForAddress(int address, HashSet<Integer> addressesAndValues)
	{
		ArrayList<Integer> checkValues = new ArrayList<Integer>();
		for (int x : addressesAndValues)
		{
			int address2 = (x >> 8);
			if (address2 == address)
			{
				checkValues.add((int)((byte)(x & 0xff)));
			}
		}
		
		if (checkValues.size() == 0)
		{
			return null;
		}
		
		int num = Math.abs(ThreadLocalRandom.current().nextInt()) % checkValues.size();
		return checkValues.get(num);
	}
	
	private Integer getRandomCheckValueForAddressThatsNot(int address, HashSet<Integer> addressesAndValues, int notThis)
	{
		ArrayList<Integer> checkValues = new ArrayList<Integer>();
		for (int x : addressesAndValues)
		{
			int address2 = (x >> 8);
			if (address2 == address)
			{
				int checkValue = (int)((byte)(x & 0xff));
				if (checkValue != notThis)
				{
					checkValues.add((int)((byte)(x & 0xff)));
				}
			}
		}
		
		if (checkValues.size() == 0)
		{
			return null;
		}
		
		int num = Math.abs(ThreadLocalRandom.current().nextInt()) % checkValues.size();
		return checkValues.get(num);
	}
	
	private Integer getNewAddressForTwoAddresses(int address, int address2, HashSet<Integer> addressesAndValues)
	{
		while (true)
		{
			int num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
			int i = 0;
			for (int x : addressesAndValues)
			{
				if (i == num)
				{
					int addr = (x >> 8);
					if (addr != address && addr != address2)
					{
						return addr;
					}
					
					break;
				}
				
				++i;
			}
		}
	}
	
	private Integer getNewAddressForCheckValue(int address, int checkValue, HashSet<Integer> addressesAndValues)
	{
		ArrayList<Integer> addresses = new ArrayList<Integer>();
		for (int x : addressesAndValues)
		{
			int checkValue2 = (int)((byte)(x & 0xff));
			if (checkValue2 == checkValue)
			{
				int addr = (x >> 8);
				if (addr != address)
				{
					addresses.add(addr);
				}
			}
		}
		
		if (addresses.size() == 0)
		{
			return null;
		}
		
		int num = Math.abs(ThreadLocalRandom.current().nextInt()) % addresses.size();
		return addresses.get(num);
	}
	
	private HashSet<Integer> intersect(HashSet<Integer> left, HashSet<Integer> right)
	{
		HashSet<Integer> intersection = new HashSet<Integer>(left); 
		intersection.retainAll(right);
		return intersection;
	}
	
	public IfElseNode[] merge(TwoPlayerMutatingDecisionTree rhs, HashSet<Integer> addressesAndValues1, HashSet<Integer> addressesAndValues2)
	{
		HashSet<Integer> addressesAndValues = intersect(addressesAndValues1, addressesAndValues2);
		IfElseNode left;
		IfElseNode right;
		
		IfElseNode[] roots1 = getRootsClones();
		IfElseNode[] roots2 = rhs.getRootsClones();
		
		if (ThreadLocalRandom.current().nextBoolean())
		{
			left = roots1[0];
			right = roots2[0];
		}
		else
		{
			left = roots2[0];
			right = roots1[0];
		}
		
		IfElseNode newNode = new IfElseNode();
		newNode.right = right;
		right.parent = newNode;
		newNode.terminal = false;
		newNode.left = left;
		left.parent = newNode;
		
		//Pick an address and a value to use
		int num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
		int i = 0;
		for (int x : addressesAndValues)
		{
			if (i == num)
			{
				newNode.address = (x >> 8);
				newNode.checkValue = (int)((byte)(x & 0xff));
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				newNode.comparisonType = num;
				
				if (num >= 3)
				{
					newNode.address2 = getOtherRandomAddress(left.address, addressesAndValues);
					newNode.checkValue = 0;
					if (num >= 6)
					{
						newNode.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
						if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
						{
							newNode.checkValue *= -1;
						}	
					}
				}
				else
				{
					newNode.address2 = 0;
				}
				
				if (num < 6)
				{
					boolean flip = ThreadLocalRandom.current().nextBoolean();
					if (flip)
					{
						newNode.comparisonType += 9;
					}
				}
				
				break;
			}
			
			++i;
		}
		
		IfElseNode[] retval = new IfElseNode[2];
		retval[0] = newNode;
		
		if (ThreadLocalRandom.current().nextBoolean())
		{
			left = roots1[1];
			right = roots2[1];
		}
		else
		{
			left = roots2[1];
			right = roots1[1];
		}
		
		newNode = new IfElseNode();
		newNode.right = right;
		right.parent = newNode;
		newNode.terminal = false;
		newNode.left = left;
		left.parent = newNode;
		
		//Pick an address and a value to use
		num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
		i = 0;
		for (int x : addressesAndValues)
		{
			if (i == num)
			{
				newNode.address = (x >> 8);
				newNode.checkValue = (int)((byte)(x & 0xff));
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				newNode.comparisonType = num;
				
				if (num >= 3)
				{
					newNode.address2 = getOtherRandomAddress(left.address, addressesAndValues);
					newNode.checkValue = 0;
					if (num >= 6)
					{
						newNode.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
						if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
						{
							newNode.checkValue *= -1;
						}	
					}
				}
				else
				{
					newNode.address2 = 0;
				}
				
				if (num < 6)
				{
					boolean flip = ThreadLocalRandom.current().nextBoolean();
					if (flip)
					{
						newNode.comparisonType += 9;
					}
				}
				
				break;
			}
			
			++i;
		}
		
		retval[1] = newNode;
		return retval;
	}
	
	public void setRoots(IfElseNode[] roots)
	{
		this.root1 = roots[0];
		this.root2 = roots[1];
	}
	
	public int run(int[] row)
	{
		int state = root1.run(row);
		state <<= 8;
		state |= root2.run(row);
		return state;
	}
}
