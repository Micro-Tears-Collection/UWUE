package code.math;

/**
 *
 * @author Roman Lahin
 */
public class MathUtils {

    /*public static double pow(final double a, final double b) {
        final int tmp = (int) (Double.doubleToLongBits(a) >> 32);
        final int tmp2 = (int) (b * (tmp - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) tmp2) << 32);
    }*/
    
    private static final double M_PI_4 = 0.78539816339;
    
    public static float atan(float x) {
        return (float) (M_PI_4*x - x*(Math.abs(x) - 1)*(0.2447 + 0.0663*Math.abs(x)));
    }
    
    public static float distanceToRay(Vector3D point, Vector3D start, Vector3D dir) {
        Vector3D d = new Vector3D(dir);
        Vector3D w = new Vector3D(point.x - start.x, point.y - start.y, point.z - start.z);

        float len = d.lengthSquared();
        float dt = 0;
        if(len != 0) dt = w.dot(d) / len;
        if(dt < 0) dt = 0;
        if(dt > 1) dt = 1;

        d.mul(dt, dt, dt);
        d.add(start.x, start.y, start.z);

        d.sub(point.x, point.y, point.z);
        return d.lengthSquared();
    }
    
    public static float distanceToLine(Vector3D point, Vector3D a, Vector3D b) {
        Vector3D d = new Vector3D(b);
	d.sub(a.x, a.y, a.z);

        Vector3D w = new Vector3D(point);
	w.sub(a.x, a.y, a.z);

	float dp = d.lengthSquared();
	float dt = 0;
	if(dp != 0) dt = w.dot(d) / dp;
	if(dt < 0) dt = 0;
	if(dt > 1) dt = 1;

	d.mul(dt, dt, dt);
	d.add(a.x, a.y, a.z);
	d.sub(point.x, point.y, point.z);
	return d.lengthSquared();
    }
    
    public static boolean isPointOnPolygon(Vector3D point, Vector3D a, Vector3D b, Vector3D c, Vector3D normal) {
        final float nx = normal.x>0 ? normal.x : -normal.x;
        final float ny = normal.y>0 ? normal.y : -normal.y;
        final float nz = normal.z>0 ? normal.z : -normal.z;

        if(nx >= ny && nx >= nz) {
            if(normal.x >= 0) {
                return isPointOnPolygon(point.z, point.y, a.z, a.y, b.z, b.y, c.z, c.y);
            } else {
                return isPointOnPolygon(point.z, point.y, c.z, c.y, b.z, b.y, a.z, a.y);
            }
        }

        if(ny >= nx && ny >= nz) {
            if(normal.y >= 0) {
                return isPointOnPolygon(point.x, point.z, a.x, a.z, b.x, b.z, c.x, c.z);
            } else {
                return isPointOnPolygon(point.x, point.z, c.x, c.z, b.x, b.z, a.x, a.z);
            }
        }

        if(nz >= nx && nz >= ny) {
            if(normal.z <= 0) {
                return isPointOnPolygon(point.x, point.y, a.x, a.y, b.x, b.y, c.x, c.y);
            } else {
                return isPointOnPolygon(point.x, point.y, c.x, c.y, b.x, b.y, a.x, a.y);
            }
        }
        return true;
    }
    
    public static boolean isPointOnPolygon(float px, float pz, 
            float ax, float az, float bx, float bz, float cx, float cz, float norY) {
        
        if(norY > 0) return isPointOnPolygon(px, pz, ax, az, bx, bz, cx, cz);
        if(norY < 0) return isPointOnPolygon(px, pz, cx, cz, bx, bz, ax, az);
        
        return false;
    }
    
    public static boolean isPointOnPolygon(float px, float py, 
            float x1, float y1, float x2, float y2, float x3, float y3) {
        return  (x2-x1)*(py-y1) <= (px-x1)*(y2-y1) &&
                (x3-x2)*(py-y2) <= (px-x2)*(y3-y2) && 
                (x1-x3)*(py-y3) <= (px-x3)*(y1-y3);
    }

}
