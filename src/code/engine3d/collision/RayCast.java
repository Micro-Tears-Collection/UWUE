package code.engine3d.collision;

import code.engine3d.Mesh;
import code.math.MathUtils;
import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class RayCast {
    private static final Vector3D colPoint = new Vector3D();
    private static final Vector3D v1 = new Vector3D(), v2 = new Vector3D(), v3 = new Vector3D();
    private static final Vector3D normal = new Vector3D();
    
    public static boolean isRayAABBCollision(Ray ray, 
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        Vector3D start = ray.start;
        Vector3D dir = ray.dir;
        
        float startX = Math.min(start.x, start.x + dir.x);
        float startY = Math.min(start.y, start.y + dir.y);
        float startZ = Math.min(start.z, start.z + dir.z);
        
        float endX = Math.max(start.x, start.x + dir.x);
        float endY = Math.max(start.y, start.y + dir.y);
        float endZ = Math.max(start.z, start.z + dir.z);
        
        return !(startX>maxx || startY>maxy || startZ>maxz || endX<minx || endY<miny || endZ<minz);
    }
    
    public static boolean cubeRayCast(Ray ray,
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        if(ray.start.x >= minx && ray.start.x <= maxx && 
                ray.start.y >= miny && ray.start.y <= maxy &&
                ray.start.z >= minz && ray.start.z <= maxz) {
            ray.set(null, 0, 0, 0, ray.start);
            return true;
        }
        
        v2.set(ray.dir);
        v2.setLength(1);
        
        boolean col = false;
        
        if(v2.y < 0 && ray.start.y >= maxy) {
            col |= cubeRayCastMini(1, maxy, (maxy - ray.start.y) / v2.y,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        if(v2.y > 0 && ray.start.y <= miny) {
            col |= cubeRayCastMini(1, miny, (miny - ray.start.y) / v2.y,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        if(v2.x < 0 && ray.start.x >= maxx) {
            col |= cubeRayCastMini(0, maxx, (maxx - ray.start.x) / v2.x,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        if(v2.x > 0 && ray.start.x <= minx) {
            col |= cubeRayCastMini(0, minx, (minx - ray.start.x) / v2.x,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        if(v2.z < 0 && ray.start.z >= maxz) {
            col |= cubeRayCastMini(2, maxz, (maxz - ray.start.z) / v2.z,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        if(v2.z > 0 && ray.start.z <= minz) {
            col |= cubeRayCastMini(2, minz, (minz - ray.start.z) / v2.z,
                    ray, minx, miny, minz, maxx, maxy, maxz);
        }
        
        return col;
    }
    
    private static boolean cubeRayCastMini(int xyz, float vl, float d, Ray ray,
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        v1.set(ray.dir);
        v1.setLength(d);
        v1.add(ray.start);
        
        if(xyz == 0) v1.x = vl;
        else if(xyz == 1) v1.y = vl;
        else v1.z = vl;

        if(d < ray.distance && 
                v1.x >= minx && v1.x <= maxx && 
                v1.y >= miny && v1.y <= maxy &&
                v1.z >= minz && v1.z <= maxz) {
            ray.set(null, 0, 0, d, v1);
            return true;
        }
        return false;
    }

    public static void rayCast(Mesh mesh, Ray ray) {
        RayCast.rayCast(mesh, null, ray);
    }

    public static void rayCast(Mesh mesh, float[] mat, Ray ray) {
        if(!mesh.collision) return;
        
        float[][] xyz = mesh.physicsVerts;
        float[][] normals = mesh.normalsPerFace;
        
        final Vector3D start = ray.start;
        final Vector3D dir = ray.dir;

        final float sx = start.x, sy = start.y, sz = start.z;
        final float ex = sx+dir.x, ey = sy+dir.y, ez = sz+dir.z;

        final float x1 = Math.min(sx, ex), y1 = Math.min(sy, ey), z1 = Math.min(sz, ez);
        final float x2 = Math.max(sx, ex), y2 = Math.max(sy, ey), z2 = Math.max(sz, ez);
        
        final float dirLen = dir.length();

        for(int t = 0; t < xyz.length; t++) {
            float[] verts = xyz[t];
            float[] norms = normals[t];

            for(int i = 0; i < verts.length; i += 9) {
                float cx = verts[i], cy = verts[i + 1], cz = verts[i + 2];
                float bx = verts[i + 3], by = verts[i + 4], bz = verts[i + 5];
                float ax = verts[i + 6], ay = verts[i + 7], az = verts[i + 8];

                v1.set(ax, ay, az);
                v2.set(bx, by, bz);
                v3.set(cx, cy, cz);
                if(mat != null) {
                    v1.transform(mat);
                    v2.transform(mat);
                    v3.transform(mat);
                }

                if(MathUtils.max(v1.x, v2.x, v3.x) < x1) continue;
                if(MathUtils.min(v1.x, v2.x, v3.x) > x2) continue;
                if(MathUtils.max(v1.y, v2.y, v3.y) < y1) continue;
                if(MathUtils.min(v1.y, v2.y, v3.y) > y2) continue;
                if(MathUtils.max(v1.z, v2.z, v3.z) < z1) continue;
                if(MathUtils.min(v1.z, v2.z, v3.z) > z2) continue;

                normal.set(norms[i / 3], norms[i / 3 + 1], norms[i / 3 + 2]);
                if(mat != null) normal.transformNoOffset(mat);
                float dis = MathUtils.rayCast(v1, v2, v3, normal, start, dir, colPoint);

                if(dis != Float.MAX_VALUE) {
                    float distance = dirLen * dis;
                    if(distance < ray.distance) {
                        ray.set(mesh, t, i, distance, colPoint);
                    }
                }
            }
        }
    }

}
