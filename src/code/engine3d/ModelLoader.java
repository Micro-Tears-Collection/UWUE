package code.engine3d;

import code.math.Vector3D;
import code.utils.IniFile;

import code.utils.StringTools;
import code.utils.assetManager.AssetManager;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class ModelLoader {
    
    static Mesh[] loadObj(E3D e3d, String name) {
		String[] lines = AssetManager.loadLines(name);
        String line = null;
        
        try {
            ArrayList<float[]> verts = new ArrayList<float[]>();
            ArrayList<float[]> normalsV = new ArrayList<float[]>();
            ArrayList<float[]> uvs = new ArrayList<float[]>();
            
            ArrayList<Mesh> meshes = new ArrayList<Mesh>();
			String currentMesh = null;
            ArrayList<Face> faces = null;
			
            Vector3D max = new Vector3D(), min = new Vector3D();
            
            LinkedHashMap<String, ArrayList<Face>> materials = new LinkedHashMap();
            String currentMaterial = null;
            
            for(int lineNum = 0; lineNum <= lines.length; lineNum++) {
                line = (lineNum < lines.length) ? lines[lineNum].trim() : "end";
                if(line.isEmpty()) continue;
                
                if(line.startsWith("f ")) {
                    String[] faceVerts = StringTools.cutOnStrings(line.substring(2), ' ');
                    int[][] faceData = new int[faceVerts.length][];
                    
                    for(int i=0; i<faceVerts.length; i++) {
                        faceData[i] = StringTools.cutOnInts(faceVerts[i], '/');
                    }
                    
                    boolean hasUV = false, hasNorm = false;
                    if(faceData[0].length >= 2) hasUV = true;
                    if(faceData[0].length >= 3) hasNorm = true;
                    
                    for(int i=0; i<faceVerts.length-2; i++) {
                        Face face = new Face();
                        face.pos = new int[] {
							faceData[0][0], faceData[i+1][0], faceData[i+2][0]
						};
                        
                        if(hasUV) face.uv = new int[] {
							faceData[0][1], faceData[i+1][1], faceData[i+2][1]
						};
                        
                        if(hasNorm) face.normals = new int[] {
							faceData[0][2], faceData[i+1][2], faceData[i+2][2]
						};
                        
                        faces.add(face);
                    }
					
                } else if(line.startsWith("usemtl ")) {
                    String matName = line.substring(7);
                    
                    if(!matName.equals(currentMaterial)) {
                        currentMaterial = matName;
                        
                        faces = materials.get(currentMaterial);
                        if(faces == null) {
                            faces = new ArrayList<Face>();
                            materials.put(currentMaterial, faces);
                        }
                    }
                } else {
                    
                    boolean newMesh = line.startsWith("o ");
                    
                    if((newMesh || line.equals("end")) && currentMesh != null) {
                        if(materials.get("null").isEmpty()) materials.remove("null");
                        
                        Set meshMatsSet = materials.keySet();
                        String[] meshMatsNames = new String[meshMatsSet.size()];
                        meshMatsSet.toArray(meshMatsNames);
                        
                        float[][] poses = new float[meshMatsNames.length][];
                        float[][] uvm = new float[meshMatsNames.length][];
                        float[][] normals = new float[meshMatsNames.length][];
                        
                        for(int i=0; i<meshMatsNames.length; i++) {
                            ArrayList<Face> meshFaces = materials.get(meshMatsNames[i]);
                            
                            poses[i] = new float[meshFaces.size() * 3 * 3];
                            uvm[i] = new float[meshFaces.size() * 3 * 2];
                            normals[i] = new float[meshFaces.size() * 3 * 3];
                            int pp = 0, uvp = 0, np = 0;
							
                            for(int x=0; x<meshFaces.size(); x++) {
                                Face face = meshFaces.get(x);
                                
                                for(int y=0; y<3; y++) {
                                    float[] vert = verts.get(face.pos[y] - 1);
                                    System.arraycopy(vert, 0, poses[i], pp, 3);
                                    pp += 3;
                                    
                                    if(vert[0] > max.x) max.x = vert[0];
                                    if(vert[0] < min.x) min.x = vert[0];
                                    
                                    if(vert[1] > max.y) max.y = vert[1];
                                    if(vert[1] < min.y) min.y = vert[1];
                                    
                                    if(vert[2] > max.z) max.z = vert[2];
                                    if(vert[2] < min.z) min.z = vert[2];
                                    
                                    if(face.uv != null) {
                                        vert = uvs.get(face.uv[y] - 1);
                                        uvm[i][uvp] = vert[0];
                                        uvm[i][uvp+1] = 1f-vert[1];
                                        uvp += 2;
                                    }
                                    
                                    if(face.normals != null) {
                                        vert = normalsV.get(face.normals[y] - 1);
                                        System.arraycopy(vert, 0, normals[i], np, 3);
                                        np += 3;
                                    }
                                }
                            }
                        }
						
                        String[] meshNameSplit = StringTools.cutOnStrings(currentMesh, ';');
                        Mesh mesh = new Mesh(meshNameSplit[0], poses, uvm, normals, meshMatsNames, min, max);
						mesh.ini = new IniFile(meshNameSplit, false);
                        
                        meshes.add(mesh);
                        
                        currentMesh = null;
                    }
                    
                    if(newMesh) {
						currentMesh = line.substring(2);
                        
                        faces = new ArrayList<Face>();
                        currentMaterial = "null";
                        materials.clear();
                        materials.put(currentMaterial, faces);
                        
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
