package code.engine3d.instancing;

import code.engine3d.E3D;
import code.engine3d.Material;
import code.math.Vector3D;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Sprite extends RenderInstance {
    
    public static final int BOTTOM = 0, CENTER = -1, TOP = -2;
    
    private FloatBuffer drawMatrix;
    
    public Material mat;
    public float w = 200, h = 200;
    public int align;
    public boolean billboard;
    
    private float xx, yy, zz;
    
    public Sprite(Material mat, boolean billboard, float w, float h, int align) {
        this.mat = mat;
        this.mat.use();
        this.w = w;
        this.h = h;
        this.billboard = billboard;
        this.align = align;
        
        drawMatrix = MemoryUtil.memAllocFloat(16);
    }
    
    public void destroy() {
        mat.free();
        mat = null;
        MemoryUtil.memFree(drawMatrix);
        drawMatrix = null;
    }

    public void setTransformation(Vector3D pos, Vector3D rot) {
        xx = pos.x;
        yy = pos.y;
        zz = pos.z;
    }
    
    public void setCamera(Matrix4f tmp, Matrix4f invCam) {
        tmp.identity();
        tmp.setTranslation(xx, yy+(billboard?h*align/2f:0), zz);
        
        tmpMat.set(invCam);
        tmpMat.mul(tmp);
        tmpMat.set(0, 0, 1); tmpMat.set(2, 0, 0);
        tmpMat.set(0, 1, 0); tmpMat.set(2, 1, 0);
        tmpMat.set(0, 2, 0); tmpMat.set(2, 2, 1);
        
        if(!billboard) {
            tmpMat.set(1, 0, 0);
            tmpMat.set(1, 1, 1);
            tmpMat.set(1, 2, 0);
        }
        
        tmpMat.translate(-w/2, billboard?0:h*align/2, 0);
        tmpMat.scale(w, h, 1);
        tmpMat.get(drawMatrix);
        
        sortZ = 0.5f * drawMatrix.get(2) + 0.5f * drawMatrix.get(6) + drawMatrix.get(14);
    }
    
    public void renderImmediate(E3D e3d) {
        mat.bind(e3d, time);
        //todo same thing with light as in meshinstance
        bindLight(e3d, xx, yy, zz, 0, 0, 0);
        
        e3d.setModelView(drawMatrix);
        
        GL33C.glBindVertexArray(e3d.spriteVAO);
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
        GL33C.glBindVertexArray(0);

        unbindLight(e3d);
        mat.unbind(e3d);
    }

}