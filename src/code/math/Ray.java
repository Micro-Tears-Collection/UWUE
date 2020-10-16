package code.math;

public class Ray {
    public boolean collision = false;
    public float distance;

    public final Vector3D collisionPoint = new Vector3D();
    public final Vector3D start = new Vector3D(), dir = new Vector3D();

    public Ray() {
        reset();
    }

    public void set(boolean isCollision, float distance, Vector3D colPoint) {
        this.collision = isCollision;
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
        distance = Float.MAX_VALUE;
    }

}
