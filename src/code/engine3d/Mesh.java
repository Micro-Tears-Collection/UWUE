package code.engine3d;

import code.math.Vector3D;
import code.utils.IniFile;

import code.utils.assetManager.ReusableContent;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class Mesh extends ReusableContent {
    
    public String name;
    public IniFile ini;

    public Vector3D min, max;
    
    private int[] vaos, vbos, vertsCount;
    public Material[] mats;
    
    public float[][] physicsVerts;
    public float[][] normalsPerFace;

    public Mesh() {}
    
	//todo create vaos vbos here, not in model loader
    public Mesh(int[] vaos, int[] vbos, int[] vertsCount, 
            Material[] mats, Vector3D min, Vector3D max) {
        set(vaos, vbos, vertsCount, mats, min, max);
    }
    
    public void set(int[] vaos, int[] vbos, int[] vertsCount, 
            Material[] mats, Vector3D min, Vector3D max) {
        this.vaos = vaos;
        this.vbos = vbos;
        this.vertsCount = vertsCount;
        this.mats = mats;
        
        this.min = new Vector3D(min);
        this.max = new Vector3D(max);
    }
    
    public ReusableContent use() {
        if(using == 0) {
            for(Material mat : mats) mat.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1) {
            for(Material mat : mats) mat.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        for(int i = 0; i < vaos.length; i++) {
            GL33C.glDeleteVertexArrays(vaos[i]);
        }
        
        for(int i = 0; i < vbos.length; i++) {
            GL33C.glDeleteBuffers(vbos[i]);
        }
    }
    
    public void setPhysics(float[][] xyz) {
        physicsVerts = xyz;
        normalsPerFace = new float[xyz.length][];
        
        for(int i=0; i<xyz.length; i++) {
            float[] verts = xyz[i];
            normalsPerFace[i] = new float[verts.length * 3 / 9];
            float[] nrms = normalsPerFace[i];
            
            for(int t=0; t<verts.length; t+=3*3) {
                float ax = verts[t], ay = verts[t+1], az = verts[t+2];
                float bx = verts[t+3], by = verts[t+4], bz = verts[t+5];
                float cx = verts[t+6], cy = verts[t+7], cz = verts[t+8];
                
                float x = (by - ay) * (cz - az) - (bz - az) * (cy - ay);
                float y = (bz - az) * (cx - ax) - (bx - ax) * (cz - az);
                float z = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
                
                double sqrt = Math.sqrt(x * x + y * y + z * z);
                
                nrms[t / 3 + 0] = (float) (x / sqrt);
                nrms[t / 3 + 1] = (float) (y / sqrt);
                nrms[t / 3 + 2] = (float) (z / sqrt);
            }
        }
    }
    
    public void renderImmediate(E3D e3d, long time, FloatBuffer modelView) {
        renderImmediate(e3d, mats, time, modelView);
    }
    
	//render with custom materials
    public void renderImmediate(E3D e3d, Material[] mats, long time, FloatBuffer modelView) {
        e3d.setModelView(modelView);
        
        for(int submesh = 0; submesh < mats.length; submesh++) {
            Material mat = mats[submesh];
            mat.bind(e3d, time);
            
            GL33C.glBindVertexArray(vaos[submesh]);
            GL33C.glDrawArrays(GL33C.GL_TRIANGLES, 0, vertsCount[submesh]);
            
            mat.unbind();
        }
        
        GL33C.glBindVertexArray(0);
    }
}
