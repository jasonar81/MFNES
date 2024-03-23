import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MultiDecisionTree implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	private ArrayList<Integer> validStates;
	private HashMap<ArrayList<Integer>, IfElseNode> roots;
	private HashMap<ArrayList<Integer>, Integer> treeSizes;
	private transient IfElseNode backup;
	private ArrayList<Integer> sceneAddresses;
	private ArrayList<Integer> disallow;
	private int defaultVal;
	private transient boolean runAllMode;
	private transient ArrayList<ArrayList<Integer>> sceneOrder;
	private transient Snapshotable snapshotAgent;
	private transient HashMap<ArrayList<Integer>, Snapshot> snapshots;
	private transient int sceneNum;
	private transient boolean foundNextKey;
	
	public MultiDecisionTree(ArrayList<Integer> validStates, ArrayList<Integer> sceneAddresses, int defaultVal, ArrayList<Integer> disallow)
	{
		this.validStates = validStates;
		roots = new HashMap<ArrayList<Integer>, IfElseNode>();
		treeSizes = new HashMap<ArrayList<Integer>, Integer>();
		this.sceneAddresses = sceneAddresses;
		this.disallow = disallow;
		this.defaultVal = defaultVal;
		sceneOrder = new ArrayList<ArrayList<Integer>>();
		snapshots = new HashMap<ArrayList<Integer>, Snapshot>();
	}
	
	public void makeWhole()
	{
		sceneOrder = new ArrayList<ArrayList<Integer>>();
		snapshots = new HashMap<ArrayList<Integer>, Snapshot>();
	}
	
	public void setSnapshotAgent(Snapshotable snapshotAgent)
	{
		this.snapshotAgent = snapshotAgent;
	}
	
	public boolean foundNextKey()
	{
		return foundNextKey;
	}
	
	public void setRunSceneMode(int sceneNum)
	{
		runAllMode = false;
		this.sceneNum = sceneNum;
	}
	
	public void setRunAllMode()
	{
		runAllMode = true;
	}
	
	public void setValidStates(ArrayList<Integer> validStates)
	{
		this.validStates = validStates;
	}
	
	public void reindexTree(ArrayList<Integer> key)
	{
		treeSizes.put(key, recomputeTreeSize(roots.get(key), 0));
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
		
		IfElseNode root = roots.get(sceneOrder.get(sceneNum));
		int treeSize = recomputeTreeSize(root, 0);
		treeSizes.put(sceneOrder.get(sceneNum), treeSize);
		backup = root.clone();
		root.clearCounts();
		
		//int numChanges = (Math.abs(ThreadLocalRandom.current().nextInt()) % 2) + 1;
		int numChanges = 1;
		int changes = 0;
		while (true)
		{
			int type = Math.abs(ThreadLocalRandom.current().nextInt()) % 7;
			if (type == 0)
			{
				//Change a terminal value
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				IfElseNode node = getNode(root, num);
				while (!node.terminal)
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
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
				System.out.println("Tree size is now " + treeSize);
				
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
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				IfElseNode node = getNode(root, num);
				while (!node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
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
						treeSize = recomputeTreeSize(root, 0);
						System.out.println("Changing terminal node to if/else");
						System.out.println("Tree size is now " + treeSize);
						
						++changes;
						if (changes != numChanges)
						{
							break;
						}
						
						if (disallow.contains(node.address) || disallow.contains(node.address2))
						{
							revert();
							changes = 0;
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
				if (treeSize == 1)
				{
					continue;
				}
				
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				IfElseNode node = getNode(root, num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
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
				treeSize = recomputeTreeSize(root, 0);
				System.out.println("Tree size is now " + treeSize);
				
				++changes;
				if (changes != numChanges)
				{
					continue;
				}
				
				if (disallow.contains(node.address) || disallow.contains(node.address2))
				{
					revert();
					changes = 0;
					continue;
				}
				
				return;
			} else if (type == 3)
			{
				//Insert non-terminal node			
				int num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				IfElseNode node = getNode(root, num);
				
				//Insert node as parent
				if (node.parent == null)
				{
					IfElseNode newNode = new IfElseNode();
					newNode.right = root;
					root.parent = newNode;
					root = newNode;
					root.terminal = false;
					
					//Pick an address and a value to use
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % addressesAndValues.size();
					int i = 0;
					for (int x : addressesAndValues)
					{
						if (i == num)
						{
							root.address = (x >> 8);
							root.checkValue = (int)((byte)(x & 0xff));
							
							IfElseNode left = root.right.clone();
							root.left = left;
							left.parent = root;
							
							num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
							root.comparisonType = num;
							
							if (num >= 3)
							{
								root.address2 = getOtherRandomAddress(root.address, addressesAndValues);
								root.checkValue = 0;
								if (num >= 6)
								{
									root.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
									if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
									{
										root.checkValue *= -1;
									}
								}
							}
							else
							{
								root.address2 = 0;
							}
							
							if (num < 6)
							{
								boolean flip = ThreadLocalRandom.current().nextBoolean();
								if (flip)
								{
									root.comparisonType += 9;
								}
							}
							
							treeSize = recomputeTreeSize(root, 0);
							System.out.println("Adding new node at root");
							System.out.println("Tree size is now " + treeSize);
							
							++changes;
							if (changes != numChanges)
							{
								break;
							}
							
							if (disallow.contains(root.address) || disallow.contains(root.address2))
							{
								revert();
								changes = 0;
								break;
							}
							
							roots.put(sceneOrder.get(sceneNum), root);
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
							treeSize = recomputeTreeSize(root, 0);
							System.out.println("Tree size is now " + treeSize);
							
							++changes;
							if (changes != numChanges)
							{
								break;
							}
							
							if (disallow.contains(newNode.address) || disallow.contains(newNode.address2))
							{
								revert();
								changes = 0;
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
				if (treeSize == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				node = getNode(root, num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
				}
				
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				while (num == node.comparisonType)
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % 9;
				}
				
				if (node.comparisonType >= 9)
				{
					node.comparisonType -= 9;
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
				System.out.println("Tree size is now " + treeSize);
				
				++changes;
				if (changes != numChanges)
				{
					continue;
				}
				
				if (disallow.contains(node.address) || disallow.contains(node.address2))
				{
					revert();
					changes = 0;
					continue;
				}
				
				return;
			} else if (type == 5)
			{
				//Change address on non-terminal node
				if (treeSize == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				node = getNode(root, num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
				}
				
				if (node.comparisonType <= 2 || (node.comparisonType >= 9 && node.comparisonType <= 11))
				{
					Integer newAddr = getNewAddressForCheckValue(node.address, node.checkValue, addressesAndValues);
					if (newAddr == null)
					{
						continue;
					}
					
					node.address = newAddr;
					System.out.println("Changing address");
					System.out.println("Tree size is now " + treeSize);
					
					++changes;
					if (changes != numChanges)
					{
						continue;
					}
					
					if (disallow.contains(node.address) || disallow.contains(node.address2))
					{
						revert();
						changes = 0;
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
						System.out.println("Tree size is now " + treeSize);
						
						++changes;
						if (changes != numChanges)
						{
							continue;
						}
						
						if (disallow.contains(node.address) || disallow.contains(node.address2))
						{
							revert();
							changes = 0;
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
						System.out.println("Tree size is now " + treeSize);
						
						++changes;
						if (changes != numChanges)
						{
							continue;
						}
						
						if (disallow.contains(node.address) || disallow.contains(node.address2))
						{
							revert();
							changes = 0;
							continue;
						}
						
						return;
					}
				}
			} else if (type == 6)
			{
				//Change check value for internal node
				if (treeSize == 1)
				{
					continue;
				}
				
				int num = 0;
				IfElseNode node = null;
				num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
				node = getNode(root, num);
				while (node.terminal) 
				{
					num = Math.abs(ThreadLocalRandom.current().nextInt()) % treeSize;
					node = getNode(root, num);
				}
				
				if (node.comparisonType <= 2 || (node.comparisonType >= 9 && node.comparisonType <= 11))
				{
					Integer checkValue = getRandomCheckValueForAddressThatsNot(node.address, addressesAndValues, node.checkValue);
					if (checkValue == null)
					{
						continue;
					}
					
					node.checkValue = checkValue;
					System.out.println("Changing check value");
					System.out.println("Tree size is now " + treeSize);
					
					++changes;
					if (changes != numChanges)
					{
						continue;
					}
					
					return;
				} else if (node.comparisonType >= 6 && node.checkValue < 9)
				{
					node.checkValue = Math.abs(ThreadLocalRandom.current().nextInt()) % 256;
					if (Math.abs(ThreadLocalRandom.current().nextInt()) % 2 == 1)
					{
						node.checkValue *= -1;
					}
					
					System.out.println("Changing check value");
					System.out.println("Tree size is now " + treeSize);
					
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
		roots.put(sceneOrder.get(sceneNum), backup);
		treeSizes.put(sceneOrder.get(sceneNum), recomputeTreeSize(backup, 0));
	}
	
	public void persist()
	{
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
	
	public void reset()
	{
		sceneOrder.clear();
		snapshots.clear();
	}

	public int run(int[] allRam) {
		if (runAllMode)
		{
			ArrayList<Integer> key = new ArrayList<Integer>();
			for (Integer address : sceneAddresses)
			{
				key.add(allRam[address]);
			}
			
			if (!roots.containsKey(key))
			{
				IfElseNode root = new IfElseNode();
				root.terminal = true;
				root.terminalValue = defaultVal;
				roots.put(key, root);
				reindexTree(key);
			}
			
			if (!snapshots.containsKey(key))
			{
				sceneOrder.add(key);
				snapshots.put(key, snapshotAgent.snapshot());
			}
			
			return roots.get(key).run(allRam);
		}
		
		foundNextKey = false;
		ArrayList<Integer> key = new ArrayList<Integer>();
		for (Integer address : sceneAddresses)
		{
			key.add(allRam[address]);
		}
		
		if (!key.equals(sceneOrder.get(sceneNum)))
		{
			if (!sceneOrder.contains(key) || sceneOrder.indexOf(key) > sceneNum)
			{
				foundNextKey = true;
				return -1;
			}
		}
		
		return roots.get(sceneOrder.get(sceneNum)).run(allRam);
	}
	
	public int numSnapshots()
	{
		return sceneOrder.size();
	}
	
	public Snapshot getSnapshotForScene(int sceneNum)
	{
		Snapshot snapshot = snapshots.get(sceneOrder.get(sceneNum));
		Snapshot retval = null;

	    try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    	ObjectOutputStream out = new ObjectOutputStream(bos);
	        out.writeObject(snapshot);
	        out.flush();
	        out.close();
	        byte[] bytes = bos.toByteArray();
	        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

	        ObjectInputStream in = new ObjectInputStream(bis);
            retval = (Snapshot)in.readObject();
            bis.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.exit(-1);
	    }
		
		return retval;
	}
}
