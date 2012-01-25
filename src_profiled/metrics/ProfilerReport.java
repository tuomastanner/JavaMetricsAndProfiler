package metrics;
import java.io.*;

import tools.*;
import java.util.*;


public class ProfilerReport {
    
    public static void main(String[] args) throws IOException { metrics.Profiler.write("entry;metrics.ProfilerReport;public static void main(String[] args) throws IOException"); 
        if (args.length < 1) {
            System.out.println("Usage: java ProfilerReport <filename>");
            return;
        }
        
        // set debug logging
        int loglevel = 1;
        if (args.length > 1 && args[1].equals("debug")) {
            loglevel = 3;
        }
        Logger logger = new Logger(System.out, "(Debug) ", loglevel);

        File curFile = new File(args[0]);
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
            
            String type = st.nextToken();
            String className = st.nextToken();
            String methodName = st.nextToken();
            long timestamp = Long.parseLong(st.nextToken());
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
                    logger.log("Error parsing file at line " + line + 
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
                method = new long[2];
                classInfo.methods.put((String)stackEntry[1], method);
            }
            long timeSpent = timestamp - prevTime;
            classInfo.totalExecTime += timeSpent;
            method[1] += timeSpent;
            
            if (popped) {
                ++method[0]; //method call counter
            }
            
            prevTime = timestamp;
        } //end while
        
        //print out results
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
                System.out.println("\n METHOD: " + method.getKey() + ", CALL COUNT: " + 
                        values[0] + ", EXECUTION TIME: " + values[1]);
            }
            
        }
        
     metrics.Profiler.write("exit;metrics.ProfilerReport;public static void main(String[] args) throws IOException"); metrics.Profiler.close(); }

}
