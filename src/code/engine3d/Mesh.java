package code.engine3d;

import code.math.Vector3D;

import code.utils.IniFile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class Mesh extends Renderable {
    
    public String name;
    public final float[] modelMatrix = new float[16], drawMatrix = new float[16];

    private Vector3D origMin, origMax;
    public Vector3D min, max;
    public Vector3D middle = new Vector3D();
    
    private int[] vertsID, uvsID, normals, vertsCount;
    private Material[] mats;
    public boolean collision = true, visible = true;
    
    public float[][] physicsVerts;
    public float[][] normalsPerFace;

    public Mesh() {}
    
    public Mesh(int[] verts, int[] uvs, int[] normals, int[] vertex, 
            Material[] mats, Vector3D min, Vector3D max) {
        set(verts, uvs, normals, vertex, mats, min, max);
    }
    
    public void set(int[] verts, int[] uvs, int[] normals, int[] vertex, 
            Material[] mats, Vector3D min, Vector3D max) {
        this.vertsID = verts;
        this.uvsID = uvs;
        this.normals = normals;
        this.vertsCount = vertex;
        this.mats = mats;
        
        this.origMin = new Vector3D(min);
        this.origMax = new Vector3D(max);
        this.min = min;
        this.max = max;
    }
    
    public void load(IniFile ini) {
        super.load(ini);
        
        collision = ini.getInt("collision", 1) == 1;
        visible = ini.getInt("visible", 1) == 1;
    }
    
    public void setPhysics(float[][] xyz) {
        physicsVerts = xyz;
        normalsPerFace = new float[xyz.length][];
        
        for(int i=0; i<xyz.length; i++) {
            float[] verts = xyz[i];
            normalsPerFace[i] = new float[verts.length * 3 / 9];
            float[] nrms = normalsPerFace[i];
            
            for(int t=0; t<verts.length; t+=3*3) {
                float cx = verts[t], cy = verts[t+1], cz = verts[t+2];
                float bx = verts[t+3], by = verts[t+4], bz = verts[t+5];
                float ax = verts[t+6], ay = verts[t+7], az = verts[t+8];
                
                float x = (ay - by) * (az - cz) - (az - bz) * (ay - cy);
                float y = (az - bz) * (ax - cx) - (ax - bx) * (az - cz);
                float z = (ax - bx) * (ay - cy) - (ay - by) * (ax - cx);
                
                double sqrt = Math.sqrt(x * x + y * y + z * z);
                
                nrms[t / 3] = (float) (x / sqrt);
                nrms[t / 3 + 1] = (float) (y / sqrt);
                nrms[t / 3 + 2] = (float) (z / sqrt);
            }
        }
    }
    
    public void fastIdentityCamera(float[] invcam) {
        System.arraycopy(invcam, 0, drawMatrix, 0, 16);
        
        for(int i=0; i<16; i++) modelMatrix[i] = 0;
        modelMatrix[0] = modelMatrix[5] = modelMatrix[10] = modelMatrix[14] = 1;
        
        min.set(origMin); max.set(origMax);
        
        updateZ();
    }
    
    public void setTransformation(Vector3D pos, Vector3D rot) {
        tmpMat.identity();
        buildMatrix(pos, rot, tmpMat);
        tmpMat.get(modelMatrix);
        updateBB(modelMatrix);
    }
    
    public void setCamera(Matrix4f tmp, Matrix4f invCam) {
        tmpMat.set(invCam);
        tmp.set(modelMatrix);
        tmpMat.mul(tmp);
        tmpMat.get(drawMatrix);
        updateZ();
    }
    
    private void updateBB(float[] mat) {
        Vector3D tmp = new Vector3D();
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        
        for(int i=0; i<8; i++) {
            tmp.set(i>=4?origMax.x:origMin.x, 
                    ((i/2)&1)==1?origMax.y:origMin.y, 
                    (i&1)==1?origMax.z:origMin.z);
            tmp.transform(mat);
            
            min.min(tmp);
            max.max(tmp);
        }
    }
    
    private void updateZ() {
        middle.set(origMin);
        middle.add(origMax);
        middle.mul(0.5f, 0.5f, 0.5f);
        middle.transform(drawMatrix);
        
        sortZ = middle.z;
    }
    
    public void render(E3D e3d) {
        if(visible) super.render(e3d);
    }
    
    public void renderImmediate(E3D e3d) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(drawMatrix);
            
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glEnableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glEnableClientState(GL15.GL_NORMAL_ARRAY);
        
        bindLight(e3d, 
                    (min.x+max.x)/2f, (min.y+max.y)/2f, (min.z+max.z)/2f,
                    (max.x-min.x)/2, (max.y-min.y)/2, (max.z-min.z)/2f);

        for(int submesh = 0; submesh < mats.length; submesh++) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertsID[submesh]);
            GL15.glVertexPointer(3, GL15.GL_FLOAT, 0, 0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvsID[submesh]);
            GL15.glTexCoordPointer(2, GL15.GL_FLOAT, 0, 0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normals[submesh]);
            GL15.glNormalPointer(GL15.GL_FLOAT, 0, 0);

            Material mat = mats[submesh];
            
            mat.animate(time);
            mat.bind();
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertsCount[submesh]);
            mat.unbind();
        }
        
        unbindLight();

        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glDisableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glDisableClientState(GL15.GL_NORMAL_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
