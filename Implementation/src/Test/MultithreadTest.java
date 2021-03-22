package Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.math3.stat.inference.GTest;
import org.apromore.prodrift.config.DriftDetectionSensitivity;
import org.apromore.prodrift.util.Utils;
import org.apromore.prodrift.util.XLogManager;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XTraceImpl;

public class MultithreadTest extends Thread{
	
	private Graph refDfg = null;
	private XLog refLog = null;
	private HashSet<Integer> noisesPoint = new HashSet<>();
	public HashMap<String, Integer> trace_mapper = new HashMap<>();
	private int refWindowEnd = 0;
	public XLog stream;
	public XLog windowLog;
	public Graph dfg;
	public HashMap<String, Integer> mapper;
	public int startIndex;
	public int endIndex;
	public int wsize;
	public long[] observed1;
	public long[] observed2;
	public HashMap<String, Long> df_1;
	public HashMap<String, Long> df_2;
	public HashMap<String, Integer> df_index;
	public double[] expected1;
	public double[] expected2;
	public double[] sums;
	public boolean needReindex = true;
	public boolean needReindex1 = true;
	//public boolean needReindex2 = true;
	String affectedDfAdd = null;
	String affectedDfRemove = null;
	double sum1;
	double sum2;
	double total;
	
	public MultithreadTest(int startIndex, int endIndex, int wsize) {
		this.startIndex = startIndex;
		this.wsize = wsize;
		stream = DriftDetector.stream;
		this.endIndex = endIndex;
	}
	
	public void run() {
		
		Graph refDfgTest = null;
		XLog refLogTest = null;
		HashMap<String, Integer> ref_mapper = null;
		
		Graph decDfgTest = null;
		XLog decLogTest = null;
		HashMap<String, Integer> dec_mapper = null;
		
		
		for(int i = startIndex;i < endIndex;i ++) {
			
			if(i + wsize * 2 - 1 >= stream.size()) {
				return;
			}
			
			if(i == startIndex) {
				windowLog = refLogTest;
				dfg = refDfgTest;
				mapper = ref_mapper;
				buildNewWindow(i, i + wsize -1);
				refLogTest = windowLog;
				refDfgTest = dfg;
				ref_mapper = mapper;
				
				windowLog = decLogTest;
				dfg = decDfgTest;
				mapper = dec_mapper;
				buildNewWindow(i + wsize, i + wsize * 2 -1);
				decLogTest = windowLog;
				decDfgTest = dfg;
				dec_mapper = mapper;
				
				
			}
			
			else {
				needReindex = true;
				needReindex1 = true;
				affectedDfAdd = null;
				affectedDfRemove = null;
				
				windowLog = refLogTest;
				dfg = refDfgTest;
				mapper = ref_mapper;
				moveWindow(i, i + wsize -1);
				//System.out.println(refWindowEnd);
				//System.out.println(addedPoint - 1 + i);
				refLogTest = windowLog;
				refDfgTest = dfg;
				ref_mapper = mapper;
				
				boolean a = false;
				
				if(!needReindex) {
					if(affectedDfAdd != null) {
						observed1[df_index.get(affectedDfAdd)] ++;
					}
					if(affectedDfRemove != null) {
						observed1[df_index.get(affectedDfRemove)] --;
					}
					a = true;
					needReindex = true;
					needReindex1 = true;
					affectedDfAdd = null;
					affectedDfRemove = null;
				}
				
				
				windowLog = decLogTest;
				dfg = decDfgTest;
				mapper = dec_mapper;
				moveWindow(i + wsize, i + wsize * 2 -1);
				decLogTest = windowLog;
				decDfgTest = dfg;
				dec_mapper = mapper;
				
				if(!needReindex) {
					if(affectedDfAdd != null) {
						observed2[df_index.get(affectedDfAdd)] ++;
					}
					if(affectedDfRemove != null) {
						observed2[df_index.get(affectedDfRemove)] --;
					}
					
					if(!a) {
						needReindex = true;
					}
				}
				
			}
			
			df_count(refDfgTest, decDfgTest);
			double p = gtest();
			//System.out.println(p);
			DriftDetector.pvalues[i] = p;
			DriftDetector.asrs[i] = new HashMap<>((int)(df_index.keySet().size() / 0.75) + 1);
			if(p <= 0.05) {
				
			
				calculateExpected();
				
				for(String x:df_index.keySet()) {
					
					DriftDetector.asrs[i].put(x, asr(df_index.get(x)));
				}
				
				
			}
			
			
		}
		
	}
	
	public double gtest() {
		
		
		GTest tester = new GTest();
		
		double fScore = 1;
		try {
			fScore = tester.gTestDataSetsComparison(observed1, observed2);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return fScore;
	}
	
	
	public void calculateExpected() {
		expected1 = new double[observed1.length];
		expected2 = new double[observed1.length];
		sums = new double[observed1.length];
		sum1 = Arrays.stream(observed1).sum();
		sum2 = Arrays.stream(observed2).sum();
		total = sum1 + sum2;
		
		for(int i = 0;i < sums.length;i ++) {
			sums[i] = observed1[i] + observed2[i];
		}
		
		for(int i = 0;i < expected1.length;i ++) {
			expected1[i] = ((double)sums[i] / total) * sum1;
			expected2[i] = ((double)sums[i] / total) * sum2;
		}
		
		
		
	}
	
	public double asr(int index) {
	
		return ((double)observed2[index] - expected2[index])/Math.sqrt(expected2[index] * (1 - (double)sum2/total) * (1 - (double)sums[index]/total));
	}
	
	public void df_count(Graph g1, Graph g2) {
		
		
		if(needReindex) {
			df_index = new HashMap<>((int)(g1.nodes.size() * g2.nodes.size()/0.75) + 1);
			
			int current_index = 0;
			for(Node s:g1.nodes.values()) {
				for(Node t:s.outgoingEdges.keySet()) {
					
						String name = s.activity + "," + t.activity;
					
						if(df_index.get(name) == null) {
							df_index.put(name, current_index);
							current_index ++;
						}
				
					
				}
			}
			
			for(Node s:g2.nodes.values()) {
				for(Node t:s.outgoingEdges.keySet()) {
					
						String name = s.activity + "," + t.activity;
						
						if(df_index.get(name) == null) {
							df_index.put(name, current_index);
							current_index ++;
						}
				
				}
			}
			
			observed1 = new long[df_index.keySet().size()];
			observed2 = new long[df_index.keySet().size()];
			
			for(String i:df_index.keySet()) {
				String from = i.split(",")[0];
				String to = i.split(",")[1];
				int index = df_index.get(i);
				try {
					observed1[index] = g1.nodes.get(from).outgoingEdges.get(g1.nodes.get(to));
				}
				catch(NullPointerException e) {
					observed1[index] = 0;
				}
				
				try {
					observed2[index] = g2.nodes.get(from).outgoingEdges.get(g2.nodes.get(to));
				}
				catch(NullPointerException e) {
					observed2[index] = 0;
				}
				
			}
			
			
		}
		
		
		
		
	}
	public void buildNewWindow(int startIndex, int endIndex) {
		
		
		dfg = buildDfg(startIndex, endIndex, stream);
	}
	
	public Graph buildDfg(int startIndex, int endIndex, XLog stream) {
		
		mapper = new HashMap<>((int)(stream.getClassifiers().size() * stream.getClassifiers().size() / 0.75) + 1);
		Graph newDfg = new Graph();
		
		XLog xl = XLogManager.getSubLog_eventBased(stream, startIndex, endIndex + 1);
		windowLog = xl;
		
		for(int i = 0;i < windowLog.size();i ++) {
			
			String trace_id = XLogManager.getTraceID(windowLog.get(i));
			
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
	
	public void moveWindow(int startIndex, int endIndex) {
		
		
		XTrace currentAdd = stream.get(endIndex);
		String trace_id_add = currentAdd.getAttributes().get("concept:name").toString();
		ActivityPair newDfr = null;
		int trace_index = -1;
		
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
		
		
	
		trace_index = mapper.get(trace_id_remove);
		
		XTrace trace = windowLog.get(trace_index);
		if(trace.size() > 1) {
			String from = trace.get(0).getAttributes().get("concept:name").toString();
			String to = trace.get(1).getAttributes().get("concept:name").toString();
			oldDfr = new ActivityPair(from, to);
		}
		
		
		
		if(newDfr != null) {
			
			
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

	public void updateDfgRemove(Graph graph, ActivityPair dfr) {
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
			needReindex = true;
			affectedDfRemove = null;
		}
		else {
			source.outgoingEdges.put(target, count - 1);
			if(needReindex1 == false) {
				needReindex = false;
			}
			
			affectedDfRemove  = dfr.from + "," + dfr.to;
				
		}		
		
	}

	public void updateDfgAdd(Graph graph, ActivityPair dfr) {
		
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
			//needReindex = true;
			needReindex1 = true;
			affectedDfAdd = null;
		}
		else {
			long count = source.outgoingEdges.get(target);
			source.outgoingEdges.put(target, count + 1);
			//needReindex = false;
			needReindex1 = false;
			affectedDfAdd = dfr.from + "," + dfr.to;
		}
		
		
	}
}
