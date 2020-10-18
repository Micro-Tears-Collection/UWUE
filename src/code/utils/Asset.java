package code.utils;

import code.audio.SoundBuffer;
import code.audio.SoundSource;
import code.engine3d.Material;
import code.engine3d.Texture;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class Asset {
    
    static Vector<Integer> vbos = new Vector();
    static Vector<DisposableContent> disposable = new Vector();
    static Hashtable<String, ReusableContent> reusable = new Hashtable();
    
    public static void free() {
        Enumeration<ReusableContent> els = reusable.elements();
        while(els.hasMoreElements()) {
            els.nextElement().using = false;
        }
    }
    
    public static void destroyDisposable(boolean destroyEverything) {
        for(Integer vbo : vbos) {
            GL15.glDeleteBuffers(vbo.intValue());
        }
        
        for(int i=0; i<disposable.size(); i++) {
            DisposableContent content = disposable.elementAt(i);
            
            if(!content.neverUnload || destroyEverything) {
                content.destroy();
                disposable.removeElementAt(i);
                i--;
            }
        }
    }
    
    public static final int NONFREE = 1, DISPOSABLE = 2, LOCKED = 4,
            ALL = NONFREE | DISPOSABLE | LOCKED,
            ALL_EXCEPT_LOCKED = ALL & (~LOCKED);
    
    public static void destroyThings(int mask) {
        boolean destroyNonFree = (mask&NONFREE) == NONFREE;
        boolean destroyLocked = (mask&LOCKED) == LOCKED;
        
        if((mask&DISPOSABLE) == DISPOSABLE) destroyDisposable(destroyLocked);
        
        Enumeration<String> keys = reusable.keys();
        Enumeration<ReusableContent> els = reusable.elements();
        
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            ReusableContent el = els.nextElement();
            
            if(((!el.using || destroyNonFree) && !el.neverUnload) || destroyLocked) {
                el.destroy();
                reusable.remove(key);
            }
        }
        
        System.gc();
    }
    
    public static Material getMaterial(String name) {
        String[] lines = StringTools.cutOnStrings(name, ';');
        IniFile stuff = new IniFile(lines, false);
        
        Texture tex = getTexture(lines[0]);
        Material mat = new Material(tex);
        
        mat.load(stuff);
        
        return mat;
    }
    
    public static Texture getTexture(String name) {
        Texture tex = (Texture)reusable.get("TEX_" + name);
        if(tex != null) {
            tex.using = true;
            return tex;
        }
        
        if(name.equals("null")) {
            tex = new Texture(0);
            tex.neverUnload = true;
            reusable.put("TEX_" + name, tex);
            return tex;
        }
        
        tex = Texture.createTexture(name);
        
        if(tex != null) {
            reusable.put("TEX_" + name, tex);
            return tex;
        }
        
        return getTexture("null");
    }
    
    public static SoundBuffer getSoundBuffer(String file) {
        SoundBuffer sound = (SoundBuffer)reusable.get("SOUNDBUFF_" + file);
        if(sound != null) {
            sound.using = true;
            return sound;
        }
        
        sound = SoundBuffer.createBuffer(file);
        if(sound != null) {
            reusable.put("SOUNDBUFF_" + file, sound);
            return sound;
        }
        
        return null;
    }
    
    public static SoundSource getSoundSource() {
        return getSoundSource(null);
    }
    
    public static SoundSource getSoundSource(String file) {
        SoundSource source = file==null?new SoundSource():new SoundSource(file);
        
        disposable.add(source);
        return source;
    }

    public static IniFile loadIni(String path, boolean sections) {
        File f = new File("data", path);
        
        Vector<String> lines = new Vector();
        try {
            Scanner sn = new Scanner(f, "UTF-8");
            sn.useDelimiter("\n");
            
            while(sn.hasNext()) {
                lines.add(sn.next().trim());
            }
            
            sn.close();
        } catch(Exception e) {
            System.out.println("Can't load "+f.getAbsolutePath());
            e.printStackTrace();
        }
        
        IniFile ini = new IniFile(new Hashtable());
        ini.set(lines.toArray(new String[lines.size()]), sections);
        
        return ini;
    }

}
