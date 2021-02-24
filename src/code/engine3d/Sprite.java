package code.engine3d;

import code.math.Vector3D;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class Sprite extends Renderable {
    
    public static final int BOTTOM = 0, CENTER = -1, TOP = -2;
    
    private float[] drawMatrix = new float[16];
    
    public Material mat;
    public float w = 200, h = 200;
    public int align;
    public boolean billboard;
    
    private float xx, yy, zz;
    
    public Sprite(Material mat, boolean billboard, float w, float h, int align) {
        this.mat = mat;
        this.w = w;
        this.h = h;
        this.billboard = billboard;
        this.align = align;
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
        
        tmpMat.scale(w, h, w);
        tmpMat.get(drawMatrix);
        drawMatrix[12] -= w/2;
        if(!billboard) drawMatrix[13] += h*align/2f;
        
        sortZ = 0.5f * drawMatrix[2] + 0.5f * drawMatrix[6] + drawMatrix[14];
    }
    
    public void renderImmediate(E3D e3d) {
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(drawMatrix);
            
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glEnableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glEnableClientState(GL15.GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, e3d.rectCoordVBO);
        GL15.glVertexPointer(3, GL15.GL_SHORT, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, e3d.rectNormals);
        GL15.glNormalPointer(GL15.GL_SHORT, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, e3d.rectuvMVBO);
        GL15.glTexCoordPointer(2, GL15.GL_SHORT, 0, 0);

        bindLight(e3d, xx, yy, zz, 0, 0, 0);
        mat.animate(time);
        mat.bind();
        
        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

        mat.unbind();
        unbindLight();
        
        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glDisableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glDisableClientState(GL15.GL_NORMAL_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL11.glDisable(GL11.GL_NORMALIZE);
    }

}