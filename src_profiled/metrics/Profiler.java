package metrics;
import java.util.*;
import java.io.*;

import tools.*;

public class Profiler {
    
    public static BufferedWriter buffer;
    
    static {  
        try {
            buffer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File("profiler_data.csv"), false)));
        }
        catch (IOException e) {
            System.out.println("Profier: could not oper profiler_data.csv");
        }
    }
    
    public static void write(String row) {
        try {
            buffer.write(row + ";" + System.currentTimeMillis() + "\n");
        }
        catch(IOException e) {
            //don't do anything
        }
    }
    
    public static void close() {
        try {
            buffer.close();
        }
        catch(IOException e){};
    }

}
