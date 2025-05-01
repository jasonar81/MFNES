import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MutatingDecisionTree implements Serializable {
private static final long serialVersionUID = -6732487624928621347L;

	public ArrayList<Integer> validStates;
	public CompleteSplitDecision root;
	public int treeSize;
	public CompleteSplitDecision backup;
}
