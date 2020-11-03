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
    
    public static boolean isSphereAABBCollision(Sphere sphere, 
            float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        
        return !(sphere.pos.x+sphere.radius < minx || 
                sphere.pos.y+sphere.height/2 < miny || 
                sphere.pos.z+sphere.radius < minz || 
                sphere.pos.x-sphere.radius > maxx || 
                sphere.pos.y-sphere.height/2 > maxy || 
                sphere.pos.z-sphere.radius > maxz);
    }
    
    public static void sphereCast(Mesh mesh, Sphere sphere) {
        SphereCast.sphereCast(mesh, null, sphere);
    }
    
    public static void sphereCast(Mesh mesh, float[] mat, Sphere sphere) {
        if(!mesh.collision) return;
        
        float[][] xyz = mesh.physicsVerts;
        float[][] normals = mesh.normalsPerFace;
        
        Vector3D pos = sphere.pos;
        float rad = sphere.radius;
        float height = sphere.height / 2f;
        
        float toRad = 1*rad/height;
        float toHeight = 1*height/rad;
        
        for(int t=0; t<xyz.length; t++) {
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

                if(max(v1.x, v2.x, v3.x) < pos.x - rad) continue;
                if(min(v1.x, v2.x, v3.x) > pos.x + rad) continue;
                if(max(v1.y, v2.y, v3.y) < pos.y - height) continue;
                if(min(v1.y, v2.y, v3.y) > pos.y + height) continue;
                if(max(v1.z, v2.z, v3.z) < pos.z - rad) continue;
                if(min(v1.z, v2.z, v3.z) > pos.z + rad) continue;
                
                v1.y = (v1.y-pos.y)*toRad + pos.y;
                v2.y = (v2.y-pos.y)*toRad + pos.y;
                v3.y = (v3.y-pos.y)*toRad + pos.y;
                
                nor.set(norms[i / 3], norms[i / 3 + 1], norms[i / 3 + 2]);
                if(mat != null) nor.transformNoOffset(mat);
                
                boolean floor = nor.y < -0.3f;
                
                nor.mul(toRad, 1, toRad);
                nor.setLength(1);
                float dis = distanceSphereToPolygon(v1, v2, v3, nor, pos, rad);

                if(dis != Float.MAX_VALUE && dis > 0) {
                    pos.add(-nor.x * dis, -nor.y * dis * toHeight, -nor.z * dis);
                    sphere.set(mesh, t, i);
                    
                    if(floor) sphere.onFloor = true;
                }
            }
        }
    }

    private static float distanceSphereToPolygon(Vector3D a, Vector3D b, Vector3D c, 
            Vector3D nor, Vector3D point, float rad) {
        
        temp.set(point.x-a.x, point.y-a.y, point.z-a.z);
        float dist = temp.dot(nor); //Расстояние
        if(dist > 0) return Float.MAX_VALUE;
        
        //Проекция на плоскость
        temp.set(point.x-(nor.x*dist), point.y-(nor.y*dist), point.z-(nor.z*dist));
        if(MathUtils.isPointOnPolygon(temp, a, b, c, nor)) {
            return rad - Math.abs(dist);
        }

        final float len1 = MathUtils.distanceToLine(point, a, b);
        final float len2 = MathUtils.distanceToLine(point, b, c);
        final float len3 = MathUtils.distanceToLine(point, c, a);

        float min = min(len1, len2, len3);
        if(min == len1) {
            nor.set(closestPointOnLineSegment(a, b, point));
        } else if(min == len2) {
            nor.set(closestPointOnLineSegment(b, c, point));
        } else if(min == len3) {
            nor.set(closestPointOnLineSegment(c, a, point));
        }
        nor.sub(point);
        nor.setLength(1);
        
        if(min <= rad*rad) return rad - (float)Math.sqrt(min);
        
        return Float.MAX_VALUE;
    }
    
    private static Vector3D closestPointOnLineSegment(Vector3D a, Vector3D b, Vector3D point) {
        Vector3D ab = new Vector3D(b);
        ab.sub(a);
        temp.set(point);
        temp.sub(a);
        float t = temp.dot(ab) / ab.lengthSquared();
        t = Math.min(Math.max(t, 0), 1);
        
        temp.set(ab);
        temp.mul(t, t, t);
        temp.add(a);
        return temp;
    }
    
    public static float min(float x1, float x2, float x3) {
        return Math.min(Math.min(x1, x2), x3);
    }
    
    public static float max(float x1, float x2, float x3) {
        return Math.max(Math.max(x1, x2), x3);
    }
    
}
