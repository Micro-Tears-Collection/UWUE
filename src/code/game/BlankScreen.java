package code.game;

import code.Screen;


/**
 *
 * @author Roman Lahin
 */
public class BlankScreen extends Screen {
    
    Main main;
    long waitTime;
    int color;
    
    public BlankScreen(Main main, long waitTime, int color) {
        this.main = main;
        this.color = color;
        this.waitTime = waitTime;
    }
    
    public void tick() {
        main.e3d.clearColor(color);
        main.e3d.flush();
        
        try {
            Thread.sleep(waitTime);
        } catch (Exception e) {}
        action();
    }
    
    public void action() {};

}
