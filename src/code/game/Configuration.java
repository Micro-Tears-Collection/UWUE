package code.game;

import code.audio.AudioEngine;
import code.audio.SoundSource;
import code.engine.Engine;
import code.engine.Window;
import code.engine3d.E3D;
import code.engine3d.game.WorldMaterial;

import code.utils.assetManager.AssetManager;
import code.utils.IniFile;
import code.utils.assetManager.ReusableContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class Configuration {
    
    public static final int SOUND = 0, MUSIC = 1, FOOTSTEP = 2;
    
    public boolean debug = false;
    
    public int musicVolume = 80, soundsVolume = 70, footstepsVolume = 90;
	public boolean hrtf = true;
    
    public int mouseLookSpeed = 100, keyboardLookSpeed = 100, 
            gamepadLookSpeed = 100;
    
    public int gamepadLayout = 0;
    
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
        //Audio
        musicVolume = conf.musicVolume;
        soundsVolume = conf.soundsVolume;
        footstepsVolume = conf.footstepsVolume;
		
		hrtf = conf.hrtf;
        
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
        
        //Debug
        debug = conf.debug;
    }
    
    void load(int fwd, int fhd) {
        IniFile conf = AssetManager.loadIni("config.ini", true);
        
        //Audio
        musicVolume = conf.getInt("audio", "music_volume", musicVolume);
        soundsVolume = conf.getInt("audio", "sounds_volume", soundsVolume);
        footstepsVolume = conf.getInt("audio", "footsteps_volume", footstepsVolume);
		
        hrtf = conf.getInt("audio", "hrtf", hrtf?1:0) == 1;
        
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
        
        debug = conf.getInt("game", "debug", debug?1:0) == 1;
    }
    
    void save() {
        IniFile conf = new IniFile(new Hashtable());
        
        //Audio
        conf.put("audio", "music_volume", String.valueOf(musicVolume));
        conf.put("audio", "sounds_volume", String.valueOf(soundsVolume));
        conf.put("audio", "footsteps_volume", String.valueOf(footstepsVolume));
		
        conf.put("audio", "hrtf", String.valueOf(hrtf?1:0));
        
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
    
    void apply(Window window, E3D e3d, boolean changeWindow) {
        if(changeWindow) {
            boolean fullscr = window.isFullscr();
            if(fullscr) {
                window.setWindow(fullscr, fullscr ? fw : ww, fullscr ? fh : wh, vsync);
            }
        }
        applyAudio();
		applyHRTF();
		applyPsxrender(e3d);
    }
    
    void applyAudio() {
        AudioEngine.soundTypesVolume[SOUND] = soundsVolume;
        AudioEngine.soundTypesVolume[MUSIC] = musicVolume;
        AudioEngine.soundTypesVolume[FOOTSTEP] = footstepsVolume;
        
        ArrayList<SoundSource> list = AudioEngine.sources;
        for(SoundSource source : list) {
            source.setVolume(source.getVolume());
        }
        
    }
    
    void applyHRTF() {
        AudioEngine.enableHRTF(hrtf);
    }
    
    void applyPsxrender(E3D e3d) {
        ArrayList<ReusableContent> list = AssetManager.getAll();
		
        for(ReusableContent content : list) {
            if(content instanceof WorldMaterial) {
				WorldMaterial mat = (WorldMaterial) content;
				mat.updateSamplerProperties(e3d);
			}
        }
            
    }

}
