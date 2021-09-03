package code.math.collision;

import code.math.Vector3D;

public class Ray {
    public boolean collision;
    public float distance;
    public Object mesh;
    public int submesh, polID;

    public final Vector3D collisionPoint = new Vector3D();
    public final Vector3D start = new Vector3D(), dir = new Vector3D();

    public Ray() {
        reset();
    }

    public final void set(Object mesh, int submesh, int polID, float distance, Vector3D colPoint) {
        collision = true;
        this.mesh = mesh;
        this.submesh = submesh;
        this.polID = polID;
        this.distance = distance;
        collisionPoint.set(colPoint);
    }

    public final void set(Ray ray) {
        collision = ray.collision;
        mesh = ray.mesh;
        submesh = ray.submesh;
        polID = ray.polID;
        distance = ray.distance;
        collisionPoint.set(ray.collisionPoint);
    }

    public final void reset() {
        collision = false;
        mesh = null;
        submesh = polID = 0;
        distance = Float.MAX_VALUE;
    }

}
