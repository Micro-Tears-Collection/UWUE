package code.math;

import code.engine3d.Mesh;

public class Ray {
    public boolean collision = false;
    public float distance;
    public Mesh mesh;
    public int submesh, polID;

    public final Vector3D collisionPoint = new Vector3D();
    public final Vector3D start = new Vector3D(), dir = new Vector3D();

    public Ray() {
        reset();
    }

    public void set(Mesh mesh, int submesh, int polID, float distance, Vector3D colPoint) {
        this.collision = true;
        this.submesh = submesh;
        this.mesh = mesh;
        this.polID = polID;
        this.distance = distance;
        this.collisionPoint.set(colPoint);
    }

    public void set(Ray ray) {
        collision = ray.collision;
        distance = ray.distance;
        collisionPoint.set(ray.collisionPoint);
    }

    public void reset() {
        collision = false;
        mesh = null;
        submesh = polID = 0;
        distance = Float.MAX_VALUE;
    }

}
