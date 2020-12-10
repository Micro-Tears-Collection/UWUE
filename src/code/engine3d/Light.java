package code.engine3d;

/**
 *
 * @author Roman Lahin
 */
public class Light {
    
    public String name;
    
    public float[] posOrDir;
    public float[] spotDir;
    public float[] color;
    
    public float cutoff = 45;
    
    //rendering stuff
    public float influence;
    
    public Light(String name, float[] pod, float[] color, float[] spotDir) {
        this.name = name;
        this.posOrDir = pod;
        this.color = color;
        this.spotDir = spotDir;
        
        setColor(color);
    }
    
    public void setColor(float[] color) {
        if(color.length == 1) this.color = new float[]{color[0]/255f, color[0]/255f, color[0]/255f, 1};
        else if(color.length == 3) this.color = new float[]{color[0]/255f, color[1]/255f, color[2]/255f, 1};
    }

}
