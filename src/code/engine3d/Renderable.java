package code.engine3d;

import code.engine3d.Lighting.LightGroup;
import code.math.Vector3D;
import code.utils.IniFile;
import org.joml.Matrix4f;

/**
 *
 * @author Roman Lahin
 */
public class Renderable {
    
    public static final int NORMALDRAW = 1, PREDRAW = 0, POSTDRAW = 2;
    
    public int drawOrder = NORMALDRAW, orderOffset;
    public float sortZ;
    
    public String lightGroupName = null;
    public LightGroup lightGroup = null;
    
    public long time;
    
    public void load(IniFile ini) {
        String tmp = ini.get("order");
        
        if(tmp != null) {
            if(tmp.startsWith("pre")) {
                drawOrder = PREDRAW;
                if(tmp.length() > 3) orderOffset = Integer.valueOf(tmp.substring(3));
            } else if(tmp.startsWith("post")) {
                drawOrder = POSTDRAW;
                if(tmp.length() > 4) orderOffset = Integer.valueOf(tmp.substring(4));
            } else orderOffset = Integer.valueOf(tmp);
        }
        
        tmp = ini.getDef("lightgroup", "default");
        if(!tmp.equals("0")) lightGroupName = tmp;
    }

    public void setTransformation(Vector3D pos, Vector3D rot) {}
    public void setCamera(Matrix4f tmp, Matrix4f invCam) {}
    
    public void animate(long time, boolean set) {
        if(set) this.time = time;
        else this.time += time;
    }
    
    public void prepareRender(E3D e3d) {
        e3d.add(this);
    }
    
    public void render(E3D e3d) {}
    
    public void bindLight(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
        if(!LightGroup.lightgroups.isEmpty() && lightGroupName != null) {
            
            if(lightGroup == null) {
                for(LightGroup lightGroup2 : LightGroup.lightgroups) {
                    if(lightGroup2.name.equals(lightGroupName)) {
                        lightGroup = lightGroup2;
                        break;
                    }
                }

                if(lightGroup == null) {
                    lightGroupName = null;
                    return;
                }
            }
            
            lightGroup.bind(e3d, x, y, z, xs, ys, zs);
        }
    }
    
    public void unbindLight() {
        if(lightGroup != null) lightGroup.unbind();
    }
    
    public static final Matrix4f tmpMat = new Matrix4f();
    
    public static Matrix4f buildMatrix(Vector3D pos, Vector3D rot) {
        return buildMatrix(pos, rot, new Matrix4f());
    }
    
    public static Matrix4f buildMatrix(Vector3D pos, Vector3D rot, Matrix4f tmp) {
        if(rot != null) {
            tmp.rotateX((float) Math.toRadians(rot.x));
            tmp.rotateY((float) Math.toRadians(rot.y));
            tmp.rotateZ((float) Math.toRadians(rot.z));
        }
        tmp.setTranslation(pos.x, pos.y, pos.z);
        
        return tmp;
    }
}
