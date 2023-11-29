package code.engine3d.instancing;

import code.engine3d.E3D;
import code.engine3d.game.lighting.LightsData;
import code.math.Vector3D;
import code.utils.IniFile;
import org.joml.Matrix4f;

/**
 *
 * @author Roman Lahin
 */
public class RenderInstance {
    
    public static final int NORMALDRAW = Integer.MAX_VALUE;
    
    public int drawOrder = NORMALDRAW;
    public float sortZ;
    
    public String lightGroupName = null;
    public LightsData lightGroup = null;
    
    public long time;
    
    public void load(IniFile ini) {
        String tmp = ini.get("order");
        
        if(tmp != null) {
            if(tmp.startsWith("post")) {
                if(tmp.length() > 4) drawOrder = Integer.valueOf(tmp.substring(4));
                else drawOrder = 0;
            }
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
    
    public void render(E3D e3d) {
        if(drawOrder == NORMALDRAW) renderImmediate(e3d);
        else e3d.add(this);
    }
    
    public void renderImmediate(E3D e3d) {}
    
    public void bindLight(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
		e3d.lightsData.bind(e3d, x, y, z, xs, ys, zs);
    }
    
    public void unbindLight(E3D e3d) {
        if(lightGroup != null) lightGroup.unbind(e3d);
    }
    
    public static final Matrix4f tmpMat = new Matrix4f();
	public static final float[] tmpMatf = new float[16];
    
    public static Matrix4f buildMatrix(Vector3D pos, Vector3D rot) {
        return buildMatrix(pos, rot, new Matrix4f());
    }
    
    public static Matrix4f buildMatrix(Vector3D pos, Vector3D rot, Matrix4f tmp) {
        if(rot != null) {
            tmp.rotateZ((float) Math.toRadians(rot.z));
            tmp.rotateX((float) Math.toRadians(rot.x));
            tmp.rotateY((float) Math.toRadians(rot.y));
        }
        tmp.setTranslation(pos.x, pos.y, pos.z);
        
        return tmp;
    }
}
