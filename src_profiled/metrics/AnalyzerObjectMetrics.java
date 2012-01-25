package metrics;
import java.io.File;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import tools.Logger;
import tools.Tools;


public class AnalyzerObjectMetrics extends Analyzer {
    public AnalyzerObjectMetrics(Logger logger) {
        super(logger); metrics.Profiler.write("entry;metrics.AnalyzerObjectMetrics;public AnalyzerObjectMetrics(Logger logger)"); 
     metrics.Profiler.write("exit;metrics.AnalyzerObjectMetrics;public AnalyzerObjectMetrics(Logger logger)"); }

    public String analyzeFile(File file, Object additionalParam) { metrics.Profiler.write("entry;metrics.AnalyzerObjectMetrics;public String analyzeFile(File file, Object additionalParam)"); 
        
        TreeMap classes = (TreeMap)additionalParam;
        
        //read file
        String contents = Tools.readFile(file);
        contents = contents.replaceAll("\t", "    ");
        
        //                  0            1            2          3     4       5     6     
        String[] tokens = {"package ", "interface ", "class ",  "{", "}", " if ", " if(", 
                          //7         8          9           10          11      12     13 
                          " switch ", " for ", " for(", " while ", " while(", " do ", " do{",};
        TokenFinder tokenFinder = new TokenFinder(contents, tokens);
        
        logger.log("starting analysis",3);
        
        //data to gather
        String packageName = "";
        ClassInfo curClass = null;
        int methodComplexity = 0;
        
        boolean doActive = false;
        
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
                 metrics.Profiler.write("exit;metrics.AnalyzerObjectMetrics;public String analyzeFile(File file, Object additionalParam)"); return null;
            }
            logger.log("token: " + tokens[result[1]], 3);
            
            if (result[1] == 0) { //package
                int endPos = tokenFinder.findToken(";", result[0]);
                packageName = contents.substring(result[0] + tokens[result[1]].length(), endPos).trim();
                packageName += ".";
                result[0] = endPos;
            }
            else if (result[1] == 1 || result[1] == 2) { //class
                inClass = true;
                int curly = tokenFinder.findToken("{", result[0]);
                String tempStr = contents.substring(
                        result[0] + tokens[result[1]].length(), curly).trim();
                int spacePos = tempStr.indexOf(" ");
                String className;
                if (spacePos == -1) {
                    className = tempStr;
                }
                else {
                    className = tempStr.substring(0, spacePos);
                }
                
                int extendsPos = tempStr.indexOf("extends ");
                int implementsPos = tempStr.indexOf("implements ");
                String parent = null;
                if (extendsPos != -1) { // has parent
                    int endPos = implementsPos != -1 ? implementsPos : tempStr.length();
                    parent = tempStr.substring(
                            extendsPos + "extends ".length(),endPos).trim();

                    //add current class to parents children list
                    ClassInfo parentClass = (ClassInfo)classes.get(parent);
                    if (parentClass == null) {
                        parentClass = new ClassInfo();
                        parentClass.name = parent;
                        parentClass.type = "class";
                        classes.put(parent, parentClass);
                    }
                    parentClass.children.add(className);
                }
                if (implementsPos != -1 && implementsPos < curly) { //implements interface
                    String interfaceStr = tempStr.substring(implementsPos + "implements ".length()).trim();
                    
                    //add current class to interface's children list
                    ClassInfo parentClass = (ClassInfo)classes.get(interfaceStr);
                    if (parentClass == null) {
                        parentClass = new ClassInfo();
                        parentClass.name = parent;
                        parentClass.type = "interface";
                        classes.put(interfaceStr, parentClass);
                    }
                    parentClass.children.add(className);
                }
                
                curClass = (ClassInfo)classes.get(className);
                if (curClass == null) { //not in treemap yet, put it there
                    curClass = new ClassInfo();
                    if (result[1] == 1) {
                        curClass.type = "interface";
                    }
                    else {
                        curClass.type = "class";
                    }
                    classes.put(className, curClass);
                }
                curClass.filename = file.getPath();
                curClass.name = className;
                curClass.packageName = packageName;
                curClass.parent = parent;
                
                result[0] = curly; //jump over { so that it won't get pushed to stack
                stack.push("class");
                logger.log("found class: " + className, 3);
            }
            else if (result[1] == 3) { // {
                if(!inMethod) {
                    inMethod = true;
                    //get method name by getting substring of previous line end + current token
                    curClass.methodInfo += "METHOD: " + contents.substring(
                            tokenFinder.findPrevious('\n', result[0]), result[0]).trim() + "\n";

                    stack.push("method");
                    logger.log("push method", 3);
                }
                else {
                    stack.push("other");
                }
            }
            else if (result[1] == 4) { // }
                String pop = stack.pop();
                if (pop.equals("method")) { //method ends
                    logger.log("pop " + pop, 3);
                    inMethod = false;
                    curClass.methodInfo += "  COMPLEXITY: " + methodComplexity + "\n";
                    curClass.complexity += methodComplexity;
                    //reset counters
                    methodComplexity = 0;
                }
                else if (pop.equals("class")) { //class ends
                    logger.log("pop: " + pop, 3);
                    inClass = false;
                    curClass = null;
                }
            }
            else if (inMethod && (result[1] == 5 || result[1] == 6)) { // if
                ++methodComplexity;
            }
            else if (inMethod && result[1] == 7) { // switch
                ++methodComplexity;
            }
            else if (inMethod && (result[1] == 8 || result[1] == 9)) { // for
                ++methodComplexity;
            }
            else if (inMethod && (result[1] == 10 || result[1] == 11)) { // while
                if (doActive) { //do while ends
                    ++methodComplexity;
                    doActive = false;
                }
                else { //plain while
                    ++methodComplexity;
                }
            }
            else if (inMethod && (result[1] == 12 || result[1] == 13)) { // do
                if (result[1] == 13) { //do with curly
                    stack.push("other");
                }
                doActive = true; //set flag for while
            }
            
        } //end while
    }
}
