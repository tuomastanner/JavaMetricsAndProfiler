package tools;
import java.io.*;

public class Tools {
	
	
	public static BufferedReader openTextFile(File file) {
        try {
            if (!file.isFile()) {
                return null;
            }
            return new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
        } catch (IOException e) {
            return null;
        }

	}
	
    public static BufferedWriter openFileWriter(File file, boolean append) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, append)));
    }
    
    public static String readFile(File file) {
        try {
            if (!file.isFile()) {
                return null;
            }
            BufferedReader fread = new BufferedReader(new FileReader(file));
//            BufferedReader fread = new BufferedReader(new InputStreamReader(
//                    new FileInputStream(file), "Cp1252"));
            
            char[] cbuf = new char[(int)file.length()];
            fread.read(cbuf, 0, (int)file.length());
            
            String fileCont = new String(cbuf);
            fread.close();
            file = null;
            return fileCont;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean saveFile(String fileName, String contents, boolean append) {
        try {
            BufferedWriter file = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, append), "Cp1252"));
            file.write(contents);
            file.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
	
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }
    
    public static int parseInt(String str, int defValue) {
        int ret = defValue;
        if (str == null) {
            return ret;
        }
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
        }
        return ret;
    }
}
