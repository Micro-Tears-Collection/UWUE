package code.game;

import code.engine.Screen;
import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class BlankScreen extends Screen {
    
    private Main main;
    private long waitEndTime;
    private int color;
    
    BlankScreen(Main main, long waitTime, int color) {
        this.main = main;
        this.color = color;
        this.waitEndTime = FPS.currentTime + waitTime;
    }
    
    public void tick() {
        main.e3d.clearColor(color);
        
        if(FPS.currentTime > waitEndTime) action();
    }
    
    public void action() {}

}
