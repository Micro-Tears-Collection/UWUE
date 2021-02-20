package code.game;

import code.audio.AudioEngine;
import code.audio.SoundSource;
import code.engine.Engine;

import code.utils.assetManager.AssetManager;
import code.utils.IniFile;
import code.utils.assetManager.DisposableContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Configuration {
    
    public static final int SOUND = 0, MUSIC = 1, FOOTSTEP = 2;
    
    public boolean debug = false;
    
    public int musicVolume = 100, soundsVolume = 100, footstepsVolume = 100;
    
    public int mouseLookSpeed = 100, keyboardLookSpeed = 100, 
            gamepadLookSpeed = 100;
    
    public int gamepadLayout = 0;
    
    public boolean startInFullscr = true, vsync = false, 
            psxRender = false, dithering = true;
    public int fw, fh;
    public int ww = 800, wh = 600, 
            vrw = 320, vrh = 240;
    public int aa = 4;
    
    public float fov = 70;
    
    Configuration(int fwd, int fhd) {
        load(fwd, fhd);
    }
    
    Configuration(Configuration conf) {
        copy(conf);
    }
    
    void copy(Configuration conf) {
        //Audio
        musicVolume = conf.musicVolume;
        soundsVolume = conf.soundsVolume;
        footstepsVolume = conf.footstepsVolume;
        
        //Controls
        mouseLookSpeed = conf.mouseLookSpeed;
        keyboardLookSpeed = conf.keyboardLookSpeed;
        gamepadLookSpeed = conf.gamepadLookSpeed;
        
        gamepadLayout = conf.gamepadLayout;
        
        //Screen
        startInFullscr = conf.startInFullscr;
        fw = conf.fw; fh = conf.fh;
        ww = conf.ww; wh = conf.wh;
        aa = conf.aa;
        vsync = conf.vsync;
        
        //PSX render
        psxRender = conf.psxRender;
        vrw = conf.vrw; vrh = conf.vrh;
        dithering = conf.dithering;
        
        //Debug
        debug = conf.debug;
    }
    
    void load(int fwd, int fhd) {
        IniFile conf = AssetManager.loadIni("config.ini", true);
        
        //Audio
        musicVolume = conf.getInt("audio", "music_volume", musicVolume);
        soundsVolume = conf.getInt("audio", "sounds_volume", soundsVolume);
        footstepsVolume = conf.getInt("audio", "footsteps_volume", footstepsVolume);
        
        //Controls
        mouseLookSpeed = conf.getInt("controls", "mouse_look_speed", mouseLookSpeed);
        keyboardLookSpeed = conf.getInt("controls", "keyboard_look_speed", keyboardLookSpeed);
        gamepadLookSpeed = conf.getInt("controls", "gamepad_look_speed", gamepadLookSpeed);
        
        gamepadLayout = conf.getInt("controls", "gamepad_layout", gamepadLayout);
        
        //Screen
        startInFullscr = conf.getInt("screen", "start_in_fullscreen", startInFullscr?1:0) == 1;
        vsync = conf.getInt("screen", "vsync", vsync?1:0) == 1;
        
        fw = conf.getInt("screen", "fullscr_width", fwd);
        fh = conf.getInt("screen", "fullscr_height", fhd);
        
        ww = conf.getInt("screen", "window_width", ww);
        wh = conf.getInt("screen", "window_height", wh);
        
        aa = conf.getInt("screen", "antialiasing", aa);
        
        //PSX Render
        psxRender = conf.getInt("screen", "psx_render", psxRender?1:0) == 1;
        dithering = conf.getInt("screen", "dithering", dithering?1:0) == 1;
        
        vrw = conf.getInt("screen", "vres_width", vrw);
        vrh = conf.getInt("screen", "vres_height", vrh);
        
        debug = conf.getInt("game", "debug", debug?1:0) == 1;
    }
    
    void save() {
        IniFile conf = new IniFile(new Hashtable());
        
        //Audio
        conf.put("audio", "music_volume", String.valueOf(musicVolume));
        conf.put("audio", "sounds_volume", String.valueOf(soundsVolume));
        conf.put("audio", "footsteps_volume", String.valueOf(footstepsVolume));
        
        //Controls
        conf.put("controls", "mouse_look_speed", String.valueOf(mouseLookSpeed));
        conf.put("controls", "keyboard_look_speed", String.valueOf(keyboardLookSpeed));
        conf.put("controls", "gamepad_look_speed", String.valueOf(gamepadLookSpeed));
        
        conf.put("controls", "gamepad_layout", String.valueOf(gamepadLayout));
        
        //Screen
        conf.put("screen", "start_in_fullscreen", String.valueOf(startInFullscr?1:0));
        conf.put("screen", "vsync", String.valueOf(vsync?1:0));
        
        conf.put("screen", "fullscr_width", String.valueOf(fw));
        conf.put("screen", "fullscr_height", String.valueOf(fh));
        
        conf.put("screen", "window_width", String.valueOf(ww));
        conf.put("screen", "window_height", String.valueOf(wh));

        conf.put("screen", "antialiasing", String.valueOf(aa));
        
        //PSX render
        conf.put("screen", "psx_render", String.valueOf(psxRender?1:0));
        
        if(psxRender) {
            conf.put("screen", "dithering", String.valueOf(dithering?1:0));
            
            conf.put("screen", "vres_width", String.valueOf(vrw));
            conf.put("screen", "vres_height", String.valueOf(vrh));
        }
        
        //Debug
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
        if(fullscr) {
            Engine.setWindow(fullscr, fullscr?fw:ww, fullscr?fh:wh, vsync);
        }
        applyAudio();
    }
    
    void applyAudio() {
        AudioEngine.soundTypesVolume[SOUND] = soundsVolume;
        AudioEngine.soundTypesVolume[MUSIC] = musicVolume;
        AudioEngine.soundTypesVolume[FOOTSTEP] = footstepsVolume;
        
        Vector<DisposableContent> list = AssetManager.getAll(AssetManager.DISPOSABLE);
        for(DisposableContent content : list) {
            
            if(content instanceof SoundSource) {
                SoundSource source = (SoundSource) content;
                
                source.setVolume(source.getVolume());
            }
        }
            
    }

}
