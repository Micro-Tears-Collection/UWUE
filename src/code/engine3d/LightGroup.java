package code.engine3d;

import java.util.Vector;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Roman Lahin
 */
public class LightGroup {
    public static Vector<Light> allLights = new Vector();
    public static Vector<LightGroup> lightgroups = new Vector();
    public static LightGroup defaultGroup;
    
    private static Vector<Light> renderLights = new Vector();
    private static int prevLightsCount = 0;
    
    public String name;
    public Vector<Light> lights;
    public float[] ambient = new float[] {0, 0, 0, 1};
    
    public LightGroup(String name) {
        this.name = name;
        lights = new Vector();
    }
    
    public void setAmbient(float[] ambient) {
        if(ambient.length == 1) {
            this.ambient[0] = ambient[0]/255f;
            this.ambient[0] = ambient[0]/255f;
            this.ambient[0] = ambient[0]/255f;
        } else if(ambient.length == 3) {
            this.ambient[0] = ambient[0]/255f;
            this.ambient[1] = ambient[1]/255f;
            this.ambient[2] = ambient[2]/255f;
        }
    }
    
    public void bind(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadMatrixf(e3d.invCamf);
            
        int i = 0;
        for(Light light : lights) {
            if(light.posOrDir[3] == 1) {
                //point source
                //calculate distance square
                float d = Math.max(Math.abs(x - light.posOrDir[0]) - xs, 0);
                d *= d;
                float t = Math.max(Math.abs(y - light.posOrDir[1]) - ys, 0);
                d += t*t;
                t = Math.max(Math.abs(z - light.posOrDir[2]) - zs, 0);
                d += t*t;
                
                if(d != 0) {
                    light.influence = Math.abs(light.color[0]) 
                            + Math.abs(light.color[1]) 
                            + Math.abs(light.color[2]);
                    
                    light.influence = light.influence * 10000 * 10 / d;
                } else light.influence = Float.MAX_VALUE;
                
                if(light.influence <= 3f/255f) continue;
            } else {
                //directional source
                light.influence = Float.MAX_VALUE;
            }
            
            renderLights.add(light);
            i++;
        }
        
        if(renderLights.size() > e3d.maxLights) sort(renderLights);
        
        i = Math.min(i, e3d.maxLights);
        
        for(int ii=0; ii<i; ii++) {
            Light light = renderLights.elementAt(ii);
            
            GL11.glLightf(GL11.GL_LIGHT0+ii, GL11.GL_CONSTANT_ATTENUATION, 0);
            GL11.glLightf(GL11.GL_LIGHT0+ii, GL11.GL_QUADRATIC_ATTENUATION, 0.0001F * 0.1f);
            
            GL11.glLightfv(GL11.GL_LIGHT0+ii, GL11.GL_DIFFUSE, light.color);
            //GL11.glLightfv(GL11.GL_LIGHT0+ii, GL11.GL_SPECULAR, light.color);
            GL11.glLightfv(GL11.GL_LIGHT0+ii, GL11.GL_POSITION, light.posOrDir);
            
            if(light.spotDir != null) {
                GL11.glLightf(GL11.GL_LIGHT0 + ii, GL11.GL_SPOT_CUTOFF, light.cutoff);
                GL11.glLightfv(GL11.GL_LIGHT0 + ii, GL11.GL_SPOT_DIRECTION, light.spotDir);
            } else {
                GL11.glLighti(GL11.GL_LIGHT0 + ii, GL11.GL_SPOT_CUTOFF, 180);
            }
            
            GL11.glEnable(GL11.GL_LIGHT0+ii);
        }
        renderLights.removeAllElements();
        
        int lc = i;
        for(;i<prevLightsCount; i++) {
            GL11.glDisable(GL11.GL_LIGHT0+i);
        }
        prevLightsCount = lc;
        
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, new float[]{1,1,1,1});
        /*GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, new float[]{0.3f,0.3f,0.3f,1});
        GL11.glMateriali(GL11.GL_FRONT, GL11.GL_SHININESS, 16);*/

        GL11.glPopMatrix();
    }
    
    private static void sort(Vector<Light> list) {
        sort(list, 0, list.size()-1);
    }
    
    private static void sort(Vector<Light> list, int low, int high) {
        if(low >= high) return; //Завершить выполнение если уже нечего делить
        
        Light base = list.elementAt((low+high)>>1); //Опорный элемент
        float influence = base.influence;

        //Разделить на подмассивы, который больше и меньше опорного элемента
        int first = low, second = high;
        while(first <= second) {
            while(list.elementAt(first).influence > influence) {
                first++;
            }

            while(list.elementAt(second).influence < influence) {
                second--;
            }

            if(first <= second) { //Меняем местами
                Light tmp = list.elementAt(first);
                list.setElementAt(list.elementAt(second), first);
                list.setElementAt(tmp, second);
                first++; second--;
            }
        }
        
        //Сортировки левой и правой части
        if(low < second) sort(list, low, second);
        if(high > first) sort(list, first, high);
    }
    
    public static Light findLight(String find) {
        for(Light light : allLights) {
            if(light.name.equals(find)) return light;
        }
        
        return null;
    }

}
