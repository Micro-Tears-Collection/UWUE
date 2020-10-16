package code.math;

import code.engine3d.Mesh;

/**
 *
 * @author Roman Lahin
 */
public class SphereCast {

    private static final Vector3D temp = new Vector3D();
    private static final Vector3D nor = new Vector3D();
    private static final Vector3D v1 = new Vector3D(), v2 = new Vector3D(), v3 = new Vector3D();
    
    public static boolean isSphereAABBCollision(Vector3D pos, float rad, 
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        
        return !(pos.x+rad < minx || pos.y < miny || pos.z+rad < minz 
                || pos.x-rad > maxx || pos.y-rad > maxy || pos.z-rad > maxz);
    }
    
    public static boolean sphereCast(Mesh mesh, Vector3D pos, float rad) {
        float[][] xyz = mesh.physicsVerts;
        float[][] normals = mesh.normalsPerFace;
        
        boolean col = false;
        
        for(int t=0; t<xyz.length; t++) {
            float[] verts = xyz[t];
            float[] norms = normals[t];

            for(int i = 0; i < verts.length; i += 9) {
                float cx = verts[i], cy = verts[i + 1], cz = verts[i + 2];
                float bx = verts[i + 3], by = verts[i + 4], bz = verts[i + 5];
                float ax = verts[i + 6], ay = verts[i + 7], az = verts[i + 8];

                if(max(ax, bx, cx) < pos.x - rad) continue;
                if(min(ax, bx, cx) > pos.x + rad) continue;
                if(max(az, bz, cz) < pos.z - rad) continue;
                if(min(az, bz, cz) > pos.z + rad) continue;
                if(max(ay, by, cy) < pos.y - rad) continue;
                if(min(ay, by, cy) > pos.y + rad) continue;

                v1.set(ax, ay, az);
                v2.set(bx, by, bz);
                v3.set(cx, cy, cz);
                
                nor.set(norms[i / 3], norms[i / 3 + 1], norms[i / 3 + 2]);
                float dis = distanceSphereToPolygon(v1, v2, v3, nor, pos, rad);

                if(dis != Float.MAX_VALUE && dis > 0) {
                    pos.add(-nor.x * dis, -nor.y * dis, -nor.z * dis);
                    col = true;
                }
            }
        }
        
        return col;
    }

    private static float distanceSphereToPolygon(Vector3D a, Vector3D b, Vector3D c, 
            Vector3D nor, Vector3D point, float rad) {
        
        temp.set(point.x-a.x, point.y-a.y, point.z-a.z);
        float dot = temp.dot(nor); //Расстояние
        if(dot > rad) return Float.MAX_VALUE;
        
        //Проекция на плоскость
        temp.set(point.x-(nor.x*dot), point.y-(nor.y*dot), point.z-(nor.z*dot));
        if(MathUtils.isPointOnPolygon(temp, a, b, c, nor)) {
            return rad - Math.abs(dot);
        }

        final float len1 = MathUtils.distanceToLine(point, a, b);
        final float len2 = MathUtils.distanceToLine(point, b, c);
        final float len3 = MathUtils.distanceToLine(point, c, a);

        float min = min(len1, len2, len3);
        
        if(min <= rad*rad) return rad - (float)Math.sqrt(min);
        
        return Float.MAX_VALUE;
    }
    
    public static float min(float x1, float x2, float x3) {
        return Math.min(Math.min(x1, x2), x3);
    }
    
    public static float max(float x1, float x2, float x3) {
        return Math.max(Math.max(x1, x2), x3);
    }
    
}
