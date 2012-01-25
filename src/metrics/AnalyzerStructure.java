package metrics;
import java.io.*; //private class (just testing)
import java.util.Stack;

import tools.*;


public class AnalyzerStructure extends Analyzer {

	public AnalyzerStructure(Logger logger) {
		super(logger);
	}

	public String analyzeFile(File file, Object additionalParam) {
        String contents = Tools.readFile(file);
        contents = contents.replaceAll("\t", "    ");
	    //                  0            1        2          3     4       5     6     
        String[] tokens = {"package ", "import ", "class ",  "{", "}", " if ", " if(", 
        				  //7         8          9           10          11      12     13
        				  " switch ", " for ", " for(", " while ", " while(", " do ", " do{"};
        TokenFinder tokenFinder = new TokenFinder(contents, tokens);
        
        logger.log("starting analysis",3);
        
        //data to gather
    	String packageName = "(default package)";
    	String importNames = "";
    	int importCount = 0;
    	int classCount = 0;
    	int methodCount = 0;
    	int ifCount = 0;
    	int switchCount = 0;
    	int forCount = 0;
    	int whileCount = 0;
    	int doWhileCount = 0;
    	boolean doActive = false;
    	String classInfo = "";
    	
    	boolean inClass = false;
    	boolean inMethod = false;
    	int[] oldresult;
    	int[] result = {-1,-1};
    	Stack<String> stack = new Stack<String>();
        while(true) {
        	oldresult = result;
        	result = tokenFinder.getNextToken(result[0] + 1);
        	if (result[0] == -1) {
        		logger.log("END REACHED", 3);
        		return "\nFile belongs to package: " + packageName + "\n\nImports " + 
        			   importCount + " packages:\n" + importNames + "\nHas " + 
        			   classCount + " classes:\n" + classInfo;
        	}
        	logger.log("token: " + tokens[result[1]], 3);
        	
        	if (result[1] == 0) { //package
        		int endPos = tokenFinder.findToken(";", result[0]);
        		packageName = contents.substring(result[0] + tokens[result[1]].length(), endPos).trim();
        		result[0] = endPos;
        	}
        	else if (result[1] == 1) { //import
        		++importCount;
        		int endPos = tokenFinder.findToken(";", result[0]);
        		importNames += contents.substring(result[0] + tokens[1].length(), endPos).trim() + "\n";
        		result[0] = endPos;
        	}
        	else if (result[1] == 2) { //class
        		inClass = true;
        		++classCount;
        		int endPos = tokenFinder.findToken("{", result[0]);
        		String info = "CLASS: " + contents.substring(
        				result[0] + tokens[result[1]].length(), endPos).trim();
        		logger.log(info , 3);
        		classInfo += "\n" + info+ "\n";
        		result[0] = endPos; //jump over { so that it won't get pushed to stack
        		stack.push("class");
        		logger.log("push class", 3);
        	}
        	else if (result[1] == 3) { // {
        		if(!inMethod) {
        			inMethod = true;
        			++methodCount;
        			//get method name by getting substring of previous token + current token
        			String info = "METHOD: " + contents.substring(
        					tokenFinder.findPrevious('\n', result[0]), result[0]).trim();
            		logger.log(info , 3);
        			classInfo += "\n" + info + "\n";
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
        			classInfo += "  if-count: " + ifCount + ", switch-count: " + 
        					switchCount + "\n  for-count: " + forCount + 
        					", while-count: " + whileCount + ", do-while-count: " + 
        					doWhileCount + "\n";
        			//reset counters
        			ifCount = switchCount = forCount = whileCount = doWhileCount = 0;
        		}
        		else if (pop.equals("class")) { //class ends
            		logger.log("pop: " + pop, 3);
        			inClass = false;
        			classInfo += "-- total number of methods in class: " + methodCount + " --\n";
        		}
        	}
        	else if (inMethod && (result[1] == 5 || result[1] == 6)) { // if
        		++ifCount;
        	}
        	else if (inMethod && result[1] == 7) { // switch
        		++switchCount;
        	}
        	else if (inMethod && (result[1] == 8 || result[1] == 9)) { // for
        		++forCount;
        	}
        	else if (inMethod && (result[1] == 10 || result[1] == 11)) { // while
        		if (doActive) {
            		++doWhileCount;
            		doActive = false;
        		}
        		else {
        			++whileCount;
        		}
        	}
        	else if (inMethod && (result[1] == 12 || result[1] == 13)) { // do
        		if (result[1] == 13) {
        			stack.push("other");
        		}
        		doActive = true; //set flag for while
        	}
        	
        } //end while
	}

}
