package Test;
import java.util.*;

public class Node {
	
	public String activity;
	public HashMap<Node, Long> outgoingEdges;
	public HashSet<String> outgoingActivities;
	//public HashMap<Node, Long> incommingEdges;
	//public HashSet<String> incommingActivities;
	
	public Node(String activity) {
		outgoingEdges = new HashMap<>(100);
		//incommingEdges = new HashMap<>();
		outgoingActivities = new HashSet<>(100);
		//incommingActivities = new HashSet<>();
		this.activity = activity;
	}
	
	
}
