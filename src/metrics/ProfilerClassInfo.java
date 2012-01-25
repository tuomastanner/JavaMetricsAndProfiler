package metrics;
import java.util.*;

public class ProfilerClassInfo {
	
	long totalExecTime = 0;
	long minExecTime = Long.MAX_VALUE;
	long maxExecTime = -1;
	
	int wmc = 0;
	int dit = 0;
	int noc = 0;
	
	TreeMap<String, long[]> methods = new TreeMap<String, long[]>();
	
}
