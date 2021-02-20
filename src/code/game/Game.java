package code.game;

import code.engine.Engine;
import code.engine.Screen;

import code.engine3d.E3D;
import code.engine3d.Material;

import code.game.world.World;
import code.game.world.WorldLoader;
import code.game.world.entities.Entity;
import code.game.world.entities.Player;

import code.math.Vector3D;

import code.ui.itemList.ItemList;

import code.utils.assetManager.AssetManager;
import code.utils.FPS;
import code.utils.Keys;

import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Game extends Screen {
    
    public Main main;
    
    public long time;
    public boolean paused;
    private int w, h;
    
    public E3D e3d;
    public DialogScreen dialog;
    
    public String currentMap;
    public World world;
    public Player player;
    
    private boolean inPauseScreen;
    private ItemList pauseScreen;
    
    private Fade fade, wakeUpFade;
    private Vector<Pause> pauses;
    private Entity toActivate;
    private Material handIcon;
    
    //Load map
    public String nextMap;
    public Vector3D newPlayerPos;
    public float nextRotX, nextRotY;
    
    //Load dialog
    public String nextDialog;
    public boolean loadDialogFromFile;
    
    public Game(Main main) {
        this.main = main;
        w = getWidth(); h = getHeight();
        Engine.showCursor(false);
        
        e3d = main.e3d;
        dialog = new DialogScreen();
        pauseScreen = ItemList.createItemList(w, h, main.font, main.selectedS);
        setPauseScreenItems();
        
        handIcon = Material.get("/images/hand.png;alpha_test=blend");
        handIcon.tex.lock();
        
        pauses = new Vector();
        
        player = new Player();
    }
    
    private void setPauseScreenItems() {
        pauseScreen.set(new String[]{"CONTINUE","OPTIONS","WAKE UP"}, main.font, true);
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
    
    public void loadMapImpl() {
        toActivate = null;
        long loadtime = System.currentTimeMillis();
        
        WorldLoader.loadWorld(this, nextMap, newPlayerPos, nextRotX, nextRotY);
        currentMap = nextMap;
        
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
    
    public void openDialogImpl() {
        long loadtime = System.currentTimeMillis();
        
        if(loadDialogFromFile) dialog.load(nextDialog, this, main.font);
        else dialog.set(nextDialog, this, main.font);
        
        nextDialog = null;
        
        if(main.getScreen() != dialog) {
            dialog.open();
        }
        
        FPS.previousFrame += System.currentTimeMillis() - loadtime;
    }
    
    public void addPause(Pause pause) {
        pauses.add(pause);
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
        
        handIcon.tex.unlock();
        AssetManager.destroyThings(AssetManager.ALL_EXCEPT_LOCKED);
        
        main.clearLua();
        
        Engine.showCursor(true);
    }
    
    public void tick() {
        if(!inPauseScreen) {
            if(nextMap != null) loadMapImpl();
            if(nextDialog != null) {
                openDialogImpl();
                return;
            }
        }
        
        update();
        render();
    }
    
    private void update() {
        if(isPaused()) {
            world.pausedAnimate(null);
            
            if(!inPauseScreen && !paused && !pauses.isEmpty()) {
                Pause pause = pauses.firstElement();
                
                if(pause.update()) pauses.removeElementAt(0);
            }
            
            return;
        }
        
        if(!showCursor() && isFocused()) {
            float lookSpeed = 60f / h * main.conf.mouseLookSpeed / 100f;
            
            player.rotY -= (getMouseX() - (w >> 1)) * lookSpeed;
            player.rotX -= (getMouseY() - (h >> 1)) * lookSpeed;
            
            setCursorPos(w >> 1, h >> 1);
        }
        float lookSpeed = FPS.frameTime * 0.1f * main.conf.keyboardLookSpeed / 100f;
        
        player.rotY += ((Keys.isPressed(Keys.LEFT)?1:0) - (Keys.isPressed(Keys.RIGHT)?1:0)) * 2 * lookSpeed;
        player.rotX += ((Keys.isPressed(Keys.UP)?1:0) - (Keys.isPressed(Keys.DOWN)?1:0)) * lookSpeed;
        player.rotX = Math.max(Math.min(player.rotX, 89), -89);
        
        world.update(player);
        if(player.isAlive()) {
            world.activateObject(main, player, false);
            toActivate = world.findObjectToActivate(player, true);
        } else {
            toActivate = null;
            wakeUp();
        }
        
        time += FPS.frameTime;
    }
    
    public void render() {
        float py = player.pos.y;
        player.pos.y += player.eyeHeight;
        e3d.setCam(player.pos, player.rotX, player.rotY, main.conf.fov, w, h);
        player.pos.y = py;
        
        world.render(e3d, w, h);
        
        e3d.prepare2D(0, 0, w, h);
        
        if(toActivate != null) {
            float sizeh = Math.max(1, Math.round(Math.min(w, h) / 20f / handIcon.tex.h)) * handIcon.tex.h;
            float sizew = sizeh * handIcon.tex.w / handIcon.tex.h;
            e3d.drawRect(handIcon, 
                    Math.round((w-sizew)/2), Math.round((h-sizeh)/2), 
                    sizew, sizeh, 0xffffff, 1);
        }
        
        if(main.conf.debug) {
            main.font.drawString("FPS: " + FPS.fps, 10, 10, 1, main.fontColor);
            main.font.drawString(
                    Math.round(player.pos.x) + ", " + Math.round(player.pos.y) + ", " + Math.round(player.pos.z),
                    10, 10 + main.font.getHeight(), 1, main.fontColor);
        }
        
        if(inPauseScreen && main.getScreen() == this) {
            e3d.drawRect(null, 0, 0, w, h, 0, 0.5f);
            
            pauseScreen.mouseUpdate(0, 0, getMouseX(), getMouseY());
            pauseScreen.draw(main.e3d, 0, 0, main.fontColor, main.fontSelColor);
        }
        
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
    
    private void togglePauseScreen() {
        if(inPauseScreen) {
            setCursorPos(w >> 1, h >> 1);
            Engine.showCursor(false);
        } else {
            Engine.showCursor(true);
            setCursorPos(w >> 1, (h - main.font.getHeight()) >> 1);
        }
        
        inPauseScreen ^= true;
    }
    
    private void pauseClicked() {
        int index = pauseScreen.getIndex();

        if(index == 0) {
            main.clickedS.play();
            togglePauseScreen();
        } else if(index == 1) {
            main.clickedS.play();
            main.setScreen(new Settings(main, this));
        } else if(index == 2) {
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
        
        if(!isPaused()) {
            if(Keys.isThatBinding(key, Player.INTERACT) && toActivate != null) {
                Entity tmp = toActivate;
                toActivate = null;
                tmp.activate(main);
            }
        } else if(inPauseScreen) {
            if(Keys.isThatBinding(key, Keys.DOWN)) {
                pauseScreen.down();
            } else if(Keys.isThatBinding(key, Keys.UP)) {
                pauseScreen.up();
            }

            if(Keys.isThatBinding(key, Keys.OK)) {
                pauseClicked();
            }
        }
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(isWakingUp()) return;
        
        if(!pressed && button == MOUSE_LEFT) {
            if(inPauseScreen) {
                if(pauseScreen.isInBox(0, 0, getMouseX(), getMouseY())) pauseClicked();
            }
        } else if(!pressed && button == MOUSE_RIGHT) {
            if(!isPaused() && main.conf.debug) world.debugPos(player);
        }
    }
    
    public void mouseScroll(double x, double y) {
        if(isWakingUp()) return;
        
        if(inPauseScreen) {
            int scroll = (int) (y * main.font.getHeight() / 2f);
            pauseScreen.scroll(scroll);
        }
    }

    public void sizeChanged(int w, int h, Screen from) {
        this.w = w; this.h = h;
        pauseScreen.setSize(w, h);
        setPauseScreenItems();
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
    
    private boolean isPaused() {
        return inPauseScreen || paused || !pauses.isEmpty();
    }
    
    private boolean isWakingUp() {
        return wakeUpFade != null && fade == wakeUpFade;
    }

}
