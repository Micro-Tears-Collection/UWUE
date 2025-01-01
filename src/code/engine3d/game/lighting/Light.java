package code.engine3d.game.lighting;

import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class Light {
    
    public String name;
    
    public Vector3D posOrDir;
    public Vector3D spotDir;
    public float[] color;
    
    public boolean isPoint, isSpot;
    float absLit;
    
    public float cutoff = 45;
    
    //rendering stuff
    float influence;
    
    public Light(String name, Vector3D posOrDir, boolean isPoint, Vector3D spotDir, float[] color) {
        this.name = name;
		
		this.posOrDir = posOrDir;
		this.isPoint = isPoint;
		this.spotDir = spotDir;
		isSpot = spotDir != null;
        
        setColor(color);
    }
    
    public void setColor(float[] color) {
        float[] arr = null;
        if(color.length == 1) arr = new float[]{color[0]/255f, color[0]/255f, color[0]/255f, 1};
        else if(color.length == 3) arr = new float[]{color[0]/255f, color[1]/255f, color[2]/255f, 1};
        
        absLit = Math.max(Math.abs(arr[0]), Math.abs(arr[1]));
        absLit = Math.max(absLit, Math.abs(arr[2]));
		
		this.color = arr;
    }

	public void destroy() {
		posOrDir = spotDir = null;
		color = null;
	}

}
