package metrics;


import java.io.*;

import tools.*;

public abstract class Analyzer {
	
	Logger logger;
	
	public Analyzer(Logger logger) {
		this.logger = logger;
	}

	public abstract String analyzeFile(File file, Object additionalParam);
	
	
}
