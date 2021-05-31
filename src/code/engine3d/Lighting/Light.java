package code.engine3d.Lighting;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Light {
    
    public String name;
    
    public FloatBuffer posOrDir;
    public FloatBuffer spotDir;
    public FloatBuffer color;
    
    boolean point;
    float absLit;
    
    public float cutoff = 45;
    
    //rendering stuff
    float influence;
    
    public Light(String name, float[] pod, float[] color, float[] spotDir) {
        this.name = name;
        
        point = pod[3] == 1;
        posOrDir = MemoryUtil.memAllocFloat(4);
        posOrDir.put(pod);
        posOrDir.rewind();
        
        this.color = MemoryUtil.memAllocFloat(4);
        if(spotDir != null) {
            this.spotDir = MemoryUtil.memAllocFloat(spotDir.length);
            this.spotDir.put(spotDir);
            this.spotDir.rewind();
        }
        
        setColor(color);
    }
    
    public void setColor(float[] color) {
        float[] arr = null;
        if(color.length == 1) arr = new float[]{color[0]/255f, color[0]/255f, color[0]/255f, 1};
        else if(color.length == 3) arr = new float[]{color[0]/255f, color[1]/255f, color[2]/255f, 1};
        
        absLit = Math.max(Math.abs(arr[0]), Math.abs(arr[1]));
        absLit = Math.max(absLit, Math.abs(arr[2]));
        
        this.color.clear();
        this.color.put(arr);
        this.color.rewind();
    }

    public void destroy() {
        MemoryUtil.memFree(posOrDir);
        MemoryUtil.memFree(color);
        if(spotDir != null) MemoryUtil.memFree(spotDir);
        
        posOrDir = color = spotDir = null;
    }

}
