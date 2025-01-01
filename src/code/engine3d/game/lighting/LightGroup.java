package code.engine3d.game.lighting;

import code.engine3d.E3D;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author Roman Lahin
 */
public class LightGroup {
    public static ArrayList<Light> allLights = new ArrayList<>();
    public static ArrayList<LightGroup> lightgroups = new ArrayList<>();
    public static LightGroup defaultGroup;
    
    private static ArrayList<Light> renderLights = new ArrayList<>();
    private static int activeLightsCount = 0;
    
    public String name;
    public ArrayList<Light> lights;
    public float[] ambient;
    
    public static void clear(boolean recreate) {
        for(LightGroup group : lightgroups) {
            group.destroy();
        }
        lightgroups.clear();
        defaultGroup = null;
        
        for(Light light : allLights) {
            light.destroy();
        }
        allLights.clear();
        
        if(recreate) {
            defaultGroup = new LightGroup("default");
            lightgroups.add(defaultGroup);
        }
    }
    
    public LightGroup(String name) {
        this.name = name;
        lights = new ArrayList<>();
        
        ambient = new float[] {0, 0, 0, 1};
    }
    
    public void destroy() {
        ambient = null;
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
    
    public void bind(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
		final float minInfluence = E3D.srgb_to_linear(1f/255f);
		
        e3d.setAmbientLight(ambient[0], ambient[1], ambient[2]);
            
        activeLightsCount = 0;
        for(Light light : lights) {
            if(light.isPoint) {
                //point source
                //calculate distance square
                float d = Math.max(Math.abs(x - light.posOrDir.x) - xs, 0);
                d *= d;
                float t = Math.max(Math.abs(y - light.posOrDir.y) - ys, 0);
                d += t*t;
                t = Math.max(Math.abs(z - light.posOrDir.z) - zs, 0);
                d += t*t;
                
                if(d != 0) light.influence = light.absLit * 10000 * 10 / d;
                else light.influence = Float.MAX_VALUE;
                
                if(light.influence <= minInfluence) continue;
            } else {
                //directional source
                light.influence = Float.MAX_VALUE;
            }
            
            renderLights.add(light);
            activeLightsCount++;
        }
        
        if(renderLights.size() > E3D.MAX_LIGHTS) {
			renderLights.sort((a, b) -> a.influence > b.influence ? -1 : (a.influence == b.influence ? 0 : 1));
		}
        
        activeLightsCount = Math.min(activeLightsCount, E3D.MAX_LIGHTS);
        
        for(int ii=0; ii<activeLightsCount; ii++) {
            Light light = renderLights.get(ii);
            
			if(light.isSpot) {
				e3d.setSpotLight(ii, light.posOrDir, light.spotDir, light.cutoff, light.color);
			} else if(light.isPoint) {
				e3d.setPointLight(ii, light.posOrDir, light.color);
			} else {
				e3d.setDirectionalLight(ii, light.posOrDir, light.color);
			}
        }
        renderLights.clear();
		
		e3d.sendLights();
    }
    
    public void unbind(E3D e3d) {
        e3d.setAmbientLight(1, 1, 1);
        
        for(int i=0; i<activeLightsCount; i++) {
			e3d.disableLight(i);
        }
		
		e3d.sendLights();
    }
    
    public static Light findLight(String find) {
        for(Light light : allLights) {
            if(find.equals(light.name)) return light;
        }
        
        return null;
    }
    
    public static LightGroup findLightGroup(String find) {
        for(LightGroup lightgroup : lightgroups) {
            if(find.equals(lightgroup.name)) return lightgroup;
        }
        
        return null;
    }

}
