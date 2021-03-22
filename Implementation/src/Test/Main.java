package Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.apromore.prodrift.util.XLogManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apromore.prodrift.util.LogStreamer;


public class Main {
	
	private static HashMap<Integer, Integer> eventTraceMapper = new HashMap<>();
	public static int consecutive = 0;
	public static int windowSize = 0;
	public static String label = "add";
	public static boolean evaluation = true;
	
	public static void main(String[] args) throws IOException {
			
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of available CPU cores: " + cores);
		if(args[0].equals("evaluateAll")) {
			if(args.length > 2) {
				cores = Integer.parseInt(args[2]);
				System.out.println("Number of threads: " + cores);
			}
			evaluation = true;
			label = args[1];
			
			if(!label.equals("add") && !label.equals("remove") && !label.equals("noise-free")) {
				System.out.println("Invalid input.");
				System.exit(0);
			}
			
			runexp(2, 500, cores);
			runexp(2, 1000, cores);
			runexp(2, 1500, cores);
			runexp(2, 2000, cores);
			runexp(2, 2500, cores);
			runexp(2, 3000, cores);
			
			runexp(3, 500, cores);
			runexp(3, 1000, cores);
			runexp(3, 1500, cores);
			runexp(3, 2000, cores);
			runexp(3, 2500, cores);
			runexp(3, 3000, cores);
			
			runexp(4, 500, cores);
			runexp(4, 1000, cores);
			runexp(4, 1500, cores);
			runexp(4, 2000, cores);
			runexp(4, 2500, cores);
			runexp(4, 3000, cores);
			
			runexp(5, 500, cores);
			runexp(5, 1000, cores);
			runexp(5, 1500, cores);
			runexp(5, 2000, cores);
			runexp(5, 2500, cores);
			runexp(5, 3000, cores);
		}
		
		if(args[0].equals("evaluate")) {
			if(args.length > 4) {
				cores = Integer.parseInt(args[4]);
				System.out.println("Number of threads: " + cores);
			}
			evaluation = true;
			label = args[1];
			if(!label.equals("add") && !label.equals("remove") && !label.equals("noise-free")) {
				System.out.println("Invalid input.");
				System.exit(0);
			}
			int cons = Integer.parseInt(args[2]);
			int win = Integer.parseInt(args[3]);
			runexp(cons, win, cores);
		}
		
		if(args[0].equals("apply")) {
			if(args.length > 4) {
				cores = Integer.parseInt(args[4]);
				System.out.println("Number of threads: " + cores);
			}
			label = "";
			String fileName = args[1];
			DriftDetector.initialise();
			DriftDetector.evaluate = false;
			DriftDetector.numThreads = cores;
			consecutive = Integer.parseInt(args[2]);
			windowSize = Integer.parseInt(args[3]);
			System.out.println(fileName);
			System.out.println(consecutive + ", ws: " + windowSize);
			Path path = null;
			XLog xl = null;
			path = Paths.get(fileName, new String[0]);
			try {
				xl = XLogManager.readLog(new FileInputStream(path.toString()), path.getFileName().toString());
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		
			DriftDetector.dfgChangePoints = new ArrayList<>();
			long startTime = System.nanoTime();
			
			XLog eventStream = LogStreamer.logStreamer(xl, null, null, "");
			
			DriftDetector.detectDriftForward(windowSize, eventStream);
			DriftDetector.detectDriftBackward(windowSize, eventStream);
			
			long stopTime = System.nanoTime();
			long duration = stopTime - startTime;
			System.out.println("Total time spent: " + (double)duration * 0.000000001 * 1000 + "ms");
		}
			
		
	}
	
	public static void runexp(int c, int ws, int thr) throws IOException {
	    Path path = Paths.get("Evaluation/" + label + "-w" + ws + "-o" + c + "/");
	    try {
	    	 Files.createDirectory(path);
	    }
	    catch(Exception e) {
	    	 
	    }
	  
		consecutive = c;
		windowSize = ws;
		DriftDetector.numThreads = thr;
		DriftDetector.consecutive = consecutive;
		
		if(label.equals("noise-free")) {
			int i = 0;
			start("" + i, "0.0", "cm");
			start("" + i, "0.0", "cb");
			start("" + i, "0.0", "cd");
			start("" + i, "0.0", "cf");
			start("" + i, "0.0", "cp");
			start("" + i, "0.0", "IOR");
			start("" + i, "0.0", "IRO");
			start("" + i, "0.0", "lp");
			start("" + i, "0.0", "OIR");
			start("" + i, "0.0", "ORI");
			start("" + i, "0.0", "pl");
			start("" + i, "0.0", "pm");
			start("" + i, "0.0", "re");
			start("" + i, "0.0", "RIO");
			start("" + i, "0.0", "ROI");
			start("" + i, "0.0", "rp");
			start("" + i, "0.0", "sw");
		}
		else {
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "cm");
				start("" + i, "0.2", "cm");
				start("" + i, "0.3", "cm");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "cb");
				start("" + i, "0.2", "cb");
				start("" + i, "0.3", "cb");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "cd");
				start("" + i, "0.2", "cd");
				start("" + i, "0.3", "cd");
			
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "cf");
				start("" + i, "0.2", "cf");
				start("" + i, "0.3", "cf");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "cp");
				start("" + i, "0.2", "cp");
				start("" + i, "0.3", "cp");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "IOR");
				start("" + i, "0.2", "IOR");
				start("" + i, "0.3", "IOR");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "IRO");
				start("" + i, "0.2", "IRO");
				start("" + i, "0.3", "IRO");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "lp");
				start("" + i, "0.2", "lp");
				start("" + i, "0.3", "lp");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "OIR");
				start("" + i, "0.2", "OIR");
				start("" + i, "0.3", "OIR");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "ORI");
				start("" + i, "0.2", "ORI");
				start("" + i, "0.3", "ORI");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "pl");
				start("" + i, "0.2", "pl");
				start("" + i, "0.3", "pl");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "pm");
				start("" + i, "0.2", "pm");
				start("" + i, "0.3", "pm");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "re");
				start("" + i, "0.2", "re");
				start("" + i, "0.3", "re");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "RIO");
				start("" + i, "0.2", "RIO");
				start("" + i, "0.3", "RIO");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "ROI");
				start("" + i, "0.2", "ROI");
				start("" + i, "0.3", "ROI");
				
			}
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "rp");
				start("" + i, "0.2", "rp");
				start("" + i, "0.3", "rp");
			}	
			for(int i = 1;i < 11;i ++) {
				start("" + i, "0.1", "sw");
				start("" + i, "0.2", "sw");
				start("" + i, "0.3", "sw");
			
			}
		}
		
		
		
		
	}
	
	public static void start(String round, String noise, String type) {
		int distance = 0;
		
		PrintWriter pw = null;
        try {
         
        	pw = new PrintWriter(new File("Evaluation/" + label + "-w" + windowSize + "-o" + consecutive + "/" + "R" + round + "-" + type + "-w" + windowSize + "-n" + noise + "-o" + consecutive + ".csv"));
        	
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
        StringBuilder builder = new StringBuilder();
		
		List<String> results = new ArrayList<String>();

		File[] files = null;
		if(label.equals("remove")) {
			files = new File("Round" + round + "R/" + noise + "/").listFiles();
		}
		else if(label.equals("add") || label.equals("noise-free")){
			files = new File("Round" + round + "/" + noise + "/").listFiles();
		}
		//File[] files = new File("Round" + round + "R/" + noise + "/").listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		for (File file : files) {
		    if (file.isFile()) {
		    	String fileName = file.getName();
		    	if(fileName.contains(type)) {
		    		if(label.equals("remove")) {
		    			results.add("Round" + round + "R/" + noise + "/" + fileName);
		    		}
		    		else if(label.equals("add") || label.equals("noise-free")){
		    			results.add("Round" + round + "/" + noise + "/" + fileName);
		    		}
		    		
		    	}
		        
		    }
		}
		
		//results.clear();
		//results.add("permit.xes");
		ArrayList<Double> fScores100 = new ArrayList<>();
		ArrayList<Double> fScores50 = new ArrayList<>();
		ArrayList<Double> fScores25 = new ArrayList<>();
		ArrayList<Double> fScores10 = new ArrayList<>();
		for(String fileName:results) {
			DriftDetector.initialise();
			System.out.println(fileName);
			System.out.println(consecutive + ", ws: " + windowSize);
			builder.append(fileName + "\n");
			Path path = null;
			XLog xl = null;
			path = Paths.get(fileName, new String[0]);
			try {
				xl = XLogManager.readLog(new FileInputStream(path.toString()), path.getFileName().toString());
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
			distance = xl.size() / 10;
			DriftDetector.dfgChangePoints = new ArrayList<>();
			long startTime = System.nanoTime();
			
			
			
			XLog eventStream = LogStreamer.logStreamer(xl, null, null, "");
			
			//XLogManager.saveLogInDisk(XLogManager.getSubLog_eventBased(eventStream,0 , 13631), "permit_1.mxml.gz");
			//XLogManager.saveLogInDisk(XLogManager.getSubLog_eventBased(eventStream, 13631 , eventStream.size()), "permit_2.mxml.gz");
			//System.exit(0);
			
			DriftDetector.detectDriftForward(windowSize, eventStream);
			DriftDetector.detectDriftBackward(windowSize, eventStream);
			
			long stopTime = System.nanoTime();
			long duration = stopTime - startTime;
			
			
			eventTraceMapper = new HashMap();
			int event_id = 0;
			for(int i = 0;i < xl.size();i ++) {
				for(int a = 0;a < xl.get(i).size();a ++) {
					eventTraceMapper.put(event_id, i);
					event_id ++;
				}
			}
			
			ArrayList<Integer> points = new ArrayList<>();
			
 			for(int i = 0;i < DriftDetector.dfgChangePoints.size();i ++) {
 				boolean add = true;
				for(int a = 0;a < DriftDetector.dfgChangePoints.size();a ++) {
					if(i == a) {
						continue;
					}
					if(DriftDetector.dfgChangePoints.get(i) - DriftDetector.dfgChangePoints.get(a) < windowSize && DriftDetector.dfgChangePoints.get(i) - DriftDetector.dfgChangePoints.get(a) > 0) {
						add = false;
						
					}
					if(DriftDetector.dfgChangePoints.get(i) - DriftDetector.dfgChangePoints.get(a) == 0 && i > a) {
						add = false;
					}
					
					
					
				}
				
				if(add) {
					points.add(DriftDetector.dfgChangePoints.get(i));
				}
			}
			
			for(int i = 0;i < points.size() - 1;i ++) {
			
			
				builder.append(eventTraceMapper.get(points.get(i)) + ",");
				
			
			}
			if(points.size() != 0) {
				builder.append(eventTraceMapper.get(points.get(points.size() - 1)));
			}
			
			
			builder.append("\n");
			builder.append((double)duration * 0.000000001 * 1000 + "\n");
			builder.append((double)duration * 0.000000001 / eventStream.size() * 1000 + "\n");
			ArrayList<Integer> pointsClone = (ArrayList<Integer>) points.clone();
			double TP = 0;
			double FP = 0;
			double FN = 0;
			double fScore = 0;
			double precision = 0;
			ArrayList<Integer> answers = new ArrayList<>();
			for(int a = 1;a < 10;a ++) {
				answers.add(a * distance);
			}
			
			for(int a = 0;a < answers.size();a ++) {
				for(int b = 0;b < points.size();b ++) {
					//System.out.println(Math.abs(points.get(b) - answers.get(a)));
					if(Math.abs(eventTraceMapper.get(points.get(b)) - answers.get(a)) <= 100) {
						TP = TP + 1;
						answers.remove(a);
						points.remove(b);
						a --;
						break;
					}
				}
			}
			
			FP = points.size();
			FN = 9 -TP;
			fScore= TP / (TP + (FP + FN) / 2);
			precision = TP / (TP + FP);
			builder.append("ET 100:,");
			builder.append("TP:," + TP + ",");
			builder.append("FP:," + FP + ",");
			builder.append("FN:," + FN + ",");
			builder.append("fScore:," + fScore);
			builder.append(",precision:," + precision);
			fScores100.add(fScore);
			builder.append("\n");
			
			points = (ArrayList<Integer>) pointsClone.clone();
			TP = 0;
			FP = 0;
			FN = 0;
			fScore = 0;
			answers = new ArrayList<>();
			for(int a = 1;a < 10;a ++) {
				answers.add(a * distance);
			}
			
			for(int a = 0;a < answers.size();a ++) {
				for(int b = 0;b < points.size();b ++) {
					//System.out.println(Math.abs(points.get(b) - answers.get(a)));
					if(Math.abs(eventTraceMapper.get(points.get(b)) - answers.get(a)) <= 50) {
						TP = TP + 1;
						answers.remove(a);
						points.remove(b);
						a --;
						break;
					}
				}
			}
			
			FP = points.size();
			FN = 9 -TP;
			fScore= TP / (TP + (FP + FN) / 2);
			precision = TP / (TP + FP);
			
			builder.append("ET 50:,");
			builder.append("TP:," + TP + ",");
			builder.append("FP:," + FP + ",");
			builder.append("FN:," + FN + ",");
			builder.append("fScore:," + fScore);
			builder.append(",precision:," + precision);
			fScores50.add(fScore);
			builder.append("\n");
			
			points = (ArrayList<Integer>) pointsClone.clone();
			TP = 0;
			FP = 0;
			FN = 0;
			fScore = 0;
			answers = new ArrayList<>();
			for(int a = 1;a < 10;a ++) {
				answers.add(a * distance);
			}
			
			for(int a = 0;a < answers.size();a ++) {
				for(int b = 0;b < points.size();b ++) {
					//System.out.println(Math.abs(points.get(b) - answers.get(a)));
					if(Math.abs(eventTraceMapper.get(points.get(b)) - answers.get(a)) <= 25) {
						TP = TP + 1;
						answers.remove(a);
						points.remove(b);
						a --;
						break;
					}
				}
			}
			
			FP = points.size();
			FN = 9 -TP;
			fScore= TP / (TP + (FP + FN) / 2);
			precision = TP / (TP + FP);
			
			builder.append("ET 25:,");
			builder.append("TP:," + TP + ",");
			builder.append("FP:," + FP + ",");
			builder.append("FN:," + FN + ",");
			builder.append("fScore:," + fScore);
			builder.append(",precision:," + precision);
			fScores25.add(fScore);
			builder.append("\n");
			
			points = (ArrayList<Integer>) pointsClone.clone();
			TP = 0;
			FP = 0;
			FN = 0;
			fScore = 0;
			answers = new ArrayList<>();
			for(int a = 1;a < 10;a ++) {
				answers.add(a * distance);
			}
			
			for(int a = 0;a < answers.size();a ++) {
				for(int b = 0;b < points.size();b ++) {
					//System.out.println(Math.abs(points.get(b) - answers.get(a)));
					if(Math.abs(eventTraceMapper.get(points.get(b)) - answers.get(a)) <= 10) {
						TP = TP + 1;
						answers.remove(a);
						points.remove(b);
						a --;
						break;
					}
				}
			}
			
			FP = points.size();
			FN = 9 -TP;
			fScore= TP / (TP + (FP + FN) / 2);
			precision = TP / (TP + FP);
			
			builder.append("ET 10:,");
			builder.append("TP:," + TP + ",");
			builder.append("FP:," + FP + ",");
			builder.append("FN:," + FN + ",");
			builder.append("fScore:," + fScore);
			builder.append(",precision:," + precision);
			fScores10.add(fScore);
			builder.append("\n");
			builder.append("\n");
			
			
			
			
		}
		
		
		
		
		pw.write(builder.toString());
        pw.close();
        System.out.println("done!");
		

	}
	
	
}
