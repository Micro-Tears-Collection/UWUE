package code.game;

import code.engine.Screen;
import code.engine3d.Texture;

import code.ui.itemList.ItemList;
import code.ui.itemList.TextItem;
import code.utils.assetManager.AssetManager;
import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class Menu extends Screen {
    
    private boolean initialized;
    private int w, h;
    
    private Main main;
    private ItemList menu;
    public Texture background, logo, shadow;
    
    public Menu(Main main) {
        this.main = main;
        
        w = main.getWidth(); h = main.getHeight();
    }
    
    public void show() {
        if(initialized) return;
        initialized = true;
        
        background = main.e3d.getTexture("/images/menu.png");
        logo = main.e3d.getTexture("/images/lsddejfg.png");
        shadow = main.e3d.getTexture("/images/menushadow.png");
        
        main.musPlayer.loadFile("/music/menu.ogg");
        main.musPlayer.play();
        
        menu = ItemList.createItemList(w/2, h, main.font, main.selectedS);
        setMenuText();
    }
    
    public void destroy() {
        main.musPlayer.stop();
        main.musPlayer.free();
        AssetManager.destroyThings(AssetManager.CONTENT);
    }

    private void setMenuText() {
        menu.removeAll();
        final Menu thisMenu = this;
        
        menu.add(new TextItem("START", main.font) {
            public void onEnter() {
                main.musPlayer.setVolume(0);
                main.musPlayer.stop();
                main.gameStartS.play();

                main.window.showCursor(false);

                BlankScreen blank = new BlankScreen(main, 5000, 0) {

                    public void action() {
                        Game game = new Game(main);
                        main.setScreen(game, true);
                        game.loadMap(main.gamecfg.get("game", "start_map"));
                        game.setFade(new Fade(true, 0, 1000));
                    }
                };
                main.setScreen(blank, true);
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("OPTIONS", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.setScreen(new Settings(main, thisMenu));
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("ABOUT", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.setScreen(new About(main, thisMenu));
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("EXIT", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.stop();
            }
        }.setHCenter(true));
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        this.w = w; this.h = h;
        menu.setSize(w/2, h);
        setMenuText();
    }
    
    public void drawBackground() {
        main.e3d.prepare2D(0, 0, w, h);
        
        int sizeb = Math.min(w, h);
        
        long scrollXX = FPS.currentTime*(-10)/1000;
        scrollXX -= (scrollXX/sizeb)*sizeb;
        long scrollYY = FPS.currentTime*(10)/1000;
        scrollYY -= (scrollYY/sizeb)*sizeb;
        
        int bx = (int)(scrollXX % sizeb);
        int by = (int)(scrollYY % sizeb);
        while(bx>0) bx-=sizeb;
        while(by>0) by-=sizeb;
        bx %= w;
        by %= h;
        
        int sby = by;
        
        while(bx < w) {
            by = sby;
            while(by < h) {
                main.hudRender.drawRect(background, bx, by, sizeb, sizeb, 0xffffff, 1);
                by += sizeb;
            }
            bx += sizeb;
        }
    }
    
    public void tick() {
        drawBackground();
        
        main.hudRender.drawRect(shadow, 0, 0, w, h, 0xffffff, 1);
        
        int logow = Math.min(h, w/2) * 3 / 4;
        int lx = (w/2 - logow) / 2;
        int ly = (h - logow) / 2;
        
        float sin = (float)Math.sin(FPS.currentTime / 200f) * logow * 0.05f;
        float cos = (float)Math.cos(FPS.currentTime / 200f) * logow * 0.05f;
        
        main.hudRender.drawRect(logo, lx+sin*2, ly+cos*2, logow, logow, 0, 1);
        main.hudRender.drawRect(logo, lx+sin, ly+cos, logow, logow, 0xffff00, 1);
        
        main.hudRender.drawRect(logo, lx, ly, logow, logow, 0xffffff, 1);

        menu.mouseUpdate(w / 2, 0, main.getMouseX(), main.getMouseY());
        
        menu.draw(main.hudRender, w / 2, 0, main.fontColor, main.fontSelColor);
    }
    
    public void keyPressed(int key) {
        menu.keyPressed(key);
    }
    
    public void keyRepeated(int key) {
        menu.keyRepeated(key);
    }
    
    public void mouseScroll(double xx, double yy) {
        int scroll = (int) (yy*main.font.getHeight()/2f);
        menu.mouseScroll(scroll);
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(button == Screen.MOUSE_LEFT) 
            menu.mouseAction(w/2, 0, main.getMouseX(), main.getMouseY(), pressed);
    }

}
