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
	
	private static final int VBOS_PER_VAO = 3,
			INTS_PER_VBO = 3 + 2 + 1;// + 1;
    
    public String name;
    public IniFile ini;

    public Vector3D min, max;
    
    private int[] vaos, vbos, vertsCount;
	public String[] mats;
    
    public float[][] poses, uvs, normals;//, tangents;
    public float[][] normalsPerFace;
    
    public Mesh(String name, float[][] poses, float[][] uvs, float[][] normals,
			String[] mats, Vector3D min, Vector3D max) {
		//Init
		this.poses = poses;
		this.uvs = uvs;
		this.normals = normals;
		
		this.mats = mats;
		this.min = new Vector3D(min);
		this.max = new Vector3D(max);
		
		calcFaceNormals();
		//if(uvs != null) calcTangents();
		
		//Load to GPU
		int submeshes = mats.length;
		
		vaos = new int[submeshes];
		vbos = new int[submeshes];
		vertsCount = new int[submeshes];
		
		for(int i=0; i<submeshes; i++) {
			int vao = GL33C.glGenVertexArrays();
			GL33C.glBindVertexArray(vao);
			vaos[i] = vao;
			vertsCount[i] = poses[i].length / 3;
			
			int[] data = new int[poses[i].length / 3 * INTS_PER_VBO];
			for(int x=0; x<data.length; x+=INTS_PER_VBO) {
				int vert = x / INTS_PER_VBO;
				
				//Pos
				data[x] = Float.floatToRawIntBits(poses[i][vert * 3]);
				data[x + 1] = Float.floatToRawIntBits(poses[i][vert * 3 + 1]);
				data[x + 2] = Float.floatToRawIntBits(poses[i][vert * 3 + 2]);
				
				if(uvs != null) {
					data[x + 3] = Float.floatToRawIntBits(uvs[i][vert * 2]);
					data[x + 4] = Float.floatToRawIntBits(uvs[i][vert * 2 + 1]);
				}
				
				if(normals != null) {
					data[x + 5] = pack2101010(
							normals[i][vert * 3], 
							normals[i][vert * 3 + 1], 
							normals[i][vert * 3 + 2], 
							0);
				}
				
				/*if(tangents != null) {
					data[x + 6] = pack2101010(
							tangents[i][vert * 4], 
							tangents[i][vert * 4 + 1], 
							tangents[i][vert * 4 + 2],
							tangents[i][vert * 4 + 3]);
				}*/
			}
			
			int vbo = GL33C.glGenBuffers(); //Creates a VBO ID
			vbos[i] = vbo;
			
			GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
			GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, data, GL33C.GL_STATIC_DRAW);
			
			GL33C.glEnableVertexAttribArray(0); //pos
			GL33C.glVertexAttribPointer(0, 3, GL33C.GL_FLOAT, false, INTS_PER_VBO * 4, 0);
			
			if(uvs != null) {
				GL33C.glEnableVertexAttribArray(1); //uvs
				GL33C.glVertexAttribPointer(1, 2, GL33C.GL_FLOAT, false, INTS_PER_VBO * 4, 3 * 4);
			}
			
			if(normals != null) {
				GL33C.glEnableVertexAttribArray(2); //norms
				GL33C.glVertexAttribPointer(2, 4, GL33C.GL_INT_2_10_10_10_REV, true, INTS_PER_VBO * 4, 5 * 4);
			}
			
			/*if(tangents != null) {
				GL33C.glEnableVertexAttribArray(3); //tangents
				GL33C.glVertexAttribPointer(3, 4, GL33C.GL_INT_2_10_10_10_REV, true, INTS_PER_VBO * 4, 6 * 4);
			}*/
		}
		
		GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
		GL33C.glBindVertexArray(0);
    }

	private int pack2101010(float x, float y, float z, float w) {
		int xx = Math.min(Math.max(Math.round(x * 511), -511), 511) & 0b1111111111;
		int yy = Math.min(Math.max(Math.round(y * 511), -511), 511) & 0b1111111111;
		int zz = Math.min(Math.max(Math.round(z * 511), -511), 511) & 0b1111111111;
		int ww = Math.min(Math.max(Math.round(w), -1), 1) & 0b11;
		
		return xx | (yy << 10) | (zz << 20) | (ww << 30);
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
			
			float[] subTangents = new float[poses[submesh].length * 4 / 3];
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
				
				Vector3D normal = new Vector3D(
						normalsPerFace[submesh][i / 3],
						normalsPerFace[submesh][i / 3 + 1],
						normalsPerFace[submesh][i / 3 + 2]);
				
				float dirCorrection = (deltaUV2.x * deltaUV1.y - deltaUV2.y * deltaUV1.x) < 0 ? -1 : 1;
				//if(deltaUV1.x * deltaUV2.y == deltaUV1.y * deltaUV2.x) {
				//	deltaUV1.set(0, 1, 0);
				//	deltaUV2.set(1, 0, 0);
				//}
				
				Vector3D tangent = new Vector3D(
					deltaUV1.y * edge2.x - deltaUV2.y * edge1.x,
					deltaUV1.y * edge2.y - deltaUV2.y * edge1.y,
					deltaUV1.y * edge2.z - deltaUV2.y * edge1.z);
				tangent.mul(dirCorrection, dirCorrection, dirCorrection);
				tangent.setLength(1);
				
				Vector3D bitangent = new Vector3D(
					deltaUV1.x * edge2.x - deltaUV2.x * edge1.x,
					deltaUV1.x * edge2.y - deltaUV2.x * edge1.y,
					deltaUV1.x * edge2.z - deltaUV2.x * edge1.z);
				bitangent.mul(dirCorrection, dirCorrection, dirCorrection);
				bitangent.setLength(1);
				
				boolean invalidTangent = !Float.isFinite(tangent.x) || 
						!Float.isFinite(tangent.y) || 
						!Float.isFinite(tangent.z);
				boolean invalidBitangent = !Float.isFinite(bitangent.x) || 
						!Float.isFinite(bitangent.y) || 
						!Float.isFinite(bitangent.z);
				
				if(invalidTangent != invalidBitangent) {
					if(invalidTangent) {
						tangent = Vector3D.cross(normal, bitangent);
						tangent.setLength(1);
					} else {
						bitangent = Vector3D.cross(normal, tangent);
						bitangent.setLength(1);
					}
				}
				
				Vector3D vn = Vector3D.cross(normal, tangent);
				tangent = Vector3D.cross(vn, normal);
				
				int tanpos = i / 3 * 4;
				subTangents[tanpos] = subTangents[tanpos + 4] = subTangents[tanpos + 8] = tangent.x;
				subTangents[tanpos + 1] = subTangents[tanpos + 4 + 1] = subTangents[tanpos + 8 + 1] = tangent.y;
				subTangents[tanpos + 2] = subTangents[tanpos + 4 + 2] = subTangents[tanpos + 8 + 2] = tangent.z;
				
				subTangents[tanpos + 3] = subTangents[tanpos + 4 + 3] = subTangents[tanpos + 8 + 3]
						= Vector3D.cross(normal, tangent).dot(bitangent) < 0 ? -1 : 1;
			}
		}
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
