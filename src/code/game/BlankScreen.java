package code.game;

import code.Screen;
import code.utils.FPS;


/**
 *
 * @author Roman Lahin
 */
public class BlankScreen extends Screen {
    
    Main main;
    long waitEndTime;
    int color;
    
    public BlankScreen(Main main, long waitTime, int color) {
        this.main = main;
        this.color = color;
        this.waitEndTime = FPS.currentTime + waitTime;
    }
    
    public void tick() {
        main.e3d.clearColor(color);
        main.e3d.flush();
        
        if(FPS.currentTime > waitEndTime) action();
    }
    
    public void action() {};

}
