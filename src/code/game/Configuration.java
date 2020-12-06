package code.game;

import code.Engine;
import code.utils.Asset;
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
    
    public boolean startInFullscr = true;
    public int fw, fh;
    public int ww = 800, wh = 600;
    public int aa = 4;
    
    public Configuration(int fwd, int fhd) {
        load(fwd, fhd);
    }
    
    public Configuration(Configuration conf) {
        copy(conf);
    }
    
    public void copy(Configuration conf) {
        startInFullscr = conf.startInFullscr;
        fw = conf.fw; fh = conf.fh;
        ww = conf.ww; wh = conf.wh;
        aa = conf.aa;
    }
    
    public void load(int fwd, int fhd) {
        IniFile conf = Asset.loadIni("config.ini", true);
        
        startInFullscr = conf.getInt("screen", "start_in_fullscreen", startInFullscr?1:0) == 1;
        
        fw = conf.getInt("screen", "fullscr_width", fwd);
        fh = conf.getInt("screen", "fullscr_height", fhd);
        
        ww = conf.getInt("screen", "window_width", ww);
        wh = conf.getInt("screen", "window_height", wh);
        
        aa = conf.getInt("screen", "antialiasing", aa);
    }
    
    public void save() {
        IniFile conf = new IniFile(new Hashtable());
        
        conf.put("screen", "start_in_fullscreen", String.valueOf(startInFullscr?1:0));
        
        conf.put("screen", "fullscr_width", String.valueOf(fw));
        conf.put("screen", "fullscr_height", String.valueOf(fh));
        
        conf.put("screen", "window_width", String.valueOf(ww));
        conf.put("screen", "window_height", String.valueOf(wh));

        conf.put("screen", "antialiasing", String.valueOf(aa));
        
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
            Engine.printError(e);
        }
    }
    
    public boolean isNeedToConfirm(Configuration other) {
        return other.fw != fw || other.fh != fh;
    }
    
    public boolean isValid() {
        return Engine.isResolutionValid(fw, fh);
    }
    
    public void apply() {
        Engine.setWindow(this, Engine.isFullscr());
    }

}
