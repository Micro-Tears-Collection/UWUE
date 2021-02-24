package infofinder;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 *
 * @author Roman Lahin
 */
public class InfoFinder {

    static String toFind;
    
    public static void main(String[] args) {
        
        String dir = null;
        
        for(int i=0; i<args.length; i++) {
            String arg = args[i];
            
            if(arg.startsWith("-")) {
                if(arg.equals("-dir")) {
                    dir = args[i+1];
                } else if(arg.equals("-find")) {
                    toFind = args[i+1];
                }
                
                i++;
            }
        }
        
        if(dir == null || toFind == null) {
            System.out.println("InfoFinder for UWUE by Roman Lahin\n"
                    + "Reads obj, txt, ini, lua files\n"
                    + "Use -dir to set UWUE data folder\n"
                    + "Use -find to set what you want to find");
            
            (new Scanner(System.in)).nextLine();
            return;
        }
        
        if(toFind != null && dir != null) {
            File folder = new File(dir);
            readFile(folder);
        }
    }

    private static void readFile(File file) {
        if(file.isFile()) {
            String path = file.getPath();
            String pathlc = file.getPath().toLowerCase();
            
            if(pathlc.endsWith(".obj") || pathlc.endsWith(".ini") || 
                    pathlc.endsWith(".txt") || pathlc.endsWith(".lua")) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();

                    String fileReaded = new String(data, "UTF-8");
                    
                    if(fileReaded.contains(toFind)) {
                        System.out.println(path);
                    }
                } catch (Exception e) {
                    System.out.println("Can't load " + file.getPath());
                    e.printStackTrace();
                }
            }
        } else {
            File[] files = file.listFiles();
            
            for(int i=0; i<files.length; i++) {
                readFile(files[i]);
            }
        }
    }
    
}
