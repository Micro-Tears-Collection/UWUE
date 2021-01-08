package code.math;

/**
 *
 * @author Roman Lahin
 */
public class Vector3D {
    
    public float x, y, z;
    
    public Vector3D() {}
    
    public Vector3D(float x, float y, float z) {
        set(x, y, z);
    }
    
    public Vector3D(Vector3D vec) {
        set(vec.x, vec.y, vec.z);
    }
    
    public void set(Vector3D vec) {
        x = vec.x; y = vec.y; z = vec.z;
    }
    
    public void set(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }
    
    public void add(Vector3D vec) {
        add(vec.x, vec.y, vec.z);
    }
    
    public void add(float x, float y, float z) {
        this.x += x; this.y += y; this.z += z;
    }
    
    public void sub(Vector3D vec) {
        sub(vec.x, vec.y, vec.z);
    }
    
    public void sub(float x, float y, float z) {
        this.x -= x; this.y -= y; this.z -= z;
    }
    
    public void mul(Vector3D vec) {
        mul(vec.x, vec.y, vec.z);
    }
    
    public void mul(float x, float y, float z) {
        this.x *= x; this.y *= y; this.z *= z;
    }
    
    public void div(Vector3D vec) {
        div(vec.x, vec.y, vec.z);
    }
    
    public void div(float x, float y, float z) {
        this.x /= x; this.y /= y; this.z /= z;
    }
    
    public void max(Vector3D vec) {
        max(vec.x, vec.y, vec.z);
    }
    
    public void max(float x, float y, float z) {
        this.x = Math.max(this.x, x);
        this.y = Math.max(this.y, y);
        this.z = Math.max(this.z, z);
    }
    
    public void min(Vector3D vec) {
        min(vec.x, vec.y, vec.z);
    }
    
    public void min(float x, float y, float z) {
        this.x = Math.min(this.x, x);
        this.y = Math.min(this.y, y);
        this.z = Math.min(this.z, z);
    }
    
    public void abs() {
        if(this.x < 0) this.x = -this.x;
        if(this.y < 0) this.y = -this.y;
        if(this.z < 0) this.z = -this.z;
    }
    
    public void transform(float[] matrix) {
        float xx = x, yy = y, zz = z;
        
        //column-major order sucks
        x = xx * matrix[0] + yy * matrix[4] + zz * matrix[8] + matrix[12];
        y = xx * matrix[1] + yy * matrix[5] + zz * matrix[9] + matrix[13];
        z = xx * matrix[2] + yy * matrix[6] + zz * matrix[10] + matrix[14];
    }
    
    public void transformNoOffset(float[] matrix) {
        float xx = x, yy = y, zz = z;
        
        //column-major order sucks
        x = xx * matrix[0] + yy * matrix[4] + zz * matrix[8];
        y = xx * matrix[1] + yy * matrix[5] + zz * matrix[9];
        z = xx * matrix[2] + yy * matrix[6] + zz * matrix[10];
    }

    public void interpolate(Vector3D v1, Vector3D v2, int i, int max) {
        x = (v1.x * (max - i) + v2.x * i) / max;
        y = (v1.y * (max - i) + v2.y * i) / max;
        z = (v1.z * (max - i) + v2.z * i) / max;
    }
    
    public float dot(Vector3D v) {
        return x*v.x + y*v.y + z*v.z;
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float lengthSquared() {
        return x*x + y*y + z*z;
    }
    
    public void setLength(float len) {
        float l = x*x + y*y + z*z;
        if(l == len*len) return;
        
        double i = len / Math.sqrt(l);
        
        x *= i;
        y *= i;
        z *= i;
    }
    
    public float distance(Vector3D v) {
        return (float) Math.sqrt(distanceSqr(v));
    }
    
    public float distanceSqr(Vector3D v) {
        return (x-v.x)*(x-v.x) + (y-v.y)*(y-v.y) + (z-v.z)*(z-v.z);
    }
    
    public void setDirection(float rotX, float rotY) {
        final float xa = 1f;
        float ya = xa * (float) Math.cos(Math.toRadians(rotX));
        float yaYDSin = ya * (float) -Math.sin(Math.toRadians(rotY));
        float yaYDCos = ya * (float) -Math.cos(Math.toRadians(rotY));

        x = yaYDSin;
        y = xa * (float) Math.sin(Math.toRadians(rotX));
        z = yaYDCos;
    }
    
    public void calcNormal(Vector3D a, Vector3D b, Vector3D c) {
        x = (a.y - b.y) * (a.z - c.z) - (a.z - b.z) * (a.y - c.y);
        y = (a.z - b.z) * (a.x - c.x) - (a.x - b.x) * (a.z - c.z);
        z = (a.x - b.x) * (a.y - c.y) - (a.y - b.y) * (a.x - c.x);

        setLength(1);
    }

}
