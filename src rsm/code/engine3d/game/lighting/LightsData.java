package code.engine3d.game.lighting;

import code.engine3d.E3D;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class LightsData {
	
    private ArrayList<Light> lights;
    public float[] ambient;
    
    public LightsData() {
        lights = new ArrayList<>();
        
        ambient = new float[] {0, 0, 0, 1};
    }
    
    public void destroy() {
        for(Light light : lights) {
            light.destroy();
        }
		
        ambient = null;
    }
	
	public void clear() {
        for(Light light : lights) {
            light.destroy();
        }
		
		lights.clear();
	}
    
    public void setAmbient(float[] ambient) {
        float[] arr = new float[] {0, 0, 0, 1};
        if(ambient.length == 1) {
            arr[0] = ambient[0]/255f;
            arr[1] = ambient[0]/255f;
            arr[2] = ambient[0]/255f;
        } else if(ambient.length == 3) {
            arr[0] = ambient[0]/255f;
            arr[1] = ambient[1]/255f;
            arr[2] = ambient[2]/255f;
        }
        
        this.ambient = arr;
    }
	
	public void addLight(Light light) {
		if(lights.size() >= E3D.MAX_LIGHTS) {
			System.out.println("Can't add more than " + E3D.MAX_LIGHTS + " light sources!");
			return;
		}
		
		lights.add(light);
	}
    
    public void bind(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
		e3d.setAmbientLight(ambient[0], ambient[1], ambient[2]);
		
		//todo store active light sources for every mesh in a separate data array
		
        for(int ii=0; ii<lights.size(); ii++) {
            Light light = lights.get(ii);
            
			if(light.isSpot) {
				e3d.setSpotLight(ii, light.posOrDir, light.spotDir, light.cutoff, light.color);
			} else if(light.isPoint) {
				e3d.setPointLight(ii, light.posOrDir, light.color);
			} else {
				e3d.setDirectionalLight(ii, light.posOrDir, light.color);
			}
        }
		
		e3d.sendLights();
    }
    
    public void unbind(E3D e3d) {
        e3d.setAmbientLight(1, 1, 1);
        
        for(int i=0; i<lights.size(); i++) {
			e3d.disableLight(i);
        }
		
		e3d.sendLights();
    }
    
    public Light findLight(String find) {
        for(Light light : lights) {
            if(find.equals(light.name)) return light;
        }
        
        return null;
    }

}
