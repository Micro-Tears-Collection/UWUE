package code.math.collision;

import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class Sphere {
    
    public float radius, height;
    public final Vector3D pos = new Vector3D();
    
    public boolean collision, onFloor;
    public Object mesh;
    public int submesh, polID;
    
    public Sphere() {
        reset();
    }

    public void set(Object mesh, int submesh, int polID) {
        collision = true;
        this.mesh = mesh;
        this.submesh = submesh;
        this.polID = polID;
    }

    public void reset() {
        collision = onFloor = false;
        mesh = null;
        submesh = polID = 0;
    }

}
