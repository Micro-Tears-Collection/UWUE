package code.game;

import code.Engine;
import code.Screen;
import code.engine3d.E3D;
import code.game.world.World;
import code.game.world.WorldLoader;
import code.game.world.entities.Player;
import code.utils.Asset;
import code.utils.FPS;
import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class Game extends Screen {
    
    public Main main;
    
    public long time;
    public boolean paused;
    int w, h;
    int prevMX, prevMY;
    
    public E3D e3d;
    public DialogScreen dialog;
    public World world;
    public Player player;
    
    Fade fade;
    String nextMap;
    
    public Game(Main main) {
        this.main = main;
        w = getWidth(); h = getHeight();
        
        e3d = main.e3d;
        
        dialog = new DialogScreen();
        
        Engine.hideCursor(true);
        player = new Player();
    }
    
    public void loadMap(String nextMap) {
        this.nextMap = nextMap;
    }
    
    private void loadMapImpl() {
        long loadtime = System.currentTimeMillis();
        WorldLoader.loadWorld(this, nextMap);
        nextMap = null;
        FPS.previousFrame += System.currentTimeMillis() - loadtime;
    }
    
    public void setFade(Fade fade) {
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
        
        Asset.destroyVBOs();
        Asset.destroyThings(true, false);
    }
    
    public void tick() {
        if(nextMap != null) loadMapImpl();
        
        render();
        update();
        
        
        if(userTryingToCloseApp()) {
            main.stop();
        }
    }
    
    void update() {
        if(paused) return;
        
        if(Engine.hideCursor && isFocused()) {
            player.rotY -= (getMouseX() - (w >> 1)) * 60f / h;
            player.rotX -= (getMouseY() - (h >> 1)) * 60f / h;
            player.rotX = Math.max(Math.min(player.rotX, 89), -89);
            
            setCursorPos(w >> 1, h >> 1);
        }
        
        world.update(player);
        
        time += FPS.frameTime;
    }
    
    void render() {
        float py = player.pos.y;
        player.pos.y += player.eyeHeight;
        e3d.setCam(player.pos, player.rotX, player.rotY, 70, w, h);
        player.pos.y = py;
        
        world.render(e3d, w, h);
        
        e3d.prepare2D(0, 0, w, h);
        
        main.font.drawString("FPS: "+FPS.fps, 10, 10, 1, main.fontColor);
        
        if(fade != null) {
            float intensity = fade.step(e3d, w, h);
            main.musPlayer.setVolume(1-intensity);
            if(fade.checkDone()) fade = null;
        }
    }
    
    public void keyPressed(int key) {
        if(Keys.isThatBinding(key, Keys.ESC)) {
            if(fade == null) {
                paused = true;
                setFade(new Fade(false, 0xffffff, 1000) {
                    public void onDone() {
                        main.setScreen(new Menu(main), true);
                    };
                });
            }
            return;
        } else if(Keys.isThatBinding(key, Keys.OK)) {
            if(paused) return; 
            /*Ray ray = new Ray();
            ray.start.set(player.pos);
            ray.start.add(0, player.eyeHeight, 0);

            final float xa = 1f;
            float ya = xa * (float) Math.cos(Math.toRadians(player.rotX));
            float yaYDSin = ya * (float) -Math.sin(Math.toRadians(player.rotY));
            float yaYDCos = ya * (float) -Math.cos(Math.toRadians(player.rotY));

            float xOffset = yaYDSin;
            float yOffset = xa * (float) Math.sin(Math.toRadians(player.rotX));
            float zOffset = yaYDCos;
            ray.dir.set(xOffset * 4000, yOffset * 4000, zOffset * 4000);

            world.rayCast(ray);

            if(ray.collision) {
                System.out.println(ray.collisionPoint.x + " " + ray.collisionPoint.y + " " + ray.collisionPoint.z);
            }*/
            dialog.set(new String[]{
                "Тесттесттесттетсттстстесттесттесттесттетсттстстесттесттесттесттетсттстстесттест*тесттесттесттетсттстстесттесттесттесттетсттстстесттест*тест*тесттесттетсттстстесттест*тест*еее*Привет привет!!!", 
                "Второй экран!", "$question 4", "Сделай выбор!", "111", "222", "333", "444", "$end", "$end", "$end", "чотыре"
            }, this, main.font);
            dialog.show();
        }
    }
    
    public void keyReleased(int key) {
        
    }
    
    public void mouseAction(int button, boolean pressed) {
        
    }
    
    public void mouseScroll(double x, double y) {
        
    }

    public void sizeChanged(int w, int h, Screen from) {
        this.w = w; this.h = h;
        if(from != dialog && dialog != null) dialog.sizeChanged(w, h, this);
    }

}
