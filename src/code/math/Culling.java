package code.math;

import java.nio.FloatBuffer;

/**
 *
 * @author Roman Lahin
 */
public class Culling {
    
    public static final int INVISIBLE = 0, VISIBLE = 1, FULLY_VISIBLE = 2;
    
    private static final float[] invCam = new float[16];
    private static float zNearClip, zFarClip;
    private static float xLine, yLine;
    /*
    
    \  ^ yY
     \ |
      \ <-yX
      /
     /
    /
    
    \      / ^ xY
     \    /  |
      \  /
       \/ <- xX
    */
    private final Vector3D[] verts;
    
    public static void set(FloatBuffer invertedCameraMatrix, float fovx, float fovy, float zNear, float zFar) {
        float xX = (float)Math.sin(Math.toRadians(fovx)/2);
        float xY = (float)Math.cos(Math.toRadians(fovx)/2);
        xLine = xX / xY;
        
        float yX = (float)Math.cos(Math.toRadians(fovy)/2);
        float yY = (float)Math.sin(Math.toRadians(fovy)/2);
        yLine = yX / yY;
        
        invertedCameraMatrix.get(invCam);
        invertedCameraMatrix.rewind();
        
        zNearClip = zNear;
        zFarClip = zFar;
    }
    
    public Culling() {
        verts = new Vector3D[8];
        for(int i=0; i<8; i++) {
            verts[i] = new Vector3D();
        }
    }
    
    public void setBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        verts[0].set(x1, y1, z1);
        verts[1].set(x2, y1, z1);
        verts[2].set(x1, y1, z2);
        verts[3].set(x2, y1, z2);
        
        verts[4].set(x1, y2, z1);
        verts[5].set(x2, y2, z1);
        verts[6].set(x1, y2, z2);
        verts[7].set(x2, y2, z2);
        
        for(int i=0; i<8; i++) {
            verts[i].transform(invCam);
        }
    }
    
    public void setBox(Vector3D min, Vector3D max) {
        verts[0].set(min.x, min.y, min.z);
        verts[1].set(max.x, min.y, min.z);
        verts[2].set(min.x, min.y, max.z);
        verts[3].set(max.x, min.y, max.z);
        
        verts[4].set(min.x, max.y, min.z);
        verts[5].set(max.x, max.y, min.z);
        verts[6].set(min.x, max.y, max.z);
        verts[7].set(max.x, max.y, max.z);
        
        for(int i=0; i<8; i++) {
            verts[i].transform(invCam);
        }
    }
    
    public int visible() {
        int zNear = 0, zFar = 0, xLeft = 0, xRight = 0, yTop = 0, yDown = 0;
        
        for(int i=0; i<8; i++) {
            Vector3D v = verts[i];
            
            if(v.z > -zNearClip) zNear++;
            else if(v.z < -zFarClip) zFar++;
            
            float rightBorderX = -v.z * xLine;
            if(v.x < -rightBorderX) xLeft++;
            else if(v.x > rightBorderX) xRight++;
            
            float downBorderY = -v.z * yLine;
            if(v.y < -downBorderY) yDown++;
            else if(v.y > downBorderY) yTop++;
        }
        
        if(zNear == 0 && zFar == 0 && xLeft == 0 && xRight == 0 && yTop == 0 && yDown == 0) return FULLY_VISIBLE;
        return (zNear != 8 && zFar != 8 && xLeft != 8 && xRight != 8 && yTop != 8 && yDown != 8) ? VISIBLE : INVISIBLE;
    }
    
}
