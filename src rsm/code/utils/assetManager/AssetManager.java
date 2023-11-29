package code.utils.assetManager;

import code.utils.IniFile;
import code.utils.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class AssetManager {
    
    static Hashtable<String, ReusableContent> reusable = new Hashtable();
    
    public static final int CONTENT = 0, NONFREE = 1, LOCKED = 2,
            ALL = CONTENT | NONFREE | LOCKED,
            ALL_EXCEPT_LOCKED = ALL & (~LOCKED);
    
    public static void destroyThings(int mask) {
        //System.out.println("\nDestroying junk!:");
        boolean destroyNonFree = (mask&NONFREE) == NONFREE;
        boolean destroyLocked = (mask&LOCKED) == LOCKED;

        Enumeration<String> keys = reusable.keys();
        Enumeration<ReusableContent> els = reusable.elements();

        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            ReusableContent el = els.nextElement();

            if(el.getUsingCount() < 0) System.out.println("wtf usage is smaller than zero on object " 
                    + el.toString());

            if((el.getUsingCount() == 0 || destroyNonFree) && (!el.neverUnload || destroyLocked)) {
                //System.out.println("destroy "+key);
                el.destroy();
                reusable.remove(key);
            }// else System.out.println("skip "+key);
        }
        
        System.gc();
    }
    
    public static void add(String name, ReusableContent obj) {
        reusable.put(name, obj);
    }
    
    public static ReusableContent get(String name) {
        return reusable.get(name);
    }

    public static ArrayList<ReusableContent> getAll() {
        ArrayList<ReusableContent> content = new ArrayList<>();

        Enumeration<ReusableContent> els = reusable.elements();

        while(els.hasMoreElements()) {
            ReusableContent el = els.nextElement();
            content.add(el);
        }

        return content;
    }

    //todo move to new class(??) or not
    
    public static byte[] load(String path) {
        File f = new File("data", path);
        
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            
            return data;
        } catch(Exception e) {
            System.out.println("Can't load "+f.getAbsolutePath());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String loadString(String path) {
        byte[] data = load(path);
        
        try {
            return data != null ? (new String(data, "UTF-8")) : null;
            
        } catch(UnsupportedEncodingException e) {
            System.out.println("Lol wtf your system doesnt support utf-8 encoding");
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String[] loadLines(String path) {
        ArrayList<String> lines = new ArrayList<>();
        String str = loadString(path);
        
        if(str != null) {
            String[] fileLines = StringTools.cutOnStrings(str, '\n');

            for(int i = 0; i < fileLines.length; i++) {
                String s = fileLines[i].trim();
                if(s.length() > 0) lines.add(s);
            }
        }
        
        return lines.toArray(new String[lines.size()]);
    }

    public static IniFile loadIni(String path, boolean sections) {
        String[] lines = loadLines(path);
        
        IniFile ini = new IniFile(new Hashtable());
        if(lines != null) ini.set(lines, sections);
        
        return ini;
    }
	
	public static String updatePath(String path, String currentDirectory) {
		if(currentDirectory == null) return path;
		else if(path.startsWith(".")) return currentDirectory + path.substring(1);
		else return path;
	}
	
	public static String getDirectory(String file) {
		int i = file.lastIndexOf('/');
		if(i == -1) return null;
		
		return file.substring(0, i);
	}
	
	public static String toGamePath(String file) {
		return (new File("data", file)).getAbsolutePath();
	}

}
