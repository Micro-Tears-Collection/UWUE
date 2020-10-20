package code.engine3d;

import code.math.Vector3D;
import org.joml.Matrix4f;

/**
 *
 * @author Roman Lahin
 */
public class Renderable {
    
    public static final int PREDRAW = 1, POSTDRAW = 2;
    
    public int drawOrder, orderOffset;

    public void setMatrix(float[] put) {}
    public void setMatrix(Vector3D pos, Vector3D rot, Matrix4f tmp, Matrix4f invCam) {}
    
    public float getZ() {return 0;}
    
    public void prepareRender(E3D e3d) {
        if(drawOrder == PREDRAW) e3d.preDraw.add(this);
        else if(drawOrder == POSTDRAW) e3d.postDraw.add(this);
        else e3d.toRender.add(this);
    }
    
    public void render(E3D e3d) {}
    
}
