package code.game;

import code.utils.Scripting;
import code.Engine;
import code.Screen;
import code.audio.SoundSource;
import code.engine3d.E3D;
import code.math.Vector3D;
import code.ui.TextBox;
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

    public IniFile gamecfg;
    public Configuration conf;
    
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
    
    TextBox textBox;
    TextBox console;
    
    public Main(int w, int h) {
        gamecfg = Asset.loadIni("game.ini", true);
        conf = new Configuration(w, h);
    }

    public void init() {
        Engine.setTitle(gamecfg.get("game", "name"));
        
        musPlayer = ((SoundSource) Asset.getSoundSource().lock()).beMusicPlayer();
        selectedS = (SoundSource) Asset.getSoundSource("/sounds/select.ogg").lock();
        selectedS.buffer.neverUnload = true;
        clickedS = (SoundSource) Asset.getSoundSource("/sounds/click.ogg").lock();
        clickedS.buffer.neverUnload = true;
        gameStartS = (SoundSource) Asset.getSoundSource("/sounds/game start.ogg").lock();
        gameStartS.buffer.neverUnload = true;
        
        font = BMFont.loadFont(gamecfg.get("hud", "font"));
        font.setInterpolation(false);
        setFontScale(Engine.h);
        
        fontColor = StringTools.getRGB(gamecfg.getDef("hud", "font_color", "255,255,255"), ',');
        fontSelColor = StringTools.getRGB(gamecfg.getDef("hud", "font_selected_color", "221,136,149"), ',');
        
        e3d = new E3D();
        
        clearLua();

        setScreen(new Menu(this));
        
        console = new TextBox(this, font) {
            public void cancel() {
                super.cancel();
                text = "";
            }
            
            public void enter() {
                super.enter();

                LuaValue val = runScript(text);
                System.out.println("bool " + val.toboolean());
                System.out.println("int " + val.toint());
                System.out.println("num " + val.todouble());
                System.out.println("str " + val.tojstring());
                
                text = "";
            }
        }.setXYW(0, 0, Engine.w);

        run();
    }
    
    public void openTextBox(TextBox textBox) {
        this.textBox = textBox;
    }
    
    public void closeTextBox() {
        this.textBox = null;
    }
    
    public void clearLua() {
        lua = JsePlatform.standardGlobals();
        lua.set("save", luasave==null?(luasave = Scripting.load(this)):luasave);
        Scripting.initFunctions(this);
    }

    public Game getGame() {
        if(screen instanceof Game) return (Game)screen;
        else if(screen instanceof DialogScreen) return ((DialogScreen) screen).game;
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
            
            if(textBox == console) {
                if(!e3d.mode2D) e3d.prepare2D(0, 0, Engine.w, Engine.h);
                console.draw(e3d);
            }
            
            e3d.flush();

            if(!conf.vsync) try {
                Thread.sleep(Math.max(1, 8 - (System.currentTimeMillis() - FPS.previousFrame)));
                //max 125 fps (todo: add support of 144hz monitors??)
            } catch (Exception e) {}
            FPS.frameEnd();
            
            if(screen != null && screen.userTryingToCloseApp()) {
                stop();
            }
        }

        destroy();
    }
    
    public void charInput(int codepoint) {
        if(textBox != null) {
            char[] chrs = Character.toChars(codepoint);
            textBox.addChars(chrs);
        }
    }

    public void keyReleased(int key) {
        if(textBox == null) Keys.keyReleased(key);
        
        if((textBox == console || textBox == null) && Keys.isThatBinding(key, TILDE)) {
            if(textBox == null) openTextBox(console);
            else console.cancel();
            
            return;
        } else if(textBox != null && Keys.isThatBinding(key, Keys.OK)) {
            textBox.enter();
            return;
        } else if(textBox != null && Keys.isThatBinding(key, Keys.ESC)) {
            textBox.cancel();
            return;
        }
        
        if(textBox != null) return;
        
        if(screen != null) screen.keyReleased(key);
    }

    public void keyPressed(int key) {
        if(textBox != null) {
            if(Keys.isThatBinding(key, ERASE)) {
                if(textBox.text.length() > 0)
                    textBox.text = textBox.text.substring(0, textBox.text.length() - 1);
            }

            return;
        }
        
        Keys.keyPressed(key);
        if(screen != null) screen.keyPressed(key);
    }

    public void mouseAction(int button, boolean pressed) {
        if(screen != null) screen.mouseAction(button, pressed);
    }

    public void sizeChanged(int w, int h, Screen scr) {
        setFontScale(h);
        console.setXYW(0, 0, w);
        
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

    public void loadMap(LuaValue mapArg, LuaValue data) {
        String map = mapArg.toString();
        Game game = getGame();

        Vector3D newPlayerPos = null;
        float rotX = Float.MAX_VALUE;
        float rotY = Float.MAX_VALUE;
        
        if(!data.isnil() && data.istable()) {
            LuaValue pos = data.get("pos");
            
            if(!pos.isnil() && pos.istable()) {
                newPlayerPos = new Vector3D(
                        pos.get(1).tofloat(),
                        pos.get(2).tofloat(),
                        pos.get(3).tofloat());
            }
            
            if(!data.get("rotX").isnil()) rotY = data.get("rotX").tofloat();
            if(!data.get("rotY").isnil()) rotY = data.get("rotY").tofloat();
        }

        if(game == null) {
            game = new Game(this);
            setScreen(game, true);
        }
        game.loadMap(map, newPlayerPos, rotX, rotY);
    }

}