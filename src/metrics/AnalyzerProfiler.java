package metrics;
import java.io.*;
import java.util.*;
import tools.*;


public class AnalyzerProfiler extends Analyzer {

	public AnalyzerProfiler(Logger logger) {
		super(logger);
	}
	@Override
	public String analyzeFile(File file, Object additionalParam) {
        String contents = Tools.readFile(file);
        contents = contents.replaceAll("\t", "    ");
        
	    //                  0            1        2          3     4    5
        String[] tokens = {"package ", "return ", "class ",  "{", "}", "interface "};
        TokenFinder tokenFinder = new TokenFinder(contents, tokens);
        
        logger.log("starting analysis of file: " + file.getName(),3);
        
        //data to gather
    	String packageName = "";
    	
    	boolean inClass = false;
    	boolean inMethod = false;
    	String className = null;
    	String methodName = null;
    	
    	LinkedList<int[]> profilePos = new LinkedList<int[]>();
    	LinkedList<String> profileCmd = new LinkedList<String>();
    	
    	int[] oldresult;
    	int[] result = {-1,-1};
    	Stack<String> stack = new Stack<String>();
    	boolean lastReturn = false;
    	boolean isVoid = false;
        while(true) {
        	oldresult = result;
        	result = tokenFinder.getNextToken(result[0] + 1);
        	if (result[0] == -1) {
        		logger.log("END REACHED", 3);
        		break;
        	}
//        	logger.log("token: " + tokens[result[1]], 3);
        	
        	if (result[1] == 0) { //package
        		int endPos = tokenFinder.findToken(";", result[0]);
        		packageName = contents.substring(result[0] + tokens[result[1]].length(), 
        				endPos).trim() + ".";
        		result[0] = endPos;
        	}
        	else if (result[1] == 2 || result[1] == 5) { //class
        		inClass = true;

        		int curly = tokenFinder.findToken("{", result[0]);
        		String tempStr = contents.substring(
        				result[0] + tokens[result[1]].length(), curly).trim();
        		int spacePos = tempStr.indexOf(" ");
        		if (spacePos == -1) {
        			className = tempStr;
        		}
        		else {
        			className = tempStr.substring(0, spacePos);
        		}
        		if (className == null || (packageName + className).equals("metrics.Profiler")) {
        			break; //don't add profiling code to Profiler (will cause an infinite loop)
        		}
        		
        		result[0] = curly; //jump over { so that it won't get pushed to stack
        		stack.push("class");
        		logger.log("push class", 3);
        	}
        	else if (result[1] == 3) { // {
        		if(!inMethod) {
        			inMethod = true;
        			//get method name by getting substring of previous token + current token
        			methodName = contents.substring(
        					tokenFinder.findPrevious('\n', result[0]), result[0]).trim();
        			
        			if (methodName.indexOf(" void ") != -1 || 
        					methodName.indexOf(className) != -1) {
        				isVoid = true;
        			}
        			else {
        				isVoid = false;
        			}
        			//add entrypoint
        			
        			//check super() call in constructor
        			int firstSemi = tokenFinder.findToken(";", result[0] + 1); 
        			String firstStm = contents.substring(result[0] + 1, firstSemi);
        			
        			int[] pos = new int[1];
        			if (firstStm.indexOf("super") == -1) {
        				pos[0] = result[0] + 1;
        			}
        			else {
        				pos[0] = firstSemi + 1;
        			}
        			
        			profilePos.add(pos);
        			profileCmd.add(" metrics.Profiler.write(\"" + 
        					"entry;" + packageName + className + ";" + methodName
        					 + "\"); ");
        			stack.push("method");
            		logger.log("push method", 3);
        		}
        		else {
        			stack.push("other");
            		logger.log("push other", 3);

        		}
        	}
        	else if (result[1] == 4) { // }
        		String pop = stack.pop();
        		logger.log("pop " + pop, 3);
        		if (pop.equals("method")) { //method ends
        			inMethod = false;
        			//add exitpoint
        			if (isVoid && !lastReturn) {
	        			int[] pos = {result[0]};
	        			profilePos.add(pos);
	        			String command = " metrics.Profiler.write(\"" + 
    					"exit;" + packageName + className + ";" + methodName + "\"); ";
		    			if (methodName.indexOf("public static void main(") != -1) {
		    				command +=  "metrics.Profiler.close(); ";
		//    				command = " try{Thread.sleep(3000);}catch(Exception e){} ";
		    			}
	        			profileCmd.add(command);
        			}
        		}
        		else if (pop.equals("class")) { //class ends
            		logger.log("pop: " + pop, 3);
        			inClass = false;
        		}
        	}
        	else if (result[1] == 1) { //return
    			//add exitpoint
    			int[] pos = {result[0]};
    			profilePos.add(pos);
    			String command = " metrics.Profiler.write(\"" + 
    					"exit;" + packageName + className + ";" + methodName + "\"); ";
    			if (methodName.indexOf("public static void main(") != -1) {
    				command +=  "metrics.Profiler.close(); ";
//    				command = " try{Thread.sleep(3000);}catch(Exception e){} ";
    			}
    			profileCmd.add(command);
    			
    			if (stack.peek().equals("method")) { //the next one is the end of the method
    				lastReturn = true;		
    			}
    			
        		int endPos = tokenFinder.findToken(";", result[0]);
        		result[0] = endPos;
        	}
        	
        } //end while
        
        //write file
        String outputFilename = file.getPath();
        if (outputFilename.startsWith("src" + File.separator)) {
        	outputFilename = outputFilename.substring(4);
        }
        outputFilename = "src_profiled" + File.separator + outputFilename;
        File outputFile = new File(outputFilename);
        File directory = new File(outputFilename.substring(0, outputFilename.lastIndexOf(File.separator)));
        if (!directory.exists()) {
        	directory.mkdirs();
        }
        try {
	        BufferedWriter output = Tools.openFileWriter(outputFile, false);
	        
	        Iterator<int[]> posIter = profilePos.iterator();
	        Iterator<String> cmdIter = profileCmd.iterator();
        
	        int prevPos = 0;
	        while(posIter.hasNext()) {
	        	int curPos = posIter.next()[0];
	        	output.write(contents.substring(prevPos, curPos));
	        	output.write(cmdIter.next());
	        	prevPos = curPos;
	        }
	        //last bit
	        output.write(contents.substring(prevPos));
	        output.close();
        }
        catch(IOException e) {
        	logger.log("Error occurred when writing file", e, 1);
        	return null;
        }
        
		return null;
	}

}
