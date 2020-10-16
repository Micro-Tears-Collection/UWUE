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

/**
 *
 * @author Roman Lahin
 */
public class Main {

    public IniFile conf;
    
    public E3D e3d;
    public SoundSource musPlayer;
    
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

}