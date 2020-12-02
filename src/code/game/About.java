package code.game;

import code.Screen;
import code.ui.TextView;
import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class About extends Screen {
    
    Main main;
    Menu menu;
    
    TextView text;
    
    public About(Main main, Menu menu) {
        this.main = main;
        this.menu = menu;
        
        setText();
    }
    
    public void destroy() {
        menu.destroy();
    }
    
    void setText() {
        text = new TextView(null, getWidth(), getHeight(), main.font);
        text.setCenter(true);
        text.setVCenter(true);
        text.setString("LSD Dream Emulator jam fan game by arki and romochka**"
                + "Special for LSDJAM2020**"
                + "Controls:*"
                + "WASD - walk*"
                + "Space - jump*"
                + "LMB - interact*"
                + "ESC - back*"
                + "Enter - confirm*"
                + "F2 - save screenshot*"
                + "F11 - toggle fullscreen*"
                + "*2020*UWUEngine");
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        setText();
        menu.sizeChanged(w, h, this);
    }
    
    public void tick() {
        menu.drawBackground();
        main.e3d.drawRect(null, 0, 0, getWidth(), getHeight(), 0, 0.5f);
        text.paint(main.e3d, 0, 0, main.fontColor);
        
        step();
    }

    private void step() {
        if(Keys.isPressed(Keys.DOWN)) text.scroll(-3);
        if(Keys.isPressed(Keys.UP)) text.scroll(3);
    }
    
    public void keyReleased(int key) {
        if(Keys.isThatBinding(key, Keys.ESC)) {
            Keys.reset();
            main.clickedS.play();
            main.setScreen(menu);
        }
    }
    
    public void mouseAction(int key, boolean pressed) {
        if(key == Screen.MOUSE_LEFT && !pressed) {
            Keys.reset();
            main.clickedS.play();
            main.setScreen(menu);
        }
    }
    
    public void mouseScroll(double xx, double yy) {
        text.scroll((int) (yy*main.scrollSpeed()));
    }

}
