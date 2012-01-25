package metrics;

public class ClassInfo {
    String packageName = "";
    String type;
    String filename;
    String name;
    String parent;
    java.util.TreeSet<String> children = new java.util.TreeSet<String>();
    String methodInfo = "";
    int complexity = 0;
    int dit = 0;
    String it = "";
}

