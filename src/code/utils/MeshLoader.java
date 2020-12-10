package code.utils;

import code.Engine;
import code.engine3d.Material;
import code.engine3d.Mesh;
import code.math.Vector3D;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Scanner;
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

    /*public static Mesh createMesh(float[] positions, float[] UVs, Material tex) {
        int verts = storeData(positions);
        int uvs = storeData(UVs);
        
        Vector3D min = new Vector3D(), max = new Vector3D();
        calcMaxMin(positions, min, max);
        
        return new Mesh(new int[]{verts}, new int[]{uvs}, new int[]{UVs.length >> 1}, new Material[]{tex}, min, max);
    }
    
    public static Mesh createMesh(float[][] positions, float[][] UVs, Material[] tex) {
        Mesh mesh = new Mesh();
        
        Vector3D min = new Vector3D(), max = new Vector3D();
        calcMaxMin(positions, min, max);
        
        createMesh(mesh, positions, UVs, tex, min, max);

        return mesh;
    }*/
    
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
    
    private static void calcMaxMin(float[] positions, Vector3D min, Vector3D max) {
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for(int i=0; i<positions.length; i+=3) {
            
            if(positions[i] > max.x) max.x = positions[i];
            if(positions[i] < min.x) min.x = positions[i];

            if(positions[i+1] > max.y) max.y = positions[i+1];
            if(positions[i+1] < min.y) min.y = positions[i+1];

            if(positions[i+2] > max.z) max.z = positions[i+2];
            if(positions[i+2] < min.z) min.z = positions[i+2];
        }
    }
    private static void calcMaxMin(float[][] opositions, Vector3D min, Vector3D max) {
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for(float[] positions : opositions)
            for(int i=0; i<positions.length; i+=3) {

                if(positions[i] > max.x) max.x = positions[i];
                if(positions[i] < min.x) min.x = positions[i];

                if(positions[i + 1] > max.y) max.y = positions[i + 1];
                if(positions[i + 1] < min.y) min.y = positions[i + 1];

                if(positions[i + 2] > max.z) max.z = positions[i + 2];
                if(positions[i + 2] < min.z) min.z = positions[i + 2];
            }
    }
    
    public static Mesh[] loadObj(String name) {
        return loadObj(name, false, null, null);
    }
    
    public static Mesh[] loadObj(String name, boolean createPhysics, String prefix, String postfix) {
        String[] data = StringTools.cutOnStrings(name, '|');
        Hashtable<String, String> replace = new Hashtable();
        for(int i=1; i<data.length; i+=2) {
            replace.put(data[i], data[i+1]);
        }
        
        File file = new File("data", data[0]);
        String line = null;
        
        try {
            if(!file.exists()) throw new Exception("Can't find model "+file.getAbsolutePath());
            Scanner sn = new Scanner(file, "UTF-8");
            sn.useDelimiter("\n");
            
            Vector<float[]> verts = new Vector();
            Vector<float[]> normalsV = new Vector();
            Vector<float[]> uvs = new Vector();
            
            Vector<Mesh> meshes = new Vector();
            Mesh currentMesh = null;
            
            LinkedHashMap<String, Vector<Face>> materials = new LinkedHashMap();
            Vector<Face> faces = null;
            String texName = null;
            
            Vector3D max = new Vector3D(), min = new Vector3D();
            
            boolean prev = false, cur;
            while((cur = sn.hasNext()) || prev) {
                line = cur ? sn.next().trim() : "end";
                prev = cur;
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
            
            sn.close();
            
            return meshes.toArray(new Mesh[meshes.size()]);
        } catch (Exception e) {
            System.out.println(line);
            Engine.printError(e);
        }
        
        return null;
    }

}

class Face {
    public int[] pos;
    public int[] uv;
    public int[] normals;
}
