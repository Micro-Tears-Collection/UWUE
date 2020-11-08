package code.game;

import code.Engine;
import code.Screen;
import code.engine3d.E3D;
import code.engine3d.Material;
import code.game.world.World;
import code.game.world.WorldLoader;
import code.game.world.entities.Player;
import code.math.Vector3D;
import code.ui.ItemList;
import code.utils.Asset;
import code.utils.FPS;
import code.utils.Keys;
import org.luaj.vm2.LuaTable;

/**
 *
 * @author Roman Lahin
 */
public class Game extends Screen {
    
    public static final int PROPORTIONAL = 0, PROPORTIONAL_FULL = 1, FULL = 2;
    
    public Main main;
    
    public long time;
    public boolean paused;
    int w, h;
    int prevMX, prevMY;
    
    public E3D e3d;
    public DialogScreen dialog;
    LuaTable luasession;
    boolean inPauseScreen;
    ItemList pauseScreen;
    
    public String currentMap;
    public World world;
    public Player player;
    
    private Fade fade, wakeUpFade;
    //todo
    private Material image;
    private int imageScale = 0;
    private int imageBackgroundColor = 0;
    
    String nextMap;
    Vector3D newPlayerPos;
    float nextRotX, nextRotY;
    
    String nextDialog;
    boolean loadDialogFromFile;
    
    public Game(Main main) {
        this.main = main;
        w = getWidth(); h = getHeight();
        
        e3d = main.e3d;
        dialog = new DialogScreen();
        createPauseScreen();
        
        Engine.hideCursor(true);
        player = new Player();
    }
    
    public void createPauseScreen() {
        pauseScreen = new ItemList(new String[]{"CONTINUE","WAKE UP"}, 
                getWidth(), getHeight(), main.font) {
                    public void itemSelected() {
                        main.selectedS.play();
                    }
                };
    }
    
    public void loadMap(String nextMap) {
        loadMap(nextMap, null, Float.MAX_VALUE, Float.MAX_VALUE);
    }
    
    public void loadMap(String nextMap, Vector3D newPlayerPos, float rotX, float rotY) {
        this.nextMap = nextMap;
        this.newPlayerPos = newPlayerPos;
        this.nextRotX = rotX;
        this.nextRotY = rotY;
        this.nextDialog = null;
    }
    
    void loadMapImpl() {
        long loadtime = System.currentTimeMillis();
        
        WorldLoader.loadWorld(this, nextMap);
        currentMap = nextMap;
        
        if(newPlayerPos != null) player.pos.set(newPlayerPos);
        if(nextRotY != Float.MAX_VALUE) {
            player.rotX = 0;
            player.rotY = nextRotY;
        }
        if(nextRotX != Float.MAX_VALUE) player.rotX = nextRotX;
        
        nextMap = null;
        newPlayerPos = null;
        FPS.previousFrame += System.currentTimeMillis() - loadtime;
    }
    
    public void openDialog(String nextDialog, boolean load) {
        this.nextMap = null;
        this.nextDialog = nextDialog;
        this.loadDialogFromFile = load;
        
        if(main.getScreen() == dialog) {
            main.setScreen(dialog);
            openDialogImpl();
        }
    }
    
    void openDialogImpl() {
        long loadtime = System.currentTimeMillis();
        
        if(loadDialogFromFile) dialog.load(nextDialog, this, main.font);
        else dialog.set(nextDialog, this, main.font);
        
        nextDialog = null;
        
        if(main.getScreen() != dialog) {
            dialog.open();
        }
        
        FPS.previousFrame += System.currentTimeMillis() - loadtime;
    }
    
    public void setFade(Fade fade) {
        if(this.fade != null) System.out.println("warning! fade effect is already set!");
        this.fade = fade;
        main.musPlayer.setVolume(fade.in?0:1);
        //paused = true;
    }

    public void destroy() {
        paused = true;
        
        main.musPlayer.stop();
        main.musPlayer.free();
        main.musPlayer.setVolume(1);
        main.musPlayer.setPitch(1);
        
        Asset.destroyThings(Asset.ALL_EXCEPT_LOCKED);
        
        main.clearLua();
    }
    
    public void tick() {
        if(nextMap != null) loadMapImpl();
        if(nextDialog != null) {
            openDialogImpl();
            return;
        } 
        
        update();
        render();
        
        if(userTryingToCloseApp()) {
            main.stop();
        }
    }
    
    void update() {
        if(isPaused()) return;
        
        if(Engine.hideCursor && isFocused()) {
            player.rotY -= (getMouseX() - (w >> 1)) * 60f / h;
            player.rotX -= (getMouseY() - (h >> 1)) * 60f / h;
            player.rotX = Math.max(Math.min(player.rotX, 89), -89);
            
            setCursorPos(w >> 1, h >> 1);
        }
        
        world.update(player);
        world.activateSomething(main, player, false);
        
        time += FPS.frameTime;
    }
    
    void render() {
        float py = player.pos.y;
        player.pos.y += player.eyeHeight;
        e3d.setCam(player.pos, player.rotX, player.rotY, 70, w, h);
        player.pos.y = py;
        
        world.render(e3d, w, h);
        
        e3d.prepare2D(0, 0, w, h);
        
        if(inPauseScreen) {
            e3d.drawRect(null, 0, 0, getWidth(), getHeight(), 0, 0.5f);
            
            pauseScreen.mouseUpdate(0, 0, getMouseX(), getMouseY());
            pauseScreen.draw(main.e3d, 0, 0, main.fontColor, main.fontSelColor, false);
        }
        
        main.font.drawString("FPS: "+FPS.fps, 10, 10, 1, main.fontColor);
        main.font.drawString(
                Math.round(player.pos.x)+", "+Math.round(player.pos.y)+", "+Math.round(player.pos.z),
                10, 10+main.font.getHeight(), 1, main.fontColor);
        
        if(fade != null && (!inPauseScreen || isWakingUp())) {
            float intensity = fade.step(e3d, w, h);
            main.musPlayer.setVolume(1-intensity);
            if(fade.checkDone()) {
                Fade oldFade = fade;
                fade = null;
                oldFade.onDone();
            }
        }
    }
    
    public void togglePauseScreen() {
        if(inPauseScreen) {
            setCursorPos(w >> 1, h >> 1);
            Engine.hideCursor(true);
        } else {
            Engine.hideCursor(false);
            setCursorPos(w >> 1, (h - main.font.getHeight()) >> 1);
        }
        
        inPauseScreen ^= true;
    }
    
    public void pauseClicked() {
        int index = pauseScreen.getIndex();

        if(index == 0) {
            main.clickedS.play();
            togglePauseScreen();
        } else if(index == 1) {
            main.clickedS.play();
            wakeUp();
        } 
    }
    
    public void keyReleased(int key) {
        if(isWakingUp()) return;
        
        if(Keys.isThatBinding(key, Keys.ESC)) {
            main.clickedS.play();
            togglePauseScreen();
            return;
        }
        
        if(inPauseScreen) {
            Keys.reset();
            if(Keys.isThatBinding(key, Keys.DOWN)) {
                pauseScreen.scrollDown();
            } else if(Keys.isThatBinding(key, Keys.UP)) {
                pauseScreen.scrollUp();
            }

            if(Keys.isThatBinding(key, Keys.OK)) {
                pauseClicked();
            }
        }
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(isWakingUp()) return;
        
        if(!pressed && button == MOUSE_LEFT) {
            if(!isPaused()) world.activateSomething(main, player, true);
            else if(inPauseScreen) {
                if(pauseScreen.isInBox(0, 0, getMouseX(), getMouseY())) pauseClicked();
            }
        } else if(!pressed && button == MOUSE_RIGHT) {
            if(!isPaused()) world.debugPos(player);
        }
    }
    
    public void mouseScroll(double x, double y) {
        if(isWakingUp()) return;
        
        if(inPauseScreen) {
            int scroll = (int) (y * main.font.getHeight() / 2f);
            pauseScreen.scrollY(scroll);
        }
    }

    public void sizeChanged(int w, int h, Screen from) {
        this.w = w; this.h = h;
        createPauseScreen();
        if(from != dialog && dialog != null) dialog.sizeChanged(w, h, this);
    }
    
    public void wakeUp() {
        if(isWakingUp()) return;
        
        paused = true;
        wakeUpFade = new Fade(false, 0xffffff, 1000) {
            public void onDone() {
                main.setScreen(new Menu(main), true);
            };
        };
        setFade(wakeUpFade);
    }
    
    public boolean isPaused() {
        return inPauseScreen || paused || image != null;
    }
    
    public boolean isWakingUp() {
        return wakeUpFade != null && fade == wakeUpFade;
    }

}
