package code.math;

import code.engine3d.Mesh;

/**
 *
 * @author Roman Lahin
 */
public class Sphere {
    
    public float radius, height;
    public Vector3D pos = new Vector3D();
    
    public boolean collision, onFloor;
    public Mesh mesh;
    public int submesh, polID;
    
    public Sphere() {
        reset();
    }

    public void set(Mesh mesh, int submesh, int polID) {
        this.collision = true;
        this.submesh = submesh;
        this.mesh = mesh;
        this.polID = polID;
    }

    public void reset() {
        collision = onFloor = false;
        mesh = null;
        submesh = polID = 0;
    }

}
