package code.game;

import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class Pause {
    
    int time;
    
    public Pause(int time) {
        this.time = time;
    }
    
    public boolean update() {
        time -= FPS.frameTime;
        
        if(time <= 0) {
            onDone();
            return true;
        }
        
        return false;
    }
    
    public void onDone() {}

}
