package code.game;

import code.engine.Screen;

import code.engine3d.E3D;
import code.engine3d.FrameBuffer;
import code.engine3d.Shader;
import code.engine3d.Texture;

import code.game.world.World;
import code.game.world.WorldLoader;
import code.game.world.entities.Entity;
import code.game.world.entities.Player;

import code.math.Vector3D;

import code.ui.itemList.ItemList;
import code.ui.itemList.TextItem;

import code.utils.assetManager.AssetManager;
import code.utils.FPS;
import code.utils.Keys;

import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class Game extends Screen {
	
	public static final float DONT_ROTATE = Float.MAX_VALUE;
    
    public Main main;
    
    public long time;
    public boolean paused;
    private int w, h;
	private boolean sizeChanged;
    
    public E3D e3d;
    public DialogScreen dialog;
    
    public String currentMap;
    public World world;
    public Player player;
    
    private boolean inPauseScreen;
    private ItemList pauseScreen;
    
    private Fade fade, wakeUpFade;
    private ArrayList<Pause> pauses;
    private Entity toActivate;
    private Texture handIcon;
    
    //Load map
    public String nextMap;
    public Vector3D newPlayerPos;
    public float nextRotX, nextRotY;
    
    //Load dialog
    public String nextDialog;
    public boolean loadDialogFromFile;
    
    public Game(Main main) {
        this.main = main;
        w = main.getWidth(); h = main.getHeight();
        main.window.showCursor(false);
        
        e3d = main.e3d;
        dialog = new DialogScreen();
        pauseScreen = ItemList.createItemList(w, h, main.font, main.selectedS);
        setPauseScreenItems();
        
        handIcon = e3d.getTexture("/images/hand.png", null);
        handIcon.use();
        
        pauses = new ArrayList<>();
        
        player = new Player();
    }
    
    private void setPauseScreenItems() {
        pauseScreen.removeAll();
        final Game game = this;
        
        pauseScreen.add(new TextItem("Continue", main.font) {
            public void onEnter() {
                main.clickedS.play();
                togglePauseScreen();
            }
        }.setHCenter(true));
        
        pauseScreen.add(new TextItem("Settings", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.setScreen(new Settings(main, game));
            }
        }.setHCenter(true));
        
        pauseScreen.add(new TextItem("Close game", main.font) {
            public void onEnter() {
                main.clickedS.play();
                wakeUp();
            }
        }.setHCenter(true));
    }
    
    public void loadMap(String nextMap) {
        loadMap(nextMap, null, DONT_ROTATE, DONT_ROTATE);
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
            openDialogImpl();
        }
    }
    
    public void openDialogImpl() {
        long loadtime = System.currentTimeMillis();
        
        if(loadDialogFromFile) dialog.load(nextDialog, this, main.font);
        else dialog.set(nextDialog, this, main.font);
        
        nextDialog = null;
        
        if(main.getScreen() != dialog) {
            //main.setScreen(dialog);
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
        
        if(world != null) world.destroy();
		player.destroy();
        
        handIcon.free();
        
        AssetManager.destroyThings(AssetManager.CONTENT);
        
        main.clearLua();
        
        main.window.showCursor(true);
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
                Pause pause = pauses.get(0);
                
                if(pause.update()) pauses.remove(0);
            }
            
            return;
        }
        
        if(!main.window.isCursorVisible() && main.window.isFocused()) {
            if(!sizeChanged) {
				float lookSpeed = 60f / h * main.conf.mouseLookSpeed / 100f;
            
				player.rotY -= (main.getMouseX() - (w / 2)) * lookSpeed;
				player.rotX -= (main.getMouseY() - (h / 2)) * lookSpeed;
			}
            
            main.window.setCursorPos(w / 2, h / 2);
        }
		sizeChanged = false;
		
        float lookSpeed = FPS.frameTime * 0.1f * main.conf.keyboardLookSpeed / 100f;
        
        player.rotY += ((Keys.isPressed(Keys.LEFT)?1:0) - (Keys.isPressed(Keys.RIGHT)?1:0)) * 2 * lookSpeed;
        player.rotX += ((Keys.isPressed(Keys.UP)?1:0) - (Keys.isPressed(Keys.DOWN)?1:0)) * lookSpeed;
        player.rotX = Math.max(Math.min(player.rotX, 89), -89);
        
        world.update(player);
		
		Vector3D camPos = new Vector3D(player.pos);
		camPos.y += player.eyeHeight;
		Vector3D camSpeed = new Vector3D(player.speed);
		camSpeed.y += 8F * FPS.frameTime / 50;
		
		world.setCamera(camPos, camSpeed, player.rotX, player.rotY);
		world.setCameraFov(main.conf.fov);
		
        if(player.isAlive()) {
            world.activateObject(main, player, false);
            toActivate = world.findObjectToActivate(player, true);
        } else {
            toActivate = null;
            wakeUp();
        }
        
        time += FPS.frameTime;
    }
    
    public final int getViewportX() {
        return (w - getViewportW()) / 2;
    }
    
    public final int getViewportY() {
        return 0;
    }
    
    public final int getViewportW() {
        return w;
    }
    
    public final int getViewportH() {
        return h;
    }
    
    public void render() {
        int drawW = w, drawH = h;
        
        world.render(e3d, drawW, drawH);
        
        e3d.prepare2D(0, 0, w, h);
            
        int vpx = getViewportX();
        int vpy = getViewportY();
        int vpw = getViewportW();
        int vph = getViewportH();
        
        if(toActivate != null && !isPaused()) {
            float sizeh = Math.max(1, Math.round(Math.min(w, h) / 20f / handIcon.h)) * handIcon.h;
            float sizew = sizeh * handIcon.w / handIcon.h;
            
            main.hudRender.drawRect(handIcon, 
                    Math.round((w-sizew)/2), Math.round((h-sizeh)/2), 
                    sizew, sizeh, 
                    0xffffff, 1);
        }
        
        if(main.conf.debug) {
			main.font.drawString(main.hudRender, "FPS: " + FPS.fps, 10, 10, 1, main.fontColor);
            main.font.drawString(main.hudRender,
                    Math.round(player.pos.x) + ", " + Math.round(player.pos.y) + ", " + Math.round(player.pos.z),
                    10, 10 + main.font.getHeight(), 1, main.fontColor);
        }
        
        if(inPauseScreen && main.getScreen() == this) {
            main.hudRender.drawRect(0, 0, w, h, 0, 0.5f);
            
            pauseScreen.mouseUpdate(0, 0, (int)main.getMouseX(), (int)main.getMouseY());
            pauseScreen.draw(main.hudRender, 0, 0, main.fontColor, main.fontSelColor);
        }
        
        if(fade != null && (!inPauseScreen || isWakingUp())) {
            float intensity = fade.step(main.hudRender, vpx, vpy, vpw, vph);
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
            main.window.setCursorPos(w >> 1, h >> 1);
            main.window.showCursor(false);
        } else {
			if(!pauses.isEmpty()) return;
			
            main.window.showCursor(true);
            main.window.setCursorPos(w >> 1, (h - main.font.getHeight()) >> 1);
        }
        
        inPauseScreen ^= true;
    }
    
    public void keyPressed(int key) {
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
            pauseScreen.keyPressed(key);
        }
    }
    
    public void keyRepeated(int key) {
        if(isWakingUp()) return;
        
        if(inPauseScreen) {
            pauseScreen.keyRepeated(key);
        }
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(isWakingUp()) return;
        
        if(button == MOUSE_LEFT) {
            if(inPauseScreen) {
                pauseScreen.mouseAction(0, 0, (int)main.getMouseX(), (int)main.getMouseY(), pressed);
            }
        } else if(!pressed && button == MOUSE_RIGHT) {
            if(!isPaused() && main.conf.debug) world.debugPos(player);
        }
    }
    
    public void mouseScroll(double x, double y) {
        if(isWakingUp()) return;
        
        if(inPauseScreen) {
            int scroll = (int) (y * main.font.getHeight() / 2f);
            pauseScreen.mouseScroll(scroll);
        }
    }

    public void sizeChanged(int w, int h, Screen from) {
        this.w = w; this.h = h;
		sizeChanged = true;
		
        pauseScreen.setSize(w, h);
        setPauseScreenItems();
        if(from != dialog && dialog != null) dialog.sizeChanged(w, h, this);
    }
    
    public void wakeUp() {
        if(isWakingUp()) return;
        
        paused = true;
        wakeUpFade = new Fade(false, 0x000000, 1000) {
            public void onDone() {
                //main.setScreen(new Menu(main), true);
                main.closeGame();
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
