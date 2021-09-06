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
	
	private static final int VBOS_PER_VAO = 3;
    
    public String name;
    public IniFile ini;

    public Vector3D min, max;
    
    private int[] vaos, vbos, vertsCount;
	public String[] mats;
    
    public float[][] poses, uvs, normals;//, tangents;
    public float[][] normalsPerFace;
    
	//todo create vaos vbos here, not in model loader
    public Mesh(String name, float[][] poses, float[][] uvs, float[][] normals,
			String[] mats, Vector3D min, Vector3D max) {
		//Init
		this.poses = poses;
		this.uvs = uvs;
		this.normals = normals;
		
		this.mats = mats;
		this.min = new Vector3D(min);
		this.max = new Vector3D(max);
		
		if(poses != null) calcFaceNormals();
		
		//Load to GPU
		int submeshes = mats.length;
		
		vaos = new int[submeshes];
		vbos = new int[submeshes * VBOS_PER_VAO];
		vertsCount = new int[submeshes];
		
		for(int i=0; i<submeshes; i++) {
			int vao = GL33C.glGenVertexArrays();
			GL33C.glBindVertexArray(vao);
			vaos[i] = vao;
			
			int vbo;
			
			//Poses
			if(poses != null) {
				vbo = GL33C.glGenBuffers(); //Creates a VBO ID
				vbos[i * VBOS_PER_VAO] = vbo;
				
				GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
				GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, poses[i], GL33C.GL_STATIC_DRAW);
				
				GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, 0, 0);
				GL33C.glEnableVertexAttribArray(0); //pos
				
				vertsCount[i] = poses[i].length / 3;
			}
			
			//Uvs
			if(uvs != null) {
				vbo = GL33C.glGenBuffers(); //Creates a VBO ID
				vbos[i * VBOS_PER_VAO + 1] = vbo;
				
				GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
				GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, uvs[i], GL33C.GL_STATIC_DRAW);
				
				GL33C.glVertexAttribPointer(1, 2, GL33C.GL_FLOAT, false, 0, 0);
				GL33C.glEnableVertexAttribArray(1); //uvs
			}
			
			//Normals
			if(normals != null) {
				vbo = GL33C.glGenBuffers(); //Creates a VBO ID
				vbos[i * VBOS_PER_VAO + 2] = vbo;
				
				GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
				GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, normals[i], GL33C.GL_STATIC_DRAW);
				
				GL33C.glVertexAttribPointer(2, 3, GL33C.GL_FLOAT, false, 0, 0);
				GL33C.glEnableVertexAttribArray(2); //normals
			}
		}
		
		GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
		GL33C.glBindVertexArray(0);
		
		//if(poses != null && uvs != null) calcTangents();
    }
    
    public void destroy() {
        for(int i = 0; i < vaos.length; i++) {
            GL33C.glDeleteVertexArrays(vaos[i]);
        }
        
        for(int i = 0; i < vbos.length; i++) {
            if(vbos[i] != 0) GL33C.glDeleteBuffers(vbos[i]);
        }
    }
    
    private void calcFaceNormals() {
        normalsPerFace = new float[poses.length][];
        
        for(int i=0; i<poses.length; i++) {
            float[] verts = poses[i];
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

	/*private void calcTangents() {
		tangents = new float[poses.length][];
		
		for(int submesh=0; submesh<poses.length; submesh++) {
			float[] subPos = poses[submesh];
			float[] subUvs = uvs[submesh];
			
			float[] subTangents = new float[poses[submesh].length];
			tangents[submesh] = subTangents;
			
			for(int i=0; i<subPos.length; i+=9) {
				Vector3D edge1 = new Vector3D(subPos[i + 3], subPos[i + 3 + 1], subPos[i + 3 + 2]);
				edge1.sub(subPos[i], subPos[i + 1], subPos[i + 2]);
				
				Vector3D edge2 = new Vector3D(subPos[i + 6], subPos[i + 6 + 1], subPos[i + 6 + 2]);
				edge2.sub(subPos[i], subPos[i + 1], subPos[i + 2]);
				
				int uvi = i * 2 / 3;
				Vector3D deltaUV1 = new Vector3D(subUvs[uvi + 2], subUvs[uvi + 2 + 1], 0);
				deltaUV1.sub(subUvs[uvi], subUvs[uvi + 1], 0);
				
				Vector3D deltaUV2 = new Vector3D(subUvs[uvi + 4], subUvs[uvi + 4 + 1], 0);
				deltaUV2.sub(subUvs[uvi], subUvs[uvi + 1], 0);

				float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
				
				subTangents[i] = subTangents[i + 3] = subTangents[i + 6] =
						f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
				
				subTangents[i + 1] = subTangents[i + 3 + 1] = subTangents[i + 6 + 1] =
						f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
				
				subTangents[i + 2] = subTangents[i + 3 + 2] = subTangents[i + 6 + 2] =
						f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
			}
			
			int vbo = GL33C.glGenBuffers(); //Creates a VBO ID
			vbos[submesh * VBOS_PER_VAO + 3] = vbo;

			GL33C.glBindVertexArray(vaos[submesh]);
			GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
			GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, subTangents, GL33C.GL_STATIC_DRAW);
			
			GL33C.glVertexAttribPointer(3, 3, GL33C.GL_FLOAT, false, 0, 0);
			GL33C.glEnableVertexAttribArray(3); //tangents
		}
		
		GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
		GL33C.glBindVertexArray(0);
	}*/
    
	//render only with custom materials
    public void renderImmediate(E3D e3d, Material[] mats, long time, FloatBuffer modelView) {
        e3d.setModelView(modelView);
        
        for(int submesh = 0; submesh < mats.length; submesh++) {
            Material mat = mats[submesh];
            mat.bind(e3d, time);
            
            GL33C.glBindVertexArray(vaos[submesh]);
            GL33C.glDrawArrays(GL33C.GL_TRIANGLES, 0, vertsCount[submesh]);
            
            mat.unbind(e3d);
        }
        
        GL33C.glBindVertexArray(0);
    }
}
