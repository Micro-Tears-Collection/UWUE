package code.math.collision;

import code.math.MathUtils;
import code.math.Vector3D;
import java.nio.FloatBuffer;

/**
 *
 * @author Roman Lahin
 */
public class SphereCast {

    private static final Vector3D nor = new Vector3D();
    private static final Vector3D v1 = new Vector3D(), v2 = new Vector3D(), v3 = new Vector3D();
    
    public static boolean isSphereAABBCollision(Sphere sphere, Vector3D min, Vector3D max) {
        return isSphereAABBCollision(sphere, min.x, min.y, min.z, max.x, max.y, max.z);
    }
    
    public static boolean isSphereAABBCollision(Sphere sphere, 
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        
        return !(sphere.pos.x+sphere.radius < minx || 
                sphere.pos.y+sphere.height/2 < miny || 
                sphere.pos.z+sphere.radius < minz || 
                sphere.pos.x-sphere.radius > maxx || 
                sphere.pos.y-sphere.height/2 > maxy || 
                sphere.pos.z-sphere.radius > maxz);
    }
    
    public static void sphereCast(Object mesh, 
			float[][] vertsAll, float[][] facesNormals,
			Sphere sphere) {
        SphereCast.sphereCast(mesh, vertsAll, facesNormals, null, sphere);
    }
    
    public static void sphereCast(Object mesh, 
			float[][] vertsAll, float[][] facesNormals,
			FloatBuffer mat, Sphere sphere) {
        
        Vector3D pos = sphere.pos;
        float rad = sphere.radius;
        float height = sphere.height / 2f;
        
        float toRad = 1*rad/height;
        float toHeight = 1*height/rad;
        
        for(int t=0; t<vertsAll.length; t++) {
            float[] verts = vertsAll[t];
            float[] norms = facesNormals[t];

            for(int i = 0; i < verts.length; i += 9) {
                float ax = verts[i], ay = verts[i + 1], az = verts[i + 2];
                float bx = verts[i + 3], by = verts[i + 4], bz = verts[i + 5];
                float cx = verts[i + 6], cy = verts[i + 7], cz = verts[i + 8];

                v1.set(ax, ay, az);
                v2.set(bx, by, bz);
                v3.set(cx, cy, cz);
                if(mat != null) {
                    v1.transform(mat);
                    v2.transform(mat);
                    v3.transform(mat);
                }

                if(MathUtils.max(v1.x, v2.x, v3.x) < pos.x - rad) continue;
                if(MathUtils.min(v1.x, v2.x, v3.x) > pos.x + rad) continue;
                if(MathUtils.max(v1.y, v2.y, v3.y) < pos.y - height) continue;
                if(MathUtils.min(v1.y, v2.y, v3.y) > pos.y + height) continue;
                if(MathUtils.max(v1.z, v2.z, v3.z) < pos.z - rad) continue;
                if(MathUtils.min(v1.z, v2.z, v3.z) > pos.z + rad) continue;
                
                v1.y = (v1.y-pos.y)*toRad + pos.y;
                v2.y = (v2.y-pos.y)*toRad + pos.y;
                v3.y = (v3.y-pos.y)*toRad + pos.y;
                
                nor.set(norms[i / 3], norms[i / 3 + 1], norms[i / 3 + 2]);
                if(mat != null) nor.transform(mat, false);
                
                boolean floor = nor.y > 0.3f;
                
                nor.mul(toRad, 1, toRad);
                nor.setLength(1);
                float dis = MathUtils.distanceSphereToPolygon(v1, v2, v3, nor, pos, rad);

                if(dis != Float.MAX_VALUE && dis > 0) {
                    pos.add(nor.x * dis, nor.y * dis * toHeight, nor.z * dis);
                    sphere.set(mesh, t, i);
                    
                    if(floor) sphere.onFloor = true;
                }
            }
        }
    }
    
}
