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
        tmp.add(0, spr.h*(spr.align+1)/2, 0);
        
        return Entity.rayCastSphere(ray, tmp, spr.w/2);
    }
    
    public void render(E3D e3d, World world) {
        spr.setMatrix(pos, null, world.m, e3d.invCam);
        spr.prepareRender(e3d);
    }

}
