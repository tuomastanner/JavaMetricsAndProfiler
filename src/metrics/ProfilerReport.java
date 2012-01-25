package metrics;
import java.io.*;

import tools.*;
import java.util.*;


public class ProfilerReport {
	
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: java ProfilerReport <profile|combined> <profiler logfile> [object metrics logfile]");
			return;
		}
		
		String mode = args[0];
		
		// set debug logging
		int loglevel = 1;
		if (mode.equals("combined")) {
			if (args.length < 3) {
				System.out.println("Note: object metrics logfile is required for combined analysis");
				System.out.println("Usage: java ProfilerReport <profile|combined> <profiler logfile> [object metrics logfile]");
				return;
			}
			if (args.length > 3 && args[3].equals("debug")) {
				loglevel = 3;
			}
		}
		else if (args.length > 2 && args[2].equals("debug")) {
			loglevel = 3;
		}
		
		Logger logger = new Logger(System.out, "(Debug) ", loglevel);

		File curFile = new File(args[1]);
		if (!curFile.isFile()) {
			System.out.println("ERROR: Could not open file: " + args[0]);
			return;
		}
		
		BufferedReader fileCont = Tools.openTextFile(curFile);
		if (fileCont == null) {
			System.out.println("ERROR opening file: " + args[0]);
			return;
		}
		
		TreeMap<String, ProfilerClassInfo> classes = new TreeMap<String, ProfilerClassInfo>();
		Stack stack = new Stack();
		
		String row = null;
		int line = 0;
		long prevTime = 0;
        while ((row = fileCont.readLine()) != null) {
        	++line;
        	StringTokenizer st = new StringTokenizer(row, ";");
        	String type = null;
        	String className = null;
        	String methodName = null;
        	long timestamp = 0;
        	try {
	    		type = st.nextToken();
	    		className = st.nextToken();
	    		methodName = st.nextToken();
	    		timestamp = Long.parseLong(st.nextToken());
        	}catch (Exception e) {
        		logger.log("ERROR parsing line: " + line + ", stopping log analysis", 1);
        		break;
        	}
    		Object[] stackEntry = null;
    		boolean popped = false;
    		if (type.equals("entry")) {
    			long[] value = {timestamp};
    			Object[] newstackEntry = {className, methodName, value};
    			try {
    				stackEntry = (Object[])stack.peek();
    			}
    			catch(EmptyStackException ne) {//first row of file
    				stackEntry = newstackEntry;
    				prevTime = timestamp;
    			}
    			stack.push(newstackEntry);
    			
    		}
    		else if (type.equals("exit")) {
    			stackEntry = (Object[])stack.pop();
    			if (!className.equals(stackEntry[0]) || !methodName.equals(stackEntry[1])) {
    				logger.log("ERROR parsing file at line " + line + 
    						". Expected " + className + "." + methodName + ", but got " +
    						stackEntry[0] + "." + stackEntry[1], 1);
    				break;
    			}
    			popped = true;
    		}
    		//load class info
			ProfilerClassInfo classInfo = classes.get((String)stackEntry[0]);
			if (classInfo == null) {
				classInfo = new ProfilerClassInfo();
				classes.put((String)stackEntry[0], classInfo);
			}
			long[] method = classInfo.methods.get((String)stackEntry[1]);
			if (method == null) {
				method = new long[3];
				classInfo.methods.put((String)stackEntry[1], method);
			}
			long timeSpent = timestamp - prevTime;
			method[1] += timeSpent;
			classInfo.totalExecTime += timeSpent;
			if (classInfo.minExecTime > timeSpent) {
				classInfo.minExecTime = timeSpent;
			}
			if (classInfo.maxExecTime < timeSpent) {
				classInfo.maxExecTime = timeSpent;
			}
			
			if (popped) {
				++method[0]; //method call counter
			}
			
			prevTime = timestamp;
        } //end while
		
        if (!mode.equals("combined")) {
	        //just print out profiler results
	        Iterator<Map.Entry<String, ProfilerClassInfo>> iter = classes.entrySet().iterator();
	        
	        while (iter.hasNext()) {
				Map.Entry<String, ProfilerClassInfo> entry =iter.next();
				String className = entry.getKey();
				ProfilerClassInfo classInfo = entry.getValue();
	        	
				System.out.println("\n----------------------");
				System.out.print("CLASS: " + className + 
						"\nTOTAL EXECUTION TIME: " + classInfo.totalExecTime);
				Iterator<Map.Entry<String, long[]>> methodIter = classInfo.methods.entrySet().iterator();
				while (methodIter.hasNext()) {
					Map.Entry<String, long[]> method = methodIter.next();
					long[] values = method.getValue();
					System.out.println("\n METHOD: " + method.getKey() + "\n  CALL COUNT: " + 
							values[0] + ", EXECUTION TIME: " + values[1]);
				}
				
	        }
        	return;
        }
        
    	String metrics = Tools.readFile(new File(args[2]));
    	if (metrics == null) {
    		System.out.println("Could not read metrics file: " + args[1]);
    		return;
    	}
    	int curPos = 0;
    	int endPos = 0;
		logger.log("read file: " + args[2], 3);
    	while (true) {
    		curPos = metrics.indexOf("CLASS: ", curPos);
    		if (curPos == -1) {
    			break;
    		}
    		curPos = curPos + 7;
    		endPos = metrics.indexOf("\r", curPos);
    		
    		String className = metrics.substring(curPos, endPos);
    		logger.log("found class:" + className, 3);
    		//load class
			ProfilerClassInfo classInfo = classes.get(className);
			if (classInfo == null) {
				classInfo = new ProfilerClassInfo();
				classes.put(className, classInfo);
			}
			//get metrics
    		curPos = metrics.indexOf("WMC: ", curPos) + 5;
			classInfo.wmc = Tools.parseInt(metrics.substring(curPos, 
					metrics.indexOf("\r", curPos)));
    		curPos = metrics.indexOf("DIT: ", curPos) + 5;
			classInfo.dit = Tools.parseInt(metrics.substring(curPos, 
					metrics.indexOf("\r", curPos)));
    		curPos = metrics.indexOf("NOC: ", curPos) + 5;
			classInfo.noc = Tools.parseInt(metrics.substring(curPos, 
					metrics.indexOf("\r", curPos)));
			//get method complexities
			int endClass = metrics.indexOf("----", curPos);
			if (endClass == -1) {
				endClass = metrics.length();
			}
			int methodPos = metrics.indexOf("METHOD: ", curPos);
			logger.log("endClass: " + endClass + ", methodPos: " + methodPos, 3);
			while (methodPos != -1 && methodPos < endClass) {
				String methodName = metrics.substring(methodPos + 8, 
						metrics.indexOf("\r", methodPos));
				long[] method = classInfo.methods.get(methodName);
				if (method == null) {
					method = new long[3];
					classInfo.methods.put(methodName, method);
				}
				methodPos = metrics.indexOf("COMPLEXITY: ", methodPos) + 12;
				String compStr = metrics.substring(methodPos, metrics.indexOf("\r", methodPos));
				logger.log(methodName + ", complexity: " + compStr, 3);
				method[2] = Tools.parseInt(compStr);
				
				methodPos = metrics.indexOf("METHOD: ", methodPos);
			}
    	} //end object metrics processing
        //print out results
        Iterator<Map.Entry<String, ProfilerClassInfo>> iter = classes.entrySet().iterator();
        
        while (iter.hasNext()) {
			Map.Entry<String, ProfilerClassInfo> entry =iter.next();
			String className = entry.getKey();
			ProfilerClassInfo classInfo = entry.getValue();
        	if (classInfo.minExecTime == Long.MAX_VALUE) {
        		classInfo.minExecTime = 0;  
        	}
        	if (classInfo.maxExecTime == -1) {
        		classInfo.maxExecTime = 0;
        	}
			System.out.println("\n----------------------");
			System.out.print("CLASS: " + className + 
					"\nWMC: " + classInfo.wmc + 
					"\nDIT: " + classInfo.dit + 
					"\nNOC: " + classInfo.noc + 
					"\n\nMINIMUM EXECUTION TIME: " + classInfo.minExecTime +
					"\nMAXIMUM EXECUTION TIME: " + classInfo.maxExecTime +
					"\nTOTAL EXECUTION TIME: " + classInfo.totalExecTime + "\n");
			Iterator<Map.Entry<String, long[]>> methodIter = classInfo.methods.entrySet().iterator();
			while (methodIter.hasNext()) {
				Map.Entry<String, long[]> method = methodIter.next();
				long[] values = method.getValue();
				System.out.println("\n METHOD: " + method.getKey() + 
						"\n  METHOD COMPLEXITY: " + values[2] + 
						"\n  CALL COUNT: " + values[0] + 
						"\n  TOTAL EXECUTION TIME: " + values[1]);
				if (values[0] == 0) {
					System.out.println("  AVERAGE EXECUTION TIME: 0\n");
				}
				else {
					System.out.println("  AVERAGE EXECUTION TIME: " + 
							(values[1]/values[0]) + "\n");
					
				}
			}
			
        }
    	
    }

}
