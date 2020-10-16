package code.game;

import code.Engine;
import code.Screen;
import code.audio.SoundSource;
import code.engine3d.E3D;
import code.utils.Asset;
import code.utils.FPS;
import code.utils.IniFile;
import code.utils.Keys;
import code.utils.StringTools;
import code.utils.font.BMFont;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author Roman Lahin
 */
public class Main {
    
    //bilding
    public static int TILDE;

    public IniFile conf;
    
    public E3D e3d;
    public SoundSource musPlayer;
    public Globals lua;
    public LuaTable luagame;
    public LuaTable luasave;
    
    public BMFont font;
    public int fontColor, fontSelColor;

    boolean run = true;
    public Screen screen, nextScreen;
    boolean needToDestroyScreen;
    SoundSource selectedS, clickedS, gameStartS;

    public void init() {
        conf = Asset.loadIni("game.ini", true);
        Engine.setTitle(conf.get("GAME", "NAME"));
        
        musPlayer = SoundSource.createMusicPlayer();
        selectedS = new SoundSource("/sounds/select.ogg");
        selectedS.buffer.neverUnload = true;
        clickedS = new SoundSource("/sounds/click.ogg");
        clickedS.buffer.neverUnload = true;
        gameStartS = new SoundSource("/sounds/game start.ogg");
        gameStartS.buffer.neverUnload = true;
        
        font = BMFont.loadFont(conf.get("HUD", "FONT"));
        font.setInterpolation(false);
        setFontScale(Engine.h);
        
        fontColor = StringTools.getRGB(conf.getDef("HUD", "FONT_COLOR", "255,255,255"), ',');
        fontSelColor = StringTools.getRGB(conf.getDef("HUD", "FONT_SELECTED_COLOR", "221,136,149"), ',');
        
        e3d = new E3D();
        
        lua = JsePlatform.standardGlobals();
        lua.set("game", (luagame = new LuaTable()));
        lua.set("save", (luasave = new LuaTable()));

        setScreen(new Menu(this));

        run();
    }

    public void setScreen(Screen screen) {
        setScreen(screen, false);
    }

    public void setScreen(Screen screen, boolean needToDestroy) {
        nextScreen = screen;
        needToDestroyScreen = needToDestroy;
    }

    private void destroy() {
        if(screen != null) screen.destroy();
        
        e3d.destroy();
        
        font.destroy();
        
        musPlayer.destroy();
        selectedS.destroy();
        clickedS.destroy();
        gameStartS.destroy();
        
        Asset.destroyAll();
        
        Engine.destroy();
    }

    public void stop() {
        run = false;
        nextScreen = null;
    }

    private void run() {
        while(run) {
            //Change screen to next screen
            if(nextScreen != null) {
                if(screen != null && needToDestroyScreen) {
                    needToDestroyScreen = false;
                    screen.destroy();
                }

                screen = nextScreen;
                screen.show();
            }
            
            FPS.frameBegin();

            if(screen != null && screen.isRunning()) {
                screen.tick();
            }

            try {
                Thread.sleep(Math.max(1, 8 - (System.currentTimeMillis() - FPS.previousFrame)));
                //max 125 fps
            } catch (Exception e) {}
            FPS.frameEnd();
        }

        destroy();
    }

    public void keyReleased(int key) {
        Keys.keyReleased(key);
        if(screen != null) screen.keyReleased(key);
        
        if(Keys.isThatBinding(key, TILDE)) {
            Scanner sn = new Scanner(System.in);
            
            while(true) {
                String line = sn.nextLine();
                if(line.equals("close lua")) break;
                else if(line.equals("print game")) {
                    printLua(luagame, System.out);
                    break;
                }
                
                runScript(line);
            }
            
            sn.close();
        }
    }

    public void keyPressed(int key) {
        Keys.keyPressed(key);
        if(screen != null) screen.keyPressed(key);
    }

    public void mouseAction(int button, boolean pressed) {
        if(screen != null) screen.mouseAction(button, pressed);
    }

    public void sizeChanged(int w, int h, Screen scr) {
        setFontScale(h);
        
        if(screen != null) screen.sizeChanged(w, h, scr);
    }
    
    void setFontScale(int h) {
        font.baseScale = Math.max(1, Math.round(h * 2 / 768f));
    }

    public void mouseScroll(double xoffset, double yoffset) {
        if(screen != null) screen.mouseScroll(xoffset, yoffset);
    }
    
    public void runScriptFromFile(String path) {
        File file = new File("data", path);
        if(!file.exists()) {
            System.out.println("No such file "+file.getAbsolutePath()+"!");
            return;
        }
        
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            byte[] chars = new byte[dis.available()];
            dis.readFully(chars);
            dis.close();
            dis = null;
            
            String script = new String(chars);
            LuaValue chunk = lua.load(script);
            chunk.call();
            
        } catch(Exception e) {
            if(dis != null) {
                try{
                    dis.close();
                } catch(Exception ee) {}
            }
            e.printStackTrace();
        }
    }

    public void runScript(String script) {
        try {
            LuaValue chunk = lua.load(script);
            chunk.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void printLua(LuaValue val, PrintStream out) {
        if(val.istable()) {
            out.println("tab");
            
            LuaValue k = LuaValue.NIL;
            while(true) {
                Varargs next = val.next(k);
                k = next.arg1();
                if(k.isnil()) break;
                
                out.println(next.arg1().toString());
                
                printLua(next.arg(2), out);
            }
            out.println("NIL");
        } else {
            out.println("val");
            out.println(val.toString());
        }
    }

}