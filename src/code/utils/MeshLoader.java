package code.utils;

import code.Engine;
import code.engine3d.Material;
import code.engine3d.Mesh;
import code.math.Vector3D;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class MeshLoader {
    
    private static int storeData(float[] data) {
        int vbo = GL15.glGenBuffers(); //Creates a VBO ID
        Asset.vbos.add(vbo);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
        
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
        
        return vbo;
    }
    
    public static void createMesh(Mesh mesh, float[][] positions, float[][] UVs, float[][] normals,
            Material[] tex, 
            Vector3D min, Vector3D max) {
        int[] vout = new int[tex.length];
        int[] uvout = new int[tex.length];
        int[] normalsout = new int[tex.length];
        int[] lensout = new int[tex.length];
        
        for(int i = 0; i < tex.length; i++) {
            vout[i] = storeData(positions[i]);
            uvout[i] = storeData(UVs[i]);
            normalsout[i] = storeData(normals[i]);
            lensout[i] = UVs[i].length >> 1;
        }

        mesh.set(vout, uvout, normalsout, lensout, tex, min, max);
    }
    
    public static Mesh[] loadObj(String name) {
        return loadObj(name, false, null, null);
    }
    
    public static Mesh[] loadObj(String name, boolean createPhysics, String prefix, String postfix) {
        //long begin = System.currentTimeMillis();
        String[] replaceRaw = StringTools.cutOnStrings(name, '|');
        Hashtable<String, String> replace = new Hashtable();
        for(int i=1; i<replaceRaw.length; i+=2) {
            replace.put(replaceRaw[i], replaceRaw[i+1]);
        }
        
        File file = new File("data", replaceRaw[0]);
        String line = null;
        
        try {
            if(!file.exists()) throw new Exception("Can't find model "+file.getAbsolutePath());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String str = new String(data, "UTF-8");
            String[] fileLines = StringTools.cutOnStrings(str, '\n');
            
            Vector<float[]> verts = new Vector();
            Vector<float[]> normalsV = new Vector();
            Vector<float[]> uvs = new Vector();
            
            Vector<Mesh> meshes = new Vector();
            Mesh currentMesh = null;
            
            LinkedHashMap<String, Vector<Face>> materials = new LinkedHashMap();
            Vector<Face> faces = null;
            String texName = null;
            
            Vector3D max = new Vector3D(), min = new Vector3D();
            
            for(int lineNum = 0; lineNum <= fileLines.length; lineNum++) {
                line = (lineNum < fileLines.length) ? fileLines[lineNum].trim() : "end";
                if(line.isEmpty()) continue;
                
                if(line.startsWith("f ")) {
                    String[] faceVerts = StringTools.cutOnStrings(line.substring(2), ' ');
                    int[][] faceVertsData = new int[faceVerts.length][];
                    
                    for(int i=0; i<faceVerts.length; i++) {
                        faceVertsData[i] = StringTools.cutOnInts(faceVerts[i], '/');
                    }
                    
                    boolean hasUV = false, hasNorm = false;
                    if(faceVertsData[0].length >= 2) hasUV = true;
                    if(faceVertsData[0].length >= 3) hasNorm = true;
                    
                    for(int i=0; i<faceVerts.length-2; i++) {
                        Face face = new Face();
                        face.pos = new int[] {faceVertsData[0][0], faceVertsData[i+1][0], faceVertsData[i+2][0]};
                        
                        if(hasUV)
                            face.uv = new int[] {faceVertsData[0][1], faceVertsData[i+1][1], faceVertsData[i+2][1]};
                        
                        if(hasNorm)
                            face.normals = new int[] {faceVertsData[0][2], faceVertsData[i+1][2], faceVertsData[i+2][2]};
                        
                        faces.add(face);
                    }
                } else if(line.startsWith("usemtl ")) {
                    String matName = line.substring(7);
                    
                    if(!matName.equals(texName)) {
                        texName = matName;
                        
                        faces = materials.get(texName);
                        if(faces == null) {
                            faces = new Vector();
                            materials.put(texName, faces);
                        }
                    }
                } else {
                    
                    boolean newMesh = line.startsWith("o ") || line.startsWith("g ");
                    
                    if((newMesh || line.equals("end")) && currentMesh != null) {
                        if(materials.get("null").isEmpty()) materials.remove("null");
                        
                        Set keys = materials.keySet();
                        String[] keysArr = new String[keys.size()];
                        keys.toArray(keysArr);
                        
                        Material[] texs = new Material[keys.size()];
                        
                        float[][] poses = new float[keysArr.length][];
                        float[][] uvm = new float[keysArr.length][];
                        float[][] normals = new float[keysArr.length][];
                        
                        for(int i=0; i<keysArr.length; i++) {
                            texs[i] = Asset.getMaterial(keysArr[i], replace, prefix, postfix);
                            Vector<Face> meshFaces = materials.get(keysArr[i]);
                            
                            poses[i] = new float[meshFaces.size() * 3 * 3];
                            uvm[i] = new float[meshFaces.size() * 3 * 2];
                            normals[i] = new float[meshFaces.size() * 3 * 3];
                            int pp = 0, uvp = 0, np = 0;
                            for(int x=0; x<meshFaces.size(); x++) {
                                Face face = meshFaces.elementAt(x);
                                
                                for(int y=0; y<3; y++) {
                                    float[] vert = verts.elementAt(face.pos[y] - 1);
                                    System.arraycopy(vert, 0, poses[i], pp, 3);
                                    pp += 3;
                                    
                                    if(vert[0] > max.x) max.x = vert[0];
                                    if(vert[0] < min.x) min.x = vert[0];
                                    
                                    if(vert[1] > max.y) max.y = vert[1];
                                    if(vert[1] < min.y) min.y = vert[1];
                                    
                                    if(vert[2] > max.z) max.z = vert[2];
                                    if(vert[2] < min.z) min.z = vert[2];
                                    
                                    if(face.uv != null) {
                                        vert = uvs.elementAt(face.uv[y] - 1);
                                        uvm[i][uvp] = vert[0];
                                        uvm[i][uvp+1] = 1f-vert[1];
                                        uvp += 2;
                                    }
                                    
                                    if(face.normals != null) {
                                        vert = normalsV.elementAt(face.normals[y] - 1);
                                        System.arraycopy(vert, 0, normals[i], np, 3);
                                        np += 3;
                                    }
                                }
                            }
                        }
                        
                        createMesh(currentMesh, poses, uvm, normals, texs, new Vector3D(min), new Vector3D(max));
                        if(createPhysics) currentMesh.setPhysics(poses);
                        currentMesh = null;
                    }
                    
                    if(newMesh) {
                        currentMesh = new Mesh();
                        
                        String[] lines = StringTools.cutOnStrings(line.substring(2),';');
                        currentMesh.name = lines[0];
                        currentMesh.load(new IniFile(lines, false));
                        
                        meshes.add(currentMesh);
                        
                        faces = new Vector();
                        texName = "null";
                        materials.clear();
                        materials.put(texName, faces);
                        
                        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
                    } else if(line.startsWith("v ")) {
                        verts.add(StringTools.cutOnFloats(line.substring(2), ' '));
                    } else if(line.startsWith("vn ")) {
                        normalsV.add(StringTools.cutOnFloats(line.substring(3), ' '));
                    } else if(line.startsWith("vt ")) {
                        uvs.add(StringTools.cutOnFloats(line.substring(3), ' '));
                    }
                }
            }
            
            //System.out.println(name+" "+(System.currentTimeMillis()-begin));
            return meshes.toArray(new Mesh[meshes.size()]);
        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        }
        
        return null;
    }

}

class Face {
    public int[] pos;
    public int[] uv;
    public int[] normals;
}
