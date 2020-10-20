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
    public float[] drawMatrix = new float[16];

    public Vector3D min, max;
    public Vector3D middle = new Vector3D();
    public int[] vertsID, uvsID, normals, vertsCount;
    public Material[] mats;
    
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
        
        this.min = min;
        this.max = max;
    }
    
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
    
    public void setMatrix(float[] put) {
        System.arraycopy(put, 0, drawMatrix, 0, 16);
        updateZ();
    }
    
    public void setMatrix(Vector3D pos, Vector3D rot, Matrix4f tmp, Matrix4f invCam) {
        tmp.identity();
        tmp.rotateY((float) Math.toRadians(rot.x));
        tmp.rotateX((float) Math.toRadians(rot.y));
        tmp.rotateX((float) Math.toRadians(rot.z));
        tmp.setTranslation(pos.x, pos.y, pos.z);
        
        tmp.get(drawMatrix);
        updateZ();
    }
    
    private void updateZ() {
        middle.set(min);
        middle.add(max.x, max.y, max.z);
        middle.mul(0.5f, 0.5f, 0.5f);
        middle.transform(drawMatrix);
    }
    
    public float getZ() {
        return middle.z;
    }
    
    public void render(E3D e3d) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(drawMatrix);
            
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glEnableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glEnableClientState(GL15.GL_NORMAL_ARRAY);
        GL15.glActiveTexture(GL15.GL_TEXTURE0);

        for(int submesh = 0; submesh < mats.length; submesh++) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertsID[submesh]);
            GL15.glVertexPointer(3, GL15.GL_FLOAT, 0, 0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvsID[submesh]);
            GL15.glTexCoordPointer(2, GL15.GL_FLOAT, 0, 0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normals[submesh]);
            GL15.glNormalPointer(GL15.GL_FLOAT, 0, 0);

            mats[submesh].bind();
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertsCount[submesh]);
        }

        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glDisableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glDisableClientState(GL15.GL_NORMAL_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
