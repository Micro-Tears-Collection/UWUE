package code.game;

import code.Engine;
import code.Screen;
import code.engine3d.E3D;
import code.game.world.World;
import code.game.world.WorldLoader;
import code.game.world.entities.Player;
import code.utils.FPS;
import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class Game extends Screen {
    
    public Main main;
    
    public boolean run;
    int w, h;
    int prevMX, prevMY;
    
    public E3D e3d;
    public DialogScreen dialog;
    public World world;
    public Player player;
    
    public Game(Main main) {
        this.main = main;
        w = getWidth(); h = getHeight();
        
        e3d = main.e3d;
        
        dialog = new DialogScreen();
        
        Engine.hideCursor(true);
        player = new Player();
    }
    
    public void loadMap(String world) {
        WorldLoader.loadWorld(this, world);
    }

    public void destroy() {
        
    }
    
    public void start() {
        if(!run) {
            run = true;
        }
    }
    public void stop() {run = false;}
    public boolean isRunning() {return run;}
    
    public void tick() {
        render();
        update();
        
        e3d.flush();
        
        if(userTryingToCloseApp()) {
            stop();
            main.stop();
        }
    }
    
    void update() {
        if(Engine.hideCursor && isFocused()) {
            player.rotY -= (getMouseX() - (w >> 1)) * 60f / h;
            player.rotX -= (getMouseY() - (h >> 1)) * 60f / h;
            player.rotX = Math.max(Math.min(player.rotX, 89), -89);
            
            setCursorPos(w >> 1, h >> 1);
        }
        
        world.update(player);
    }
    
    void render() {
        float py = player.pos.y;
        player.pos.y += player.eyeHeight;
        e3d.setCam(player.pos, player.rotX, player.rotY, 70, w, h);
        player.pos.y = py;
        
        world.render(e3d, w, h);
        
        e3d.ortho(w, h);
        e3d.prepare2D(0, 0, w, h);
        
        main.font.drawString("FPS: "+FPS.fps, 10, 10, 1, main.fontColor);
    }
    
    public void keyPressed(int key) {
        if(Keys.isThatBinding(key, Keys.ESC)) {
            Engine.hideCursor(!Engine.hideCursor);
            return;
        } else if(Keys.isThatBinding(key, Keys.OK)) {
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
