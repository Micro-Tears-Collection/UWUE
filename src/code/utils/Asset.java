package code.utils;

import code.audio.SoundBuffer;
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
    static Hashtable<String, CachedContent> cached = new Hashtable();
    
    public static void freeThings() {
        destroyVBOs();
        
        Enumeration<CachedContent> els = cached.elements();
        while(els.hasMoreElements()) {
            els.nextElement().using = false;
        }
    }
    
    public static void destroyThings() {
        destroyThings(false);
    }
    
    public static void destroyThings(boolean destroyNonFree) {
        Enumeration<String> keys = cached.keys();
        Enumeration<CachedContent> els = cached.elements();
        
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            CachedContent el = els.nextElement();
            
            if((!el.using || destroyNonFree) && !el.neverUnload) {
                el.destroy();
                cached.remove(key);
            }
        }
    }
    
    public static void destroyAll() {
        Enumeration<String> keys = cached.keys();
        Enumeration<CachedContent> els = cached.elements();
        
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            CachedContent el = els.nextElement();
            
            el.destroy();
        }
        
        destroyVBOs();
    }
    
    public static void destroyVBOs() {
        for(Integer vbo : vbos) {
            GL15.glDeleteBuffers(vbo.intValue());
        }
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
        Texture tex = (Texture)cached.get("TEX_" + name);
        if(tex != null) {
            tex.using = true;
            return tex;
        }
        
        if(name.equals("null")) {
            tex = new Texture(0);
            tex.neverUnload = true;
            cached.put("TEX_" + name, tex);
            return tex;
        }
        
        tex = Texture.createTexture(name);
        
        if(tex != null) {
            cached.put("TEX_" + name, tex);
            return tex;
        }
        
        return getTexture("null");
    }
    
    public static SoundBuffer getSoundBuffer(String file) {
        SoundBuffer sound = (SoundBuffer)cached.get("SOUNDBUFF_" + file);
        if(sound != null) {
            sound.using = true;
            return sound;
        }
        
        sound = SoundBuffer.createBuffer(file);
        if(sound != null) {
            cached.put("SOUNDBUFF_" + file, sound);
            return sound;
        }
        
        return null;
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
