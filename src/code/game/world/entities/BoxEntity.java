package code.game.world.entities;

import code.math.collision.Ray;
import code.math.collision.RayCast;
import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class BoxEntity extends Entity {
    
    public Vector3D size = new Vector3D();
    
    public BoxEntity(float sizex, float sizey, float sizez) {
        size.set(sizex, sizey, sizez);
        
        activable = true;
    }
    
    public void destroy() {
        super.destroy();
        size = null;
    }
	
	public Vector3D getMin() {
		return new Vector3D(pos.x - size.x/2, pos.y - size.y/2, pos.z - size.z/2);
	}
	
	public Vector3D getMax() {
		return new Vector3D(pos.x + size.x/2, pos.y + size.y/2, pos.z + size.z/2);
	}
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(onlyMeshes || !activable) return false;
        
        return RayCast.cubeRayCast(ray, 
                pos.x-size.x/2, pos.y-size.y/2, pos.z-size.z/2, 
                pos.x+size.x/2, pos.y+size.y/2, pos.z+size.z/2);
    }
    
    protected boolean inRadius(Vector3D start) {
        float xx = (start.x - pos.x);
        xx = Math.max(0, xx*xx - size.x*size.x/4);
        
        float yy = (start.y - pos.y);
        yy = Math.max(0, yy*yy - size.y*size.y/4);
        
        float zz = (start.z - pos.z);
        zz = Math.max(0, zz*zz - size.z*size.z/4);
        
        return (xx+yy+zz) <= activateRadius*activateRadius;
    }

}
