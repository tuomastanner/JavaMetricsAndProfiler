package tools;

import java.io.*;

/** Writes messages to the servers context log if they are importan enough.
 *  1 = serious errors
 *  2 = other errors
 *  3 = notifications
 *  4 = all database queries
 *  5 = all server / client communication
 *
 * @author  tuomas
 */
public class Logger {
    
    private int logLevel;
    private PrintStream out;
    private String prefix;
    
    /** Creates a new instance of Logger */
    public Logger(PrintStream out, String prefix, int logLevel) {
        this.out = out;
    	this.logLevel = logLevel;
        this.prefix = prefix;
    }
    public void log(String msg, int level) {
        if (level <= logLevel) {
            out.println(prefix + msg);
        }
    }
    
    public void log(String msg, Throwable err, int level) {
        if (level <= logLevel) {
            out.println(prefix + " [Exception occurred] " + msg);
            out.print(prefix + " [StackTrace]");
            err.printStackTrace(out);
        }
    }
}
