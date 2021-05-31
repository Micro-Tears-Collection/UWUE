package code.engine3d.Lighting;

import code.engine3d.E3D;
import java.nio.FloatBuffer;
import java.util.Vector;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class LightGroup {
    public static Vector<Light> allLights = new Vector();
    public static Vector<LightGroup> lightgroups = new Vector();
    public static LightGroup defaultGroup;
    
    private static Vector<Light> renderLights = new Vector();
    private static int activeLightsCount = 0;
    
    public String name;
    public Vector<Light> lights;
    private FloatBuffer ambient;
    
    public static void clear(boolean recreate) {
        for(LightGroup group : lightgroups) {
            group.destroy();
        }
        lightgroups.removeAllElements();
        defaultGroup = null;
        
        for(Light light : allLights) {
            light.destroy();
        }
        allLights.removeAllElements();
        
        if(recreate) {
            defaultGroup = new LightGroup("default");
            lightgroups.add(defaultGroup);
        }
    }
    
    public LightGroup(String name) {
        this.name = name;
        lights = new Vector();
        
        ambient = MemoryUtil.memAllocFloat(4);
        ambient.put(new float[] {0, 0, 0, 1});
        ambient.rewind();
    }
    
    public void destroy() {
        MemoryUtil.memFree(ambient);
        ambient = null;
    }
    
    public void setAmbient(float[] ambient) {
        float[] arr = new float[] {0, 0, 0, 1};
        if(ambient.length == 1) {
            arr[0] = ambient[0]/255f;
            arr[0] = ambient[0]/255f;
            arr[0] = ambient[0]/255f;
        } else if(ambient.length == 3) {
            arr[0] = ambient[0]/255f;
            arr[1] = ambient[1]/255f;
            arr[2] = ambient[2]/255f;
        }
        
        this.ambient.clear();
        this.ambient.put(arr);
        this.ambient.rewind();
    }
    
    public void bind(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
        /*GL33C.glMatrixMode(GL33C.GL_MODELVIEW);
        GL33C.glPushMatrix();
        GL33C.glLoadMatrixf(e3d.invCamf);
        
        GL33C.glEnable(GL33C.GL_LIGHTING);*/
            
        activeLightsCount = 0;
        for(Light light : lights) {
            if(light.point) {
                //point source
                //calculate distance square
                float d = Math.max(Math.abs(x - light.posOrDir.get(0)) - xs, 0);
                d *= d;
                float t = Math.max(Math.abs(y - light.posOrDir.get(1)) - ys, 0);
                d += t*t;
                t = Math.max(Math.abs(z - light.posOrDir.get(2)) - zs, 0);
                d += t*t;
                
                if(d != 0) light.influence = light.absLit * 10000 * 10 / d;
                else light.influence = Float.MAX_VALUE;
                
                if(light.influence <= 1f/255f) continue;
            } else {
                //directional source
                light.influence = Float.MAX_VALUE;
            }
            
            renderLights.add(light);
            activeLightsCount++;
        }
        
        //if(renderLights.size() > e3d.maxLights) sort(renderLights);
        
        //activeLightsCount = Math.min(activeLightsCount, e3d.maxLights);
        
        for(int ii=0; ii<activeLightsCount; ii++) {
            Light light = renderLights.elementAt(ii);
            
            //GL33C.glLightfv(GL33C.GL_LIGHT0+ii, GL33C.GL_DIFFUSE, light.color);
            ////GL33C.glLightfv(GL33C.GL_LIGHT0+ii, GL33C.GL_SPECULAR, light.color);
            //GL33C.glLightfv(GL33C.GL_LIGHT0+ii, GL33C.GL_POSITION, light.posOrDir);
            
            /*if(light.spotDir != null) {
                GL33C.glLightf(GL33C.GL_LIGHT0 + ii, GL33C.GL_SPOT_CUTOFF, light.cutoff);
                GL33C.glLightfv(GL33C.GL_LIGHT0 + ii, GL33C.GL_SPOT_DIRECTION, light.spotDir);
            } else {
                GL33C.glLighti(GL33C.GL_LIGHT0 + ii, GL33C.GL_SPOT_CUTOFF, 180);
            }*/
            
            //GL33C.glEnable(GL33C.GL_LIGHT0+ii);
        }
        renderLights.removeAllElements();
        
        /*GL33C.glMaterialfv(GL33C.GL_FRONT, GL33C.GL_AMBIENT, ambient);

        GL33C.glPopMatrix();*/
    }
    
    public void unbind() {
        //GL33C.glDisable(GL33C.GL_LIGHTING);
        
        for(int i=0; i<activeLightsCount; i++) {
            //GL33C.glDisable(GL33C.GL_LIGHT0+i);
        }
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
            while(list.elementAt(first).influence > influence) first++;

            while(list.elementAt(second).influence < influence) second--;

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
            if(find.equals(light.name)) return light;
        }
        
        return null;
    }

}
