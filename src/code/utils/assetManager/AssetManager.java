package code.utils.assetManager;

import code.utils.IniFile;
import code.utils.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//todo mesh caching
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class AssetManager {
    
    public static Vector<Integer> vbos = new Vector();
    static Vector<DisposableContent> disposable = new Vector();
    static Hashtable<String, ReusableContent> reusable = new Hashtable();
    
    public static void free() {
        Enumeration<ReusableContent> els = reusable.elements();
        while(els.hasMoreElements()) {
            els.nextElement().using = false;
        }
    }
    
    private static void destroyDisposable(boolean destroyLocked) {
        for(Integer vbo : vbos) {
            GL15.glDeleteBuffers(vbo.intValue());
        }
        
        for(int i=0; i<disposable.size(); i++) {
            DisposableContent content = disposable.elementAt(i);
            
            if(!content.neverUnload || destroyLocked) {
                content.destroy();
                disposable.removeElementAt(i);
                i--;
            }
        }
    }
    
    public static final int REUSABLE = 1, DISPOSABLE = 2, NONFREE = 4, LOCKED = 8,
            ALL = REUSABLE | DISPOSABLE | NONFREE | LOCKED,
            ALL_EXCEPT_LOCKED = ALL & (~LOCKED);
    
    public static void destroyThings(int mask) {
        boolean destroyNonFree = (mask&NONFREE) == NONFREE;
        boolean destroyLocked = (mask&LOCKED) == LOCKED;
        
        if((mask&DISPOSABLE) == DISPOSABLE) destroyDisposable(destroyLocked);
        
        if((mask&REUSABLE) == REUSABLE) {
            Enumeration<String> keys = reusable.keys();
            Enumeration<ReusableContent> els = reusable.elements();

            while(keys.hasMoreElements()) {
                String key = keys.nextElement();
                ReusableContent el = els.nextElement();

                if((!el.using || destroyNonFree) && (!el.neverUnload || destroyLocked)) {
                    el.destroy();
                    reusable.remove(key);
                }
            }
        }
        
        System.gc();
    }
    
    public static void addDisposable(DisposableContent obj) {
        disposable.add(obj);
    }
    
    public static void addReusable(String name, ReusableContent obj) {
        reusable.put(name, obj);
    }
    
    public static ReusableContent get(String name) {
        return reusable.get(name);
    }
    
    public static Vector<DisposableContent> getAll(int mask) {
        Vector<DisposableContent> content = new Vector();
        
        if((mask&DISPOSABLE) == DISPOSABLE) content.addAll(disposable);
        
        if((mask&REUSABLE) == REUSABLE) {
            Enumeration<ReusableContent> els = reusable.elements();

            while(els.hasMoreElements()) {
                ReusableContent el = els.nextElement();
                content.add(el);
            }
        }
        
        return content;
    }
    
    //todo move to new class(??) or not
    
    public static String loadString(String path) {
        File f = new File("data", path);
        
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fis.read(data);
            fis.close();
            
            return new String(data, "UTF-8");
        } catch(Exception e) {
            System.out.println("Can't load "+f.getAbsolutePath());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String[] loadLines(String path) {
        Vector<String> lines = new Vector();
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

}
