package code.game;

import code.Engine;
import code.Screen;
import code.engine3d.Material;
import code.engine3d.Texture;
import code.ui.ItemList;
import code.utils.FPS;
import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class Menu extends Screen {
    
    Main main;
    ItemList menu;
    Material background, logo, shadow;
    
    public Menu(Main main) {
        this.main = main;
        
        background = new Material(Texture.createTexture("/images/menu.png"));
        logo = new Material(Texture.createTexture("/images/lsddejfg.png"));
        logo.alphaTest = true;
        shadow = new Material(Texture.createTexture("/images/menushadow.png"));
        shadow.blendMode = Material.BLEND;
        
        main.musPlayer.loadFile("/music/menu.ogg");
        main.musPlayer.start();
        
        Engine.hideCursor(false);
        
        createMenu();
    }
    
    public void destroy() {
        background.tex.destroy();
        logo.tex.destroy();
        shadow.tex.destroy();
    }

    private void createMenu() {
        menu = new ItemList(new String[]{"START","OPTIONS","ABOUT","EXIT"}, 
                getWidth()/2, getHeight(), main.font) {
                    public void itemSelected() {
                        main.selectedS.start();
                    }
                };
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        createMenu();
    }
    
    public void drawBackground() {
        main.e3d.prepare2D(0, 0, getWidth(), getHeight());
        main.e3d.ortho(getWidth(), getHeight());
        
        int sizeb = Math.min(getWidth(), getHeight());
        
        long scrollXX = FPS.currentTime*(-10)/1000;
        scrollXX -= (scrollXX/sizeb)*sizeb;
        long scrollYY = FPS.currentTime*(10)/1000;
        scrollYY -= (scrollYY/sizeb)*sizeb;
        
        int bx = (int)(scrollXX % sizeb);
        int by = (int)(scrollYY % sizeb);
        while(bx>0) bx-=sizeb;
        while(by>0) by-=sizeb;
        bx %= getWidth();
        by %= getHeight();
        
        int sby = by;
        
        while(bx < getWidth()) {
            by = sby;
            while(by < getHeight()) {
                main.e3d.drawRect(background, bx, by, sizeb, sizeb, 0xffffff, 1);
                by += sizeb;
            }
            bx += sizeb;
        }
    }
    
    public void tick() {
        drawBackground();
        
        main.e3d.drawRect(shadow, 0, 0, getWidth(), getHeight(), 0xffffff, 1);
        
        int logow = Math.min(getHeight(), getWidth()/2) * 3 / 4;
        int lx = (getWidth()/2 - logow) / 2;
        int ly = (getHeight() - logow) / 2;
        
        float sin = (float)Math.sin(FPS.currentTime / 200f) * logow * 0.05f;
        float cos = (float)Math.cos(FPS.currentTime / 200f) * logow * 0.05f;
        
        main.e3d.drawRect(logo, lx+sin*2, ly+cos*2, logow, logow, 0, 1);
        main.e3d.drawRect(logo, lx+sin, ly+cos, logow, logow, 0xffff00, 1);
        
        main.e3d.drawRect(logo, lx, ly, logow, logow, 0xffffff, 1);

        menu.mouseUpdate(getWidth() / 2, 0, getMouseX(), getMouseY());
        
        menu.draw(main.e3d, getWidth() / 2, 0, main.fontColor, main.fontSelColor, false);
        
        main.e3d.flush();
    }
    
    public void menuClicked() {
        int index = menu.getIndex();

        if(index == 0) {
            main.musPlayer.setVolume(0);
            main.musPlayer.stop();
            main.musPlayer.buffer.free();
            main.gameStartS.start();
        
            Engine.hideCursor(true);
            
            BlankScreen blank = new BlankScreen(main, 5000, 0) {
                
                public void action() {
                    Game game = new Game(main);
                    main.setScreen(game, true);
                    game.loadMap(main.conf.get("GAME", "START_MAP"));
                    game.start();
                    game.setFade(new Fade(true, 0, 1000));
                }
            };
            main.setScreen(blank, true);
            
        } else if(index == 2) {
            main.clickedS.start();
            main.setScreen(new About(main, this));
        } else if(index == 3) {
            main.clickedS.start();
            main.stop();
        }
    }
    
    public void keyPressed(int key) {
        if(Keys.isThatBinding(key, Keys.DOWN)) {
            menu.scrollDown();
            Keys.reset();
        } else if(Keys.isThatBinding(key, Keys.UP)) {
            menu.scrollUp();
            Keys.reset();
        }

        if(Keys.isThatBinding(key, Keys.OK)) {
            Keys.reset();
            menuClicked();
        }
    }
    
    public void mouseScroll(double xx, double yy) {
        int scroll = (int) (yy*main.font.getHeight()/2f);
        menu.scrollY(scroll);
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(pressed || button != Screen.MOUSE_LEFT) return;
        
        if(menu.isInBox(getWidth()/2, 0, getMouseX(), getMouseY())) menuClicked();
    }

}
