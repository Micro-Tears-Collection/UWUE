package code.math;

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
