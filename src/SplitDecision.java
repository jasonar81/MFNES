//For building decision trees

import java.util.ArrayList;

public class SplitDecision {
	ArrayList<Integer> availableFeatures = new ArrayList<Integer>();
	CompleteSplitDecision parent;
	ArrayList<SplitData> splitData = new ArrayList<SplitData>();
	Filter filter;
	int inProgressFeature;
}
