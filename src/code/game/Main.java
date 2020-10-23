package code.game;

import code.utils.Scripting;
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
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author Roman Lahin
 */
public class Main {
    
    //bilding
    public static int TILDE, ERASE;

    public IniFile conf;
    
    public E3D e3d;
    public SoundSource musPlayer;
    public Globals lua;
    public LuaTable luasave;
    
    public BMFont font;
    public int fontColor, fontSelColor;

    boolean run = true;
    public Screen screen, nextScreen;
    boolean needToDestroyScreen;
    SoundSource selectedS, clickedS, gameStartS;
    
    boolean consoleOpen;
    String consoleText;

    public void init() {
        conf = Asset.loadIni("game.ini", true);
        Engine.setTitle(conf.get("game", "name"));
        
        musPlayer = ((SoundSource) Asset.getSoundSource().lock()).beMusicPlayer();
        selectedS = (SoundSource) Asset.getSoundSource("/sounds/select.ogg").lock();
        selectedS.buffer.neverUnload = true;
        clickedS = (SoundSource) Asset.getSoundSource("/sounds/click.ogg").lock();
        clickedS.buffer.neverUnload = true;
        gameStartS = (SoundSource) Asset.getSoundSource("/sounds/game start.ogg").lock();
        gameStartS.buffer.neverUnload = true;
        
        font = BMFont.loadFont(conf.get("hud", "font"));
        font.setInterpolation(false);
        setFontScale(Engine.h);
        
        fontColor = StringTools.getRGB(conf.getDef("hud", "font_color", "255,255,255"), ',');
        fontSelColor = StringTools.getRGB(conf.getDef("hud", "font_selected_color", "221,136,149"), ',');
        
        e3d = new E3D();
        
        lua = JsePlatform.standardGlobals();
        lua.set("save", (luasave = Scripting.load(this)));
        Scripting.initFunctions(this);

        setScreen(new Menu(this));

        run();
    }

    public Game getGame() {
        if(screen instanceof Game) return (Game)screen;
        else return null;
    }

    public Screen getScreen() {
        return screen;
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
        
        Scripting.save(luasave);
        
        e3d.destroy();
        font.destroy();
        
        Asset.destroyThings(Asset.ALL);
        
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

            if(screen != null/* && screen.isRunning()*/) {
                screen.tick();
            }
            
            if(consoleOpen) {
                if(!e3d.mode2D) e3d.prepare2D(0, 0, Engine.w, Engine.h);
                
                e3d.drawRect(null, 0, 0, Engine.w, font.getHeight(), 0, 0.5f);
                font.drawString(consoleText, 0, 0, 1, 0xffffff);
            }
            
            e3d.flush();

            try {
                Thread.sleep(Math.max(1, 8 - (System.currentTimeMillis() - FPS.previousFrame)));
                //max 125 fps
            } catch (Exception e) {}
            FPS.frameEnd();
        }

        destroy();
    }
    
    public void charInput(int codepoint) {
        if(consoleOpen) {
            char[] chrs = Character.toChars(codepoint);
            consoleText += String.valueOf(chrs, 0, chrs.length);
        }
    }

    public void keyReleased(int key) {
        if(!consoleOpen) Keys.keyReleased(key);
        
        if(Keys.isThatBinding(key, TILDE)) {
            consoleOpen ^= true;
            consoleText = consoleOpen?"":null;
            return;
        } else if(consoleOpen && Keys.isThatBinding(key, ERASE)) {
            if(consoleText.length() > 0) 
                consoleText = consoleText.substring(0, consoleText.length()-1);
            
        } else if(consoleOpen && Keys.isThatBinding(key, Keys.OK)) {
            consoleOpen = false;
            
            LuaValue val = runScript(consoleText);
            System.out.println("bool " + val.toboolean());
            System.out.println("int " + val.toint());
            System.out.println("num " + val.todouble());
            System.out.println("str " + val.tojstring());
            consoleText = null;
        }
        
        if(consoleOpen) return;
        
        if(screen != null) screen.keyReleased(key);
    }

    public void keyPressed(int key) {
        if(consoleOpen) return;
        
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
    
    public float scrollSpeed() {
        return font.getHeight()/2f;
    }
    
    public LuaValue loadScript(String script) {
        try {
            return lua.load(script);
        } catch (Exception e) {
            Engine.printError(e);
        }
        
        return null;
    }
    
    public LuaValue loadScriptFromFile(String path) {
        File file = new File("data", path);
        if(!file.exists()) {
            System.out.println("No such file "+file.getAbsolutePath()+"!");
            return null;
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
            return chunk;
            
        } catch(Exception e) {
            if(dis != null) {
                try{
                    dis.close();
                } catch(Exception ee) {}
            }
            Engine.printError(e);
        }
        
        return null;
    }
    
    public void runScriptFromFile(String path) {
        LuaValue chunk = loadScriptFromFile(path);
        if(chunk != null) chunk.call();
    }

    public LuaValue runScript(String script) {
        try {
            LuaValue chunk = lua.load(script);
            return chunk.call();
        } catch (Exception e) {
            Engine.printError(e);
        }
        
        return LuaValue.NIL;
    }

    public LuaValue runScript(LuaValue chunk) {
        try {
            return chunk.call();
        } catch (Exception e) {
            Engine.printError(e);
        }
        
        return LuaValue.NIL;
    }

}