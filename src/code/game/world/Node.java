package code.game.world;

import code.engine3d.E3D;
import code.engine3d.Mesh;
import code.math.Culling;
import code.math.Ray;
import code.math.RayCast;
import code.math.SphereCast;
import code.math.Vector3D;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Node {
    
    public Mesh mesh;
    public Vector<Node> childs;
    
    public Node(Mesh mesh) {
        this.mesh = mesh;
        childs = new Vector();
    }
    
    static final Culling cul = new Culling();
    
    public void render(E3D e3d, float[] invCam, World world) {
        cul.setBox(mesh.min, mesh.max);
        
        if(cul.visible()) {
            mesh.setMatrix(invCam);
            mesh.prepareRender(e3d);
            
            for(int i=0; i<childs.size(); i++) {
                childs.elementAt(i).render(e3d, invCam, world);
            }
        }
    }
    
    public boolean sphereCast(Vector3D sphere, float radius) {
        boolean col = false;

        if(SphereCast.isSphereAABBCollision(
                sphere, radius,
                mesh.min.x, mesh.min.y, mesh.min.z,
                mesh.max.x, mesh.max.y, mesh.max.z)) {
            
            if(mesh.physicsVerts != null) col |= SphereCast.sphereCast(mesh, sphere, radius);
            
            for(int i=0; i<childs.size(); i++) {
                col |= childs.elementAt(i).sphereCast(sphere, radius);
            }
        }
        
        return col;
    }
    
    public void rayCast(Ray ray) {
        if(RayCast.isRayAABBCollision(
                ray,
                mesh.min.x, mesh.min.y, mesh.min.z,
                mesh.max.x, mesh.max.y, mesh.max.z)) {
            
            if(mesh.physicsVerts != null) RayCast.rayCast(mesh, ray);
            
            for(int i=0; i<childs.size(); i++) {
                childs.elementAt(i).rayCast(ray);
            }
        }
    }

}
