package code.math;

import code.engine3d.Mesh;

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

    public static void rayCast(Mesh mesh, Ray ray) {
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

                if(SphereCast.max(ax, bx, cx) < x1) continue;
                if(SphereCast.min(ax, bx, cx) > x2) continue;
                if(SphereCast.max(az, bz, cz) < z1) continue;
                if(SphereCast.min(az, bz, cz) > z2) continue;
                if(SphereCast.max(ay, by, cy) < y1) continue;
                if(SphereCast.min(ay, by, cy) > y2) continue;

                v1.set(ax, ay, az);
                v2.set(bx, by, bz);
                v3.set(cx, cy, cz);

                normal.set(norms[i / 3], norms[i / 3 + 1], norms[i / 3 + 2]);
                float dis = rayCast(v1, v2, v3, normal, start, dir, colPoint);

                if(dis != Float.MAX_VALUE) {
                    float distance = dirLen * dis;
                    if(distance < ray.distance) {
                        ray.set(mesh, t, i, distance, colPoint);
                    }
                }
            }
        }
    }


    private static float rayCast(Vector3D a, Vector3D b, Vector3D c, Vector3D nor, 
            Vector3D start, Vector3D dir, Vector3D pos) {
        
        pos.set(start.x-a.x, start.y-a.y, start.z-a.z);
        float dot = dir.dot(nor);
        
        if(dot <= 0) return Float.MAX_VALUE;
        dot = -pos.dot(nor) / dot;
        if(dot < 0 || dot > 1) return Float.MAX_VALUE;
        
        pos.set(start.x + (dir.x * dot),
                start.y + (dir.y * dot),
                start.z + (dir.z * dot));
        
        if(MathUtils.isPointOnPolygon(pos, a, b, c, nor)) return dot;
        
        return Float.MAX_VALUE;
    }

}
