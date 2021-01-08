package code.game.world;

import code.engine3d.E3D;
import code.engine3d.Mesh;
import code.math.Culling;
import code.math.Ray;
import code.math.RayCast;
import code.math.Sphere;
import code.math.SphereCast;
import code.math.Vector3D;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Node {
    
    public Mesh mesh;
    public Vector3D min, max;
    public Vector<Node> childs;
    
    /**
     * Node creation
     * @param mesh Nullable
     * @param min Minimal bounding box xyz
     * @param max Maximum bounding box xyz
     */
    public Node(Mesh mesh, Vector3D min, Vector3D max) {
        this.mesh = mesh;
        this.min = min;
        this.max = max;
        
        childs = new Vector();
    }
    
    public boolean hasChild(Node child) {
        if(child == this) return true;
        
        for(Node node : childs) {
            if(node.hasChild(child)) return true;
        }
        
        return false;
    }
    
    static final Culling cul = new Culling();
    
    public void render(E3D e3d, float[] invCam, World world, long renderTime) {
        if(mesh != null) mesh.setMatrix(invCam);
        cul.setBox(min, max);
        
        if(cul.visible()) {
            if(mesh != null) {
                mesh.animate(renderTime, true);
                mesh.prepareRender(e3d);
            }
            
            for(int i=0; i<childs.size(); i++) {
                childs.elementAt(i).render(e3d, invCam, world, renderTime);
            }
        }
    }
    
    public void sphereCast(Sphere sphere) {
        if(SphereCast.isSphereAABBCollision(
                sphere,
                min.x, min.y, min.z,
                max.x, max.y, max.z)) {
            
            if(mesh != null && mesh.physicsVerts != null) SphereCast.sphereCast(mesh, sphere);
            
            for(int i=0; i<childs.size(); i++) {
                childs.elementAt(i).sphereCast(sphere);
            }
        }
    }
    
    public void rayCast(Ray ray) {
        if(RayCast.isRayAABBCollision(
                ray,
                min.x, min.y, min.z,
                max.x, max.y, max.z)) {
            
            if(mesh != null && mesh.physicsVerts != null) RayCast.rayCast(mesh, ray);
            
            for(int i=0; i<childs.size(); i++) {
                childs.elementAt(i).rayCast(ray);
            }
        }
    }
    
    public String toString() {
        return mesh==null?super.toString().substring(21):mesh.name;
    }

}
