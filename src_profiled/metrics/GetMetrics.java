package metrics;
import java.io.BufferedReader;

import tools.*;
import java.io.*;
import java.util.*;

public class GetMetrics {

    /** A tool that counts metrics from source code.
     * The following reports are available:
     * lines: Logical LOC count
     * structure: information on class structure
     * object-metrics: WMC DIT and NOC metrics + method complexities
     * 
     * @param args first arg is the report type. Next one is file or directory
     * to be processed.
     */
    public static void main(String[] args) { metrics.Profiler.write("entry;metrics.GetMetrics;public static void main(String[] args)"); 

        if (args.length < 2) {
            System.out.println("Usage: java GetMetrics [lines, structure, object-metrics] <name of file to be counted> [debug]");
            return;
        }

        // set debug logging
        int loglevel = 1;
        if (args.length > 2 && args[2].equals("debug")) {
            loglevel = 3;
        }
        Logger logger = new Logger(System.out, "(Debug) ", loglevel);

        String method = args[0];

        File curFile = new File(args[1]);
        if (curFile == null) {
            System.out.println("ERROR: Could not open file: " + args[1]);
            return;
        }

        Object additionalParam = null;
        Analyzer analyzer = null;
        String results = "";
        // initialize analyzer
        if (method.equals("lines")) {
            analyzer = new AnalyzerLines(logger);
        } else if (method.equals("structure")) {
            analyzer = new AnalyzerStructure(logger);
        } else if (method.equals("object-metrics")) {
            analyzer = new AnalyzerObjectMetrics(logger);
            additionalParam = new TreeMap();
        } else if (method.equals("profiler")) {
            analyzer = new AnalyzerProfiler(logger);
        } else {
            System.out.println("method not supported");
            return;
        }

        // process files
        if (curFile.isFile()) {
                results = analyzer.analyzeFile(curFile, additionalParam);
        }
        else {
            results = processFiles(curFile, analyzer, additionalParam);
        }

        // post process
        if (method.equals("lines")) {
            System.out.println("filename\tphysical\tlogical");
            System.out.println(results);
        }
        else if (method.equals("structure")) {
            System.out.println(results);
        }
        else if (method.equals("object-metrics")) {
            // go through the classes and count the inheritance
            TreeMap classes = (TreeMap) additionalParam;
            Iterator iter = classes.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ClassInfo curClass = (ClassInfo) entry.getValue();
                String parent = curClass.parent;
                curClass.it = curClass.name;
                while (parent != null) {
                    curClass.it = parent + " > " + curClass.it;
                    ++curClass.dit;
                    ClassInfo tempClass = (ClassInfo) classes.get(parent);
                    parent = tempClass.parent;
                }

                System.out.println("----------------------");
                System.out.print("CLASS: " + curClass.packageName + curClass.name + "\nTYPE: " + 
                        curClass.type +"\nFROM FILE: "
                        + curClass.filename + "\n\nWMC: " + curClass.complexity
                        + "\nDIT: " + curClass.dit);
                if (curClass.dit > 0) {
                    System.out.print(", INHERITANCE: " + curClass.it);
                }
                System.out.print("\nNOC: " + curClass.children.size());
                if (curClass.children.size() > 0) {
                    System.out.print("\nCHILDREN: ");
                    Iterator<String> childIter = curClass.children.iterator();
                    while (childIter.hasNext()) {
                        System.out.print(childIter.next() + " ");
                    }
                }
                System.out.println("\n\n" + curClass.methodInfo + "\n");
            }

        }
     metrics.Profiler.write("exit;metrics.GetMetrics;public static void main(String[] args)"); metrics.Profiler.close(); }
    /** Goes through a folder recursively and analyzes all files that end with .java
     * 
     * @param startDir
     * @param analyzer
     * @param additionalParam
     * @return report of the analysis
     */
    public static String processFiles(File startDir, Analyzer analyzer, Object additionalParam) { metrics.Profiler.write("entry;metrics.GetMetrics;public static String processFiles(File startDir, Analyzer analyzer, Object additionalParam)"); 
        String results = "";
        File[] files = startDir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (!files[i].isFile()) {
                results += processFiles(files[i], analyzer, additionalParam);
            }
            if (!files[i].getPath().endsWith(".java")) { // skip non-java files
                continue;
            }
            results += analyzer.analyzeFile(files[i], additionalParam);
        }
         metrics.Profiler.write("exit;metrics.GetMetrics;public static String processFiles(File startDir, Analyzer analyzer, Object additionalParam)"); return results;
    }
}
