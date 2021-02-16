package code.game;

import code.engine.Engine;

import code.utils.assetManager.AssetManager;
import code.utils.IniFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 *
 * @author Roman Lahin
 */
public class Configuration {
    
    public boolean debug = false;
    public boolean startInFullscr = true, vsync = false;
    public int fw, fh;
    public int ww = 800, wh = 600;
    public int aa = 4;
    
    public float fov = 70;
    
    Configuration(int fwd, int fhd) {
        load(fwd, fhd);
    }
    
    Configuration(Configuration conf) {
        copy(conf);
    }
    
    void copy(Configuration conf) {
        startInFullscr = conf.startInFullscr;
        fw = conf.fw; fh = conf.fh;
        ww = conf.ww; wh = conf.wh;
        aa = conf.aa;
        vsync = conf.vsync;
        debug = conf.debug;
    }
    
    void load(int fwd, int fhd) {
        IniFile conf = AssetManager.loadIni("config.ini", true);
        
        startInFullscr = conf.getInt("screen", "start_in_fullscreen", startInFullscr?1:0) == 1;
        vsync = conf.getInt("screen", "vsync", vsync?1:0) == 1;
        
        fw = conf.getInt("screen", "fullscr_width", fwd);
        fh = conf.getInt("screen", "fullscr_height", fhd);
        
        ww = conf.getInt("screen", "window_width", ww);
        wh = conf.getInt("screen", "window_height", wh);
        
        aa = conf.getInt("screen", "antialiasing", aa);
        
        debug = conf.getInt("game", "debug", debug?1:0) == 1;
    }
    
    void save() {
        IniFile conf = new IniFile(new Hashtable());
        
        conf.put("screen", "start_in_fullscreen", String.valueOf(startInFullscr?1:0));
        conf.put("screen", "vsync", String.valueOf(vsync?1:0));
        
        conf.put("screen", "fullscr_width", String.valueOf(fw));
        conf.put("screen", "fullscr_height", String.valueOf(fh));
        
        conf.put("screen", "window_width", String.valueOf(ww));
        conf.put("screen", "window_height", String.valueOf(wh));

        conf.put("screen", "antialiasing", String.valueOf(aa));
        
        conf.put("game", "debug", String.valueOf(debug?1:0));
        
        FileOutputStream fos = null;
        try {
            File file = new File("data", "config.ini");
            if(!file.exists()) file.createNewFile();
            
            PrintStream ps = new PrintStream((fos = new FileOutputStream(file)));
            conf.save(ps);
            ps.close();
            
        } catch (Exception e) {
            if(fos != null) try {
                fos.close();
            } catch(Exception ee) {}
            e.printStackTrace();
        }
    }
    
    boolean isNeedToConfirm(Configuration other) {
        return other.fw != fw || other.fh != fh || other.aa != aa;
    }
    
    boolean isValid() {
        return Engine.isResolutionValid(fw, fh);
    }
    
    void apply() {
        boolean fullscr = Engine.isFullscr();
        Engine.setWindow(fullscr, fullscr?fw:ww, fullscr?fh:wh, vsync);
    }

}
