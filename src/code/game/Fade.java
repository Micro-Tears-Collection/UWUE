package code.game;

import code.engine3d.E3D;
import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class Fade {
    
    boolean in;
    public int len, max;
    public int color;
    
    public Fade(boolean in, int color, int len) {
        this.in = in;
        this.color = color;
        this.len = max = len;
    }
    
    public float step(E3D e3d, int w, int h) {
        if(len < 0) return 0;
        
        float intensity = (float) len / max;
        if(!in) intensity = 1 - intensity;

        e3d.drawRect(null, 0, 0, w, h, color, intensity);

        len -= FPS.frameTime;
        
        return Math.max(0, Math.min(1, intensity));
    }
    
    public boolean checkDone() {
        if(len <= 0) onDone();
        return len <= 0;
    }
    
    public void onDone() {}

}
