package Test;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.apromore.prodrift.util.XLogManager;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import java.util.*;
import org.apache.commons.math3.stat.inference.GTest;
import org.apromore.prodrift.config.DriftDetectionSensitivity;
import org.apromore.prodrift.util.Utils;

public class DriftDetector {
	
	public static int consecutive = 2;
	public static int numThreads = 1;
	public static double[] pvalues;
	public static HashMap<String, Double>[] asrs;
	
	private static int refWindowStart = 0;
	private static int refWindowEnd = 0;
	private static int detectWindowStart = 0;
	private static int detectWindowEnd = 0;
	private static Graph refDfg = null;
	private static XLog refLog = null;
	private static HashSet<Integer> noisesPoint = new HashSet<>();
	public static HashMap<String, Integer> trace_mapper = new HashMap<>();
	//private static Graph decDfg = null;
	private static Graph decStartDfg = null;
	public static ArrayList<Integer> dfgAddedPoints = new ArrayList<>();
	public static ArrayList<Integer> dfgRemovedPoints = new ArrayList<>();
	public static ArrayList<Integer> dfgChangePoints = new ArrayList<>();
	public static HashMap<String, ArrayList<Binding>> bindings = new HashMap<>(); 
	public static HashMap<String, Long> df_1;
	public static HashMap<String, Long> df_2;
	public static HashMap<String, Integer> df_index;
	public static boolean evaluate = true;
	
	
	public static long[] observed1;
	public static long[] observed2;
	//public static double[] observed1;
	//public static double[] observed2;
	public static double[] expected1;
	public static double[] expected2;
	
	public static XLog stream;
	public static XLog windowLog;
	public static Graph dfg;
	public static HashMap<String, Integer> mapper;
	
	public static void initialise() {
	
		noisesPoint = new HashSet<>();
		refWindowStart = 0;
		refWindowEnd = 0;
		detectWindowStart = 0;
		detectWindowEnd = 0;
		refDfg = null;
		refLog = null;
		trace_mapper = new HashMap<>();
		//private static Graph decDfg = null;
		decStartDfg = null;
		dfgAddedPoints = new ArrayList<>();
		dfgRemovedPoints = new ArrayList<>();
		dfgChangePoints = new ArrayList<>();
		bindings = new HashMap<>(); 
	}
	
	public static void calculateExpected() {
		expected1 = new double[observed1.length];
		expected2 = new double[observed1.length];
		//long[] sums = new long[observed1.length];
		//long sum1 = Arrays.stream(observed1).sum();
		//long sum2 = Arrays.stream(observed2).sum();
		//long total = sum1 + sum2;
		double[] sums = new double[observed1.length];
		double sum1 = Arrays.stream(observed1).sum();
		double sum2 = Arrays.stream(observed2).sum();
		double total = sum1 + sum2;
		
		for(int i = 0;i < sums.length;i ++) {
			sums[i] = observed1[i] + observed2[i];
		}
		
		for(int i = 0;i < expected1.length;i ++) {
			expected1[i] = ((double)sums[i] / total) * sum1;
			expected2[i] = ((double)sums[i] / total) * sum2;
		}
		
		
	}
	
	public static double asr(int index) {
		//long[] sums = new long[observed1.length];
		double[] sums = new double[observed1.length];
		//long sum1 = Arrays.stream(observed1).sum();
		//long sum2 = Arrays.stream(observed2).sum();
		double sum1 = Arrays.stream(observed1).sum();
		double sum2 = Arrays.stream(observed2).sum();
		double total = sum1 + sum2;
		
		for(int i = 0;i < sums.length;i ++) {
			sums[i] = observed1[i] + observed2[i];
		}
		//System.out.println(observed2[index]);
		//System.out.println(expected2[index]);
		//System.out.println(sum2);
		//System.out.println(sums[index]);
		
		//return ((double)observed2[index] - expected2[index])/Math.sqrt(expected2[index] * (1 - (double)expected2[index]/sum2) * (1 - (double)expected2[index]/sums[index]));
		return ((double)observed2[index] - expected2[index])/Math.sqrt(expected2[index] * (1 - (double)sum2/total) * (1 - (double)sums[index]/total));
	}
	
	public static void df_count(Graph g1, Graph g2) {
		df_1 = new HashMap<>();
		df_2 = new HashMap<>();
		df_index = new HashMap<>();
		
		int current_index = 0;
		for(Node s:g1.nodes.values()) {
			for(Node t:s.outgoingEdges.keySet()) {
				//if(s.outgoingEdges.get(t) > 5) {
					String name = s.activity + "," + t.activity;
					df_1.put(name, s.outgoingEdges.get(t));
					if(df_index.get(name) == null) {
						df_index.put(name, current_index);
						current_index ++;
					}
				//}
				
			}
		}
		
		for(Node s:g2.nodes.values()) {
			for(Node t:s.outgoingEdges.keySet()) {
				//if(s.outgoingEdges.get(t) > 5) {
					String name = s.activity + "," + t.activity;
					df_2.put(name, s.outgoingEdges.get(t));
					if(df_index.get(name) == null) {
						df_index.put(name, current_index);
						current_index ++;
					}
				//}
			}
		}
		
		observed1 = new long[df_index.keySet().size()];
		observed2 = new long[df_index.keySet().size()];
		
		for(int i = 0;i < observed1.length;i ++) {
			observed1[i] = 0;
			observed2[i] = 0;
		}
		
		for(int i = 0;i < df_1.keySet().size();i ++) {
			int index = df_index.get(df_1.keySet().toArray()[i]);
			observed1[index] = df_1.get(df_1.keySet().toArray()[i]);
		}
		
		for(int i = 0;i < df_2.keySet().size();i ++) {
			int index = df_index.get(df_2.keySet().toArray()[i]);
			observed2[index] = df_2.get(df_2.keySet().toArray()[i]);
		}
		
	}
	
	public static double gtest() {
		
		
		GTest tester = new GTest();
		
		double pValue = 1;
		try {
			pValue = tester.gTestDataSetsComparison(observed1, observed2);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		for(int i = 0;i < observed1.length;i ++) {
			
		}
		for(int i = 0;i < observed1.length;i ++) {
			
		}
	
		return pValue;
	}
	
	
	public static void detectDriftForward(int wsize, XLog eventstream) {
		if(!evaluate) {
			System.out.println("Forward detection: ");
		}
		
		pvalues = new double[eventstream.size()];
		asrs = new HashMap[eventstream.size()];
		for(int i = 0;i < asrs.length;i ++) {
			asrs[i] = null;;
		}
		
		for(int i = 0;i < pvalues.length;i ++) {
			pvalues[i] = 1;
		}
		stream = eventstream;
		refWindowEnd = wsize - 1;
		detectWindowStart = refWindowEnd + 1;
		
		ArrayList<MultithreadTest> threads = new ArrayList<>();
		for(int i = 0;i < numThreads;i ++) {
			int interval = (int) Math.ceil(eventstream.size() / numThreads);
			MultithreadTest thread = new MultithreadTest(i * interval, i * interval + interval, wsize);
			threads.add(thread);
			thread.start();
		}
		
		for(Thread thread:threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//Stage 1: Compare df relations
		//ArrayList<ActivityPair> removed = null;
		ActivityPair added = null;
		
					
		//decStartDfg = buildDfg(refWindowStart, detectWindowStart, stream);
		
		windowLog = refLog;
		dfg = refDfg;
		mapper = trace_mapper;
		buildNewWindow(0, refWindowEnd);
		refLog = windowLog;
		refDfg = dfg;
		trace_mapper = mapper;
	
		while(detectWindowStart < stream.size()) {
			added = null;
			
			if(refWindowEnd == -1) {
				break;
			}
				
			ActivityPair newDfr = getNewDfr(stream, refLog, detectWindowStart);
			if(newDfr != null) {
				if(!checkDfr(newDfr.from, newDfr.to, refDfg)) {
					added = newDfr;
				}
			}
			
			
			
			int addedPoint = -1;
			
			Graph refDfgTest = null;
			XLog refLogTest = null;
			HashMap<String, Integer> ref_mapper = new HashMap<>();
			
			Graph decDfgTest = null;
			XLog decLogTest = null;
			HashMap<String, Integer> dec_mapper = new HashMap<>();
			
			if(added != null) {
				
				if(detectWindowStart + wsize - 1 >= stream.size()) {
					break;
				}
				
				
				addedPoint = detectWindowStart;
				
			}
			
			
			if(addedPoint != -1) {
			
				//System.out.println((double)addedPoint/stream.size() * 100 + "%");
				boolean fil = true;
				int asrCount = 0;
				
				
				
				for(int i = 0;i < wsize / consecutive * 2;i ++) {
					
					double p = 1;
					if(addedPoint + wsize - 1 + i - wsize / consecutive >= stream.size()) {
						fil = false;
						break;
					}
					
					if(addedPoint - wsize + i - wsize / consecutive < 0) {
						fil = false;
						break;
					}
					
					boolean passTest = true;
					if(i == 0) {
						
						for(int a = 0;a < wsize / consecutive * 2;a ++) {
							if(addedPoint - wsize + a - wsize / consecutive >= stream.size()) {
								passTest = false;
								break;
							}
							if(pvalues[addedPoint - wsize + a - wsize / consecutive] > 0.05) {
								passTest = false;
								break;
							}
						}
					}
					
					if(!passTest) {
						fil = false;
						break;
					}
					
					
				
					
					double asr = 1;
					try {
						asr = asrs[addedPoint - wsize + i - wsize / consecutive].get(added.from + "," + added.to);
					}
					catch(Exception e) {}
					
					
					if(asr > 1.96) {
						//nn = true;							
						asrCount ++;
						//break;
					}
					else {
						fil = false;
						//System.out.println("asr fail");
						break;
					}
						
					//}
					
					
					
				}
				
						
				if(fil) {
					if(!evaluate) {
						System.out.print("Event: ");
						System.out.println(addedPoint);
						System.out.println(stream.get(addedPoint).get(0).getAttributes().get("time:timestamp").toString());
						System.out.println(added.from + " -> " + added.to);
					}
					
						
					
					dfgAddedPoints.add(addedPoint);
					dfgChangePoints.add(addedPoint);
					refWindowStart = addedPoint;
					refWindowEnd = refWindowStart + wsize;
					detectWindowStart = refWindowEnd + 1;
					
					windowLog = refLog;
					dfg = refDfg;
					mapper = trace_mapper;
					buildNewWindow(refWindowStart, refWindowEnd);
					refLog = windowLog;
					refDfg = dfg;
					trace_mapper = mapper;
				}
				else {
					noisesPoint.add(addedPoint);
					refWindowStart ++;
				    refWindowEnd ++;
					detectWindowStart ++;
					
					windowLog = refLog;
					dfg = refDfg;
					mapper = trace_mapper;
					moveWindow(refWindowStart, refWindowEnd);
					refLog = windowLog;
					refDfg = dfg;
					trace_mapper = mapper;
				}
				
				
			}
			
			else {
				refWindowStart ++;
				refWindowEnd ++;
				detectWindowStart ++;
				
				windowLog = refLog;
				dfg = refDfg;
				mapper = trace_mapper;
				moveWindow(refWindowStart, refWindowEnd);
				refLog = windowLog;
				refDfg = dfg;
				trace_mapper = mapper;
			}
					
		}
				
	}
	
	public static void detectDriftBackward(int wsize, XLog eventstream) {
		
		noisesPoint = new HashSet<>();
		stream = eventstream;
		XLog xlReverse = XLogManager.orderByTraceCompletionTimeStamp(stream);
		xlReverse.clear();
				
		for(XTrace current:stream) {
			xlReverse.add(0, current);
		}
		//System.out.println(xlReverse.size());
		stream = xlReverse;
		
		if(!evaluate) {
			System.out.println("Backward detection: ");
		}
		
		refWindowStart = 0;
		refWindowEnd = refWindowStart + wsize - 1;
		detectWindowStart = refWindowEnd + 1;
		
		//ArrayList<ActivityPair> removed = null;
		ActivityPair added = null;
		
		
		
		windowLog = refLog;
		dfg = refDfg;
		mapper = trace_mapper;
		buildNewWindow(0, refWindowEnd);
		refLog = windowLog;
		refDfg = dfg;
		trace_mapper = mapper;
		
		//System.out.println(checkDfrCount("Permit FINAL_APPROVED by SUPERVISOR", "Request For Payment SUBMITTED by EMPLOYEE", refDfg));
		
		//System.out.println(refLog);
		//decDfg = buildDfg(0, xl.size() - 1, xl);
		//move forward: finding new df relations
		while(detectWindowStart < stream.size()) {
			added = null;
			//System.out.println(checkDfrCount("Permit FINAL_APPROVED by SUPERVISOR", "Request For Payment SUBMITTED by EMPLOYEE", refDfg));
			//System.out.println(dfg.nodes.size());
			//System.out.println(detectWindowStart);
			if(refWindowEnd == -1) {
				break;
			}
			//System.out.println(refWindowStart + " - " + refWindowEnd + "VS " + detectWindowStart);
				
			ActivityPair newDfr = getNewDfr(stream, refLog, detectWindowStart);
			if(newDfr != null) {
				if(!checkDfr(newDfr.from, newDfr.to, refDfg)) {
					added = newDfr;
				}
			}
			
			
			
			int addedPoint = -1;
			
			Graph refDfgTest = null;
			XLog refLogTest = null;
			HashMap<String, Integer> ref_mapper = new HashMap<>();
			
			Graph decDfgTest = null;
			XLog decLogTest = null;
			HashMap<String, Integer> dec_mapper = new HashMap<>();
			
			if(added != null) {
				
				if(detectWindowStart + wsize - 1 >= stream.size()) {
					break;
				}
				
				
				
				addedPoint = detectWindowStart;
				
			}
			
			
			if(addedPoint != -1) {
			
				//System.out.println((double)addedPoint/stream.size() * 100 + "%");
				boolean fil = true;
				int asrCount = 0;
				
				
				
				for(int i = 0;i < wsize / consecutive * 2;i ++) {
					double p = 1;
					if(addedPoint + wsize - 1 + i - wsize / consecutive >= stream.size()) {
						fil = false;
						break;
					}
					
					if(addedPoint - wsize + i - wsize / consecutive < 0) {
						fil = false;
						break;
					}
					
					boolean passTest = true;
					if(i == 0) {
						for(int a = 0;a < wsize / consecutive * 2;a ++) {
							if(stream.size() - (addedPoint + wsize - 1 + a - wsize / consecutive) + 1 < 0) {
								passTest = false;
								break;
							}
							if(pvalues[stream.size() - (addedPoint + wsize - 1 + a - wsize / consecutive) + 1] > 0.05) {
								passTest = false;
								break;
							}
						}
					}
					
					if(!passTest) {
						fil = false;
						break;
					}
					
					double asr = 1;
					try {
						asr = asrs[stream.size() - (addedPoint + wsize - 1 + i - wsize / consecutive) + 1].get(added.to + "," + added.from);
					}
					catch(Exception e) {
						
					}
					
					
					if(asr * -1 > 1.96) {
						//nn = true;							
						asrCount ++;
						//break;
					}
					else {
						//System.out.println("asr fail");
						fil = false;
						break;
					}
						
					//}
					
					
					
				}
				
				
						
				if(fil) {
					if(!evaluate) {
						System.out.print("Event: ");
						System.out.println(stream.size() - addedPoint);
						System.out.println(stream.get(addedPoint).get(0).getAttributes().get("time:timestamp").toString());
						System.out.println(added.to + " -> " + added.from);
					}
				
						
					
					dfgAddedPoints.add(stream.size() - addedPoint);
					dfgChangePoints.add(stream.size() - addedPoint);
					refWindowStart = addedPoint;
					refWindowEnd = refWindowStart + wsize;
					detectWindowStart = refWindowEnd + 1;
					
					windowLog = refLog;
					dfg = refDfg;
					mapper = trace_mapper;
					buildNewWindow(refWindowStart, refWindowEnd);
					refLog = windowLog;
					refDfg = dfg;
					trace_mapper = mapper;
				}
				else {
					noisesPoint.add(addedPoint);
					refWindowStart ++;
				    refWindowEnd ++;
					detectWindowStart ++;
					
					windowLog = refLog;
					dfg = refDfg;
					mapper = trace_mapper;
					moveWindow(refWindowStart, refWindowEnd);
					refLog = windowLog;
					refDfg = dfg;
					trace_mapper = mapper;
				}
				
				
			}
			
			else {
				refWindowStart ++;
				refWindowEnd ++;
				detectWindowStart ++;
				
				windowLog = refLog;
				dfg = refDfg;
				mapper = trace_mapper;
				moveWindow(refWindowStart, refWindowEnd);
				refLog = windowLog;
				refDfg = dfg;
				trace_mapper = mapper;
			}
					
		}
		
		
		
				
		
	}
	
	
	public static boolean checkDfr(String from, String to, Graph dfg) {
		
		Node source = dfg.nodes.get(from);
		if(source == null) {
			return false;
		}
		
		if(!source.outgoingActivities.contains(to)) {
			return false;
		}
		
		return true;
	}
	
	public static long checkDfrCount(String from, String to, Graph dfg) {
		
		Node source = dfg.nodes.get(from);
		if(source == null) {
			return 0;
		}
		
		if(!source.outgoingActivities.contains(to)) {
			return 0;
		}
		
		return source.outgoingEdges.get(dfg.nodes.get(to));
	}

	public static ArrayList<ActivityPair> compareDfrAdded(Graph ref, Graph dec) {
		
		ArrayList<ActivityPair> added = new ArrayList<>();
		
		//find added df relations
		for(Node a:dec.nodes.values()) {
			for(Node b:a.outgoingEdges.keySet()) {
						
				Node aRef = ref.nodes.get(a.activity);
				//check if the start activity exists in dec
				if(aRef == null) {
					added.add(new ActivityPair(a.activity, b.activity));
				}
				else if(!aRef.outgoingActivities.contains(b.activity)) {
					added.add(new ActivityPair(a.activity, b.activity));
				}
						
			}
		}		
		
		return added;
	}
	
	public static Graph buildDfg(int startIndex, int endIndex, XLog stream) {
		
		mapper = new HashMap<>((int)(stream.getClassifiers().size() * stream.getClassifiers().size() / 0.75) + 1);
		Graph newDfg = new Graph();
		XLog xl = XLogManager.getSubLog_eventBased(stream, startIndex, endIndex + 1);
		windowLog = xl;
		
		for(int i = 0;i < windowLog.size();i ++) {
			//String trace_id = XLogManager.getTraceID(windowLog.get(i));
			String trace_id = XLogManager.getTraceID(windowLog.get(i));
			//System.out.println(trace_id);
			mapper.put(trace_id, i);
			
			
		}
		
		for(int i = 0;i < xl.size();i ++) {
			XTrace currentTrace = xl.get(i);
			Iterator<XEvent> currentTraceIterator = currentTrace.iterator();
			XEvent currentEvent = currentTraceIterator.next();
			Node currentNode = newDfg.nodes.get(currentEvent.getAttributes().get("concept:name").toString());
			
			if(currentNode == null) {
				newDfg.nodes.put(currentEvent.getAttributes().get("concept:name").toString(), new Node(currentEvent.getAttributes().get("concept:name").toString()));
			}
			
			while(currentTraceIterator.hasNext()) {
			
				//System.out.println(currentEvent.getAttributes());
				XEvent nextEvent = currentTraceIterator.next();
				String currentEventName = currentEvent.getAttributes().get("concept:name").toString();
				String nextEventName = nextEvent.getAttributes().get("concept:name").toString();
				
				//check if the activity is already in the dfg		
				Node nextNode = newDfg.nodes.get(nextEventName);
				currentNode = newDfg.nodes.get(currentEventName);
				if(nextNode == null) {
					newDfg.nodes.put(nextEventName, new Node(nextEventName));
					nextNode = newDfg.nodes.get(nextEventName);
				}
				
				if(currentNode.outgoingEdges.get(nextNode) == null) {
				
					currentNode.outgoingEdges.put(nextNode, (long) 1);
					currentNode.outgoingActivities.add(nextNode.activity);
				}
				else {
					currentNode.outgoingEdges.put(nextNode, currentNode.outgoingEdges.get(nextNode) + 1);
					
				}
				
				
				
				currentEvent = nextEvent;
				
			}
		}
		
		return newDfg;
	}
	
	public static ActivityPair getNewDfr(XLog stream, XLog xl, int index) {
		//System.out.println(index);
		XTrace current = stream.get(index);
		String trace_id = current.getAttributes().get("concept:name").toString();
		ActivityPair newDfr = null;
		int trace_index = -1;
		if(trace_mapper.get(trace_id) != null) {
			trace_index = trace_mapper.get(trace_id);
			XTrace trace = refLog.get(trace_index);
			if(trace.size() > 0) {
				String from = trace.get(trace.size() - 1).getAttributes().get("concept:name").toString();
				String to = current.get(0).getAttributes().get("concept:name").toString();
				newDfr = new ActivityPair(from, to);
				
			}
			
		}
		
		return newDfr;
	}
	
	public static void updateDfgAdd(Graph graph, ActivityPair dfr) {
		
		String from = dfr.from;
		String to = dfr.to;
		//System.out.println(from);
		//System.out.println(to);
		Node source = dfg.nodes.get(from);
		Node target = dfg.nodes.get(to);
		if(source == null) {
			dfg.nodes.put(from, new Node(from));
		}
		
		if(target == null) {
			dfg.nodes.put(to, new Node(to));
		}
		
		source = dfg.nodes.get(from);
		target = dfg.nodes.get(to);
		
		if(!source.outgoingActivities.contains(to)) {
			source.outgoingActivities.add(to);
			source.outgoingEdges.put(target, (long) 1);
		}
		else {
			long count = source.outgoingEdges.get(target);
			source.outgoingEdges.put(target, count + 1);
		}
		
		
	}
	
	public static void updateDfgRemove(Graph graph, ActivityPair dfr) {
		String from = dfr.from;
		String to = dfr.to;
		//System.out.print(from);
		//System.out.println( "- >" + to);
		Node source = dfg.nodes.get(from);
		Node target = dfg.nodes.get(to);	
		long count = source.outgoingEdges.get(target);
		if(count == 1) {
			source.outgoingEdges.remove(target);
			source.outgoingActivities.remove(to);
		}
		else {
			source.outgoingEdges.put(target, count - 1);
				
		}		
		
	}

	public static void buildNewWindow(int startIndex, int endIndex) {
		
		
		dfg = buildDfg(startIndex, endIndex, stream);
	}
	
	public static void moveWindow(int startIndex, int endIndex) {
		
		//System.out.println(trace_mapper.get("declaration 86920"));
		//System.out.println(endIndex);
		XTrace currentAdd = stream.get(endIndex);
		String trace_id_add = currentAdd.getAttributes().get("concept:name").toString();
		ActivityPair newDfr = null;
		int trace_index = -1;
		//System.out.println(windowLog.size());
		if(mapper.get(trace_id_add) != null) {
			trace_index = mapper.get(trace_id_add);
			XTrace trace = windowLog.get(trace_index);
			trace.add(currentAdd.get(0));
			if(trace.size() > 1) {
				String from = trace.get(trace.size() - 2).getAttributes().get("concept:name").toString();
				String to = trace.get(trace.size() - 1).getAttributes().get("concept:name").toString();
				newDfr = new ActivityPair(from, to);
			}
			
			
		}
		
		else {
			XTraceImpl xTraceImpl = new XTraceImpl((XAttributeMap)currentAdd.getAttributes().clone());
			xTraceImpl.add(currentAdd.get(0));
			windowLog.add(xTraceImpl);
			mapper.put(trace_id_add, windowLog.size() - 1);
		}
		
		XTrace currentRemove = stream.get(startIndex - 1);
		String trace_id_remove = currentRemove.getAttributes().get("concept:name").toString();
		ActivityPair oldDfr = null;
		trace_index = -1;
		
		
		//System.out.println(trace_id_remove);
		//System.out.println(currentRemove.get(0).getAttributes().get("concept:name").toString());
		trace_index = mapper.get(trace_id_remove);
		//System.out.println(trace_id_remove);
		XTrace trace = windowLog.get(trace_index);
		if(trace.size() > 1) {
			String from = trace.get(0).getAttributes().get("concept:name").toString();
			String to = trace.get(1).getAttributes().get("concept:name").toString();
			oldDfr = new ActivityPair(from, to);
		}
		
		if(newDfr != null) {
			
			//System.out.println(newDfr.from + ";" + newDfr.to);
			if(dfg == refDfg && (!noisesPoint.contains(endIndex))) {
				updateDfgAdd(dfg, newDfr);
			}
			
			else if(dfg != refDfg) {
				updateDfgAdd(dfg, newDfr);
			}
			
		}
		
		if(oldDfr != null) {
			trace.remove(0);
			
			try {
				updateDfgRemove(dfg, oldDfr);
			}
			catch(Exception e) {
				
			}
				
		}
		else {
			trace.remove(0);
		}
		
		
	}
}
