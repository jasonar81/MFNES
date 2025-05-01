import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MigrateTree {
	public static void main(String[] args)
	{
		String inFile = args[0];
		String outFile = args[1];
		
		try
		{
			File file = new File(inFile);
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream i = new ObjectInputStream(f);
	
			MutatingDecisionTree inTree = (MutatingDecisionTree)i.readObject();
			i.close();
			f.close();
			
			NewMutatingDecisionTree outTree = new NewMutatingDecisionTree(inTree.validStates);
			
			//Translate
			CompleteSplitDecision inNode = inTree.root;
			IfElseNode outNode = outTree.getRoot();
			translate(inNode, outNode);
			
			outTree.reindexTree();
			file = new File(outFile);
			FileOutputStream f2 = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f2);
	
			o.writeObject(outTree);
			o.close();
			f2.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void translate(CompleteSplitDecision in, IfElseNode out)
	{
		if (in.terminal)
		{
			out.terminal = true;
			out.terminalValue = in.terminalValue;
			return;
		}
		
		boolean hasElse = (in.children.size() > in.splits.size());
		for (int i = 0; i < in.splits.size();)
		{
			out.terminal = false;
			out.address = in.address;
			out.checkValue = in.splits.get(i);
			
			IfElseNode newNode = new IfElseNode();
			out.left = newNode;
			newNode.parent = out;
			translate(in.children.get(i), newNode);
			
			++i;
			if (i < in.splits.size())
			{
				newNode = new IfElseNode();
				out.right = newNode;
				newNode.parent = out;
				out = newNode;
			}
		}
		
		//Else case goes in out.right
		if (hasElse)
		{
			IfElseNode newNode = new IfElseNode();
			out.right = newNode;
			newNode.parent = out;
			translate(in.children.get(in.splits.size()), newNode);
		}
		else
		{
			IfElseNode newNode = new IfElseNode();
			out.right = newNode;
			newNode.parent = out;
			newNode.terminal = true;
			newNode.terminalValue = in.majority;
		}
	}
}
