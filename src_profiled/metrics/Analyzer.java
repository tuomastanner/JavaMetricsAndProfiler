package metrics;


import java.io.*;

import tools.*;

public abstract class Analyzer {
    
    Logger logger;
    
    public Analyzer(Logger logger) { metrics.Profiler.write("entry;metrics.Analyzer;public Analyzer(Logger logger)"); 
        this.logger = logger;
     metrics.Profiler.write("exit;metrics.Analyzer;public Analyzer(Logger logger)"); }

    public abstract String analyzeFile(File file, Object additionalParam);
    
    
}
