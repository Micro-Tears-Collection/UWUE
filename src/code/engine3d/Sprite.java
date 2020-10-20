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
    
    private float[] drawMatrix = new float[16];
    private float z;
    
    public Material mat;
    public float size = 200, sizey = 200;
    public float beginy = -sizey/2;
    public boolean billboard = true;

    public void setMatrix(float[] put) {
        set(null, new Matrix4f(), (new Matrix4f()).set(put));
    }
    
    public void setMatrix(Vector3D pos, Vector3D rot, Matrix4f tmp, Matrix4f invCam) {
        Matrix4f invout = new Matrix4f(invCam);
        tmp.identity();
        set(pos, tmp, invout);
    }
    
    private void set(Vector3D pos, Matrix4f tmp, Matrix4f invCam) {
        if(tmp != null) tmp.setTranslation(pos.x, pos.y, pos.z);
        if(billboard) tmp.translate(0, beginy, 0);
        
        invCam.mul(tmp);
        invCam.set(0, 0, 1); invCam.set(2, 0, 0);
        invCam.set(0, 1, 0); invCam.set(2, 1, 0);
        invCam.set(0, 2, 0); invCam.set(2, 2, 1);
        
        if(!billboard) {
            invCam.set(1, 0, 0);
            invCam.set(1, 1, 1);
            invCam.set(1, 2, 0);
        }
        
        invCam.scale(size, sizey, size);
        invCam.get(drawMatrix);
        drawMatrix[12] -= size/2;
        if(!billboard) drawMatrix[13] += beginy;
        
        z = 0.5f * drawMatrix[2] + 0.5f * drawMatrix[6] + drawMatrix[14];
    }
    
    public float getZ() {
        return z;
    }
    
    public void render(E3D e3d) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(drawMatrix);
            
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glEnableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glActiveTexture(GL15.GL_TEXTURE0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, e3d.rectCoordVBO);
        GL15.glVertexPointer(3, GL15.GL_SHORT, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, e3d.rectuvMVBO);
        GL15.glTexCoordPointer(2, GL15.GL_SHORT, 0, 0);

        mat.bind();
        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glDisableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

}
