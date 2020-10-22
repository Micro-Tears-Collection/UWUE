package code.game.world.entities;

import code.engine3d.E3D;
import code.engine3d.Sprite;
import code.game.world.World;
import code.math.MathUtils;
import code.math.Ray;
import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class SpriteObject extends Entity {
    
    public Sprite spr;
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(onlyMeshes) return false;
        
        Vector3D tmp = new Vector3D(pos);
        tmp.add(0, spr.h/2+spr.offsety, 0);
        
        float dist = MathUtils.distanceToRay(tmp, ray.start, ray.dir);
        if(dist > spr.w*spr.w/4) return false;
        
        dist = Math.max(0, ray.start.distanceSqr(tmp) - spr.w*spr.w/4);
        
        if(dist < ray.dir.lengthSquared() && dist < ray.distance*ray.distance) {
            ray.distance = (float) Math.sqrt(dist);
            ray.mesh = null;
            return true;
        }
        
        return false;
    }
    
    public void render(E3D e3d, World world) {
        spr.setMatrix(pos, null, world.m, e3d.invCam);
        spr.prepareRender(e3d);
    }

}
