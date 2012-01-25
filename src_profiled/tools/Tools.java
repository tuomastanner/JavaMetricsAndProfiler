package tools;
import java.io.*;

public class Tools {
    
    
    public static BufferedReader openTextFile(File file) { metrics.Profiler.write("entry;tools.Tools;public static BufferedReader openTextFile(File file)"); 
        try {
            if (!file.isFile()) {
                 metrics.Profiler.write("exit;tools.Tools;public static BufferedReader openTextFile(File file)"); return null;
            }
             metrics.Profiler.write("exit;tools.Tools;public static BufferedReader openTextFile(File file)"); return new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
        } catch (IOException e) {
             metrics.Profiler.write("exit;tools.Tools;public static BufferedReader openTextFile(File file)"); return null;
        }

    }
    
    public static BufferedWriter openFileWriter(File file, boolean append) throws IOException { metrics.Profiler.write("entry;tools.Tools;public static BufferedWriter openFileWriter(File file, boolean append) throws IOException"); 
         metrics.Profiler.write("exit;tools.Tools;public static BufferedWriter openFileWriter(File file, boolean append) throws IOException"); return new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, append)));
    }
    
    public static String readFile(File file) { metrics.Profiler.write("entry;tools.Tools;public static String readFile(File file)"); 
        try {
            if (!file.isFile()) {
                 metrics.Profiler.write("exit;tools.Tools;public static String readFile(File file)"); return null;
            }
            BufferedReader fread = new BufferedReader(new FileReader(file));
//            BufferedReader fread = new BufferedReader(new InputStreamReader(
//                    new FileInputStream(file), "Cp1252"));
            
            char[] cbuf = new char[(int)file.length()];
            fread.read(cbuf, 0, (int)file.length());
            
            String fileCont = new String(cbuf);
            fread.close();
            file = null;
             metrics.Profiler.write("exit;tools.Tools;public static String readFile(File file)"); return fileCont;
        } catch (Exception e) {
             metrics.Profiler.write("exit;tools.Tools;public static String readFile(File file)"); return null;
        }
    }
    
    public static boolean saveFile(String fileName, String contents, boolean append) { metrics.Profiler.write("entry;tools.Tools;public static boolean saveFile(String fileName, String contents, boolean append)"); 
        try {
            BufferedWriter file = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, append), "Cp1252"));
            file.write(contents);
            file.close();
        } catch (Exception e) {
             metrics.Profiler.write("exit;tools.Tools;public static boolean saveFile(String fileName, String contents, boolean append)"); return false;
        }
         metrics.Profiler.write("exit;tools.Tools;public static boolean saveFile(String fileName, String contents, boolean append)"); return true;
    }
    
}
