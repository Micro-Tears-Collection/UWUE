package code.game;

import code.engine.Screen;
import code.engine3d.Texture;

import code.ui.itemList.ItemList;
import code.ui.itemList.TextItem;
import code.utils.assetManager.AssetManager;
import code.utils.FPS;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class Menu extends Screen {
    
    private boolean initialized;
    private int w, h;
    
    private Main main;
    private ItemList menu;
    public Texture smoke, smoke2, shadow, shadow2;
    
    public Menu(Main main) {
        this.main = main;
        
        w = main.getWidth(); h = main.getHeight();
    }
    
    public void show() {
        if(initialized) return;
        initialized = true;
        
        smoke = main.e3d.getTexture("/images/smoke.png", null);
        smoke2 = main.e3d.getTexture("/images/smoke2.png", null);
        shadow = main.e3d.getTexture("/images/menushadow.png", null);
        shadow2 = main.e3d.getTexture("/images/menushadow2.png", null);
        
        /*main.musPlayer.loadFile("/music/menu.ogg");
        main.musPlayer.play();*/
        
        menu = ItemList.createItemList(w/2, h, main.font, main.selectedS);
        setMenuText();
    }
    
    public void destroy() {
        /*main.musPlayer.stop();
        main.musPlayer.free();*/
        AssetManager.destroyThings(AssetManager.CONTENT);
    }

    private void setMenuText() {
        menu.removeAll();
        final Menu thisMenu = this;
        
        menu.add(new TextItem("Play", main.font) {
            public void onEnter() {
				main.clickedS.play();
                /*main.musPlayer.setVolume(0);
                main.musPlayer.stop();*/
                /*main.gameStartS.play();

                main.window.showCursor(false);

                BlankScreen blank = new BlankScreen(main, 5000, 0) {

                    public void action() {*/
                        Game game = new Game(main);
                        main.setScreen(game, true);
                        game.loadMap(main.gamecfg.get("game", "start_map"));
                        game.setFade(new Fade(true, 0, 1000));
                    /*}
                };
                main.setScreen(blank, true);*/
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("Options", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.setScreen(new Settings(main, thisMenu));
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("About", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.setScreen(new About(main, thisMenu));
            }
        }.setHCenter(true));
        
        menu.add(new TextItem("Exit", main.font) {
            public void onEnter() {
                main.clickedS.play();
                main.closeGame();
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
		main.hudRender.drawRect(0, 0, w, h, 0, 1);
        
        int sizeb = (int) (Math.min(w, h) / 1.7f);
		
        double scrollXX = FPS.currentTime*(-13.)/1000;
        scrollXX -= Math.floor(scrollXX/sizeb)*sizeb;
        double scrollYY = FPS.currentTime*(13.)/1000;
        scrollYY -= Math.floor(scrollYY/sizeb)*sizeb;
        
        float bx = (float) (scrollXX % sizeb);
        float by = (float) (scrollYY % sizeb);
        while(bx>0) bx-=sizeb;
        while(by>0) by-=sizeb;
        bx %= w;
        by %= h;
        
        float sby = by;
        
        while(bx < w) {
            by = sby;
            while(by < h) {
                main.hudRender.drawRect(smoke2, bx, by, sizeb, sizeb, 0xffffff, 0.2f);
                by += sizeb;
            }
            bx += sizeb;
        }
		
		main.hudRender.drawRect(shadow, 0, 0, w, h, 0, 1, 1, 0, 0xffffff, 1f);
		
        sizeb = (int) (Math.min(w, h) * 1.5f);
        
        scrollXX = FPS.currentTime*(5.)/1000;
        scrollXX -= Math.floor(scrollXX/sizeb)*sizeb;
        scrollYY = FPS.currentTime*(-5.)/1000;
        scrollYY -= Math.floor(scrollYY/sizeb)*sizeb;
        
        bx = (float)(scrollXX % sizeb);
        by = (float)(scrollYY % sizeb);
        while(bx>0) bx-=sizeb;
        while(by>0) by-=sizeb;
        bx %= w;
        by %= h;
        
        sby = by;
        
        while(bx < w) {
            by = sby;
            while(by < h) {
                main.hudRender.drawRect(smoke, bx, by, sizeb, sizeb, 0xffffff, 0.07f);
                by += sizeb;
            }
            bx += sizeb;
        }
    }
    
    public void tick() {
        drawBackground();
        
        main.hudRender.drawRect(shadow2, 0, 0, w, h, 1, 0, 0, 1, 0xffffff, 0.5f);
		
		BMFont font = main.font;
		
		float scale1 = 1.5f + (float)(Math.sin(FPS.currentTime / 900f)) * 0.01f;
		float sin = (float)Math.sin(FPS.currentTime / 700f + 1000) * main.font.getHeight() * scale1;
        float cos = (float)Math.cos(FPS.currentTime / 405f) * main.font.getHeight() * scale1;
		float scale2 = 1.5f + (float)(Math.cos(FPS.currentTime / 800f + 1500)) * 0.01f;
		float sin2 = (float)Math.sin(FPS.currentTime / 420f + 300) * main.font.getHeight() * scale2;
        float cos2 = (float)Math.cos(FPS.currentTime / 550f + 100) * main.font.getHeight() * scale2;
		
		float a1 = 0.8f - (float)(0.5f + Math.sin(FPS.currentTime / 300f) / 2f) * 0.3f,
				a2 = 0.8f - (float)(0.5f + Math.sin(FPS.currentTime / 350f + 1500) / 2f) * 0.3f;
		
		float stringW = font.stringWidth("Meet me") * scale1;
		font.drawString(main.hudRender, "Meet me", 
				w*3/4-stringW/2+cos*0.02f, 
				h/2-font.getHeight()*scale1+sin*0.02f, 
				scale1, 0xffffff, a1);
		stringW = font.stringWidth("tonight") * scale2;
		font.drawString(main.hudRender, "tonight", 
				w*3/4-stringW/2+cos2*0.02f, 
				h/2+sin2*0.02f, 
				scale2, 0xffffff, a2);

        menu.mouseUpdate(0, 0, (int)main.getMouseX(), (int)main.getMouseY());
        
        menu.draw(main.hudRender, 0, 0, main.fontColor, main.fontSelColor);
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
            menu.mouseAction(0, 0, (int)main.getMouseX(), (int)main.getMouseY(), pressed);
    }

}
