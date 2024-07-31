package code.game.world.entities;

import code.engine3d.E3D;
import code.engine3d.instancing.Sprite;

import code.game.world.World;

import code.math.collision.Ray;
import code.math.Vector3D;

import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class SpriteObject extends Entity {
    
    private Sprite spr;
	private Sprite spr2;
	private long animTime = 0;
	
    public boolean visible = true;
    
    public SpriteObject(Sprite spr, Sprite spr2) {
        this.spr = spr;
        this.spr2 = spr2;
    }
    
    public void destroy() {
        spr.destroy();
        spr = null;
        if(spr2 != null) spr2.destroy();
        spr2 = null;
        super.destroy();
    }
	
	public Vector3D getMin() {
		return new Vector3D(pos.x - spr.w / 2, pos.y + spr.h*spr.align/2, pos.z - spr.w / 2);
	}
	
	public Vector3D getMax() {
		return new Vector3D(pos.x + spr.w / 2, pos.y + spr.h*(2 - spr.align)/2, pos.z + spr.w / 2);
	}
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(onlyMeshes) return false;
        
        Vector3D tmp = new Vector3D(pos);
        tmp.add(0, spr.h*(spr.align+1)/2, 0);
        
        return Entity.rayCastSphere(ray, tmp, spr.w/2, spr.h);
    }
    
    public void update(World world) {
        animate(FPS.frameTime, false, null);
    }
    
    public void animate(long step, boolean paused, Entity teteAtete) {
        if(!paused || animateWhenPaused || teteAtete == this) {
			//spr.animate(step, false);
			if(spr2 != null) animTime = (animTime + step) % 500;
		}
    }
    
    public void render(E3D e3d, World world) {
        if(visible) {
			Sprite drawSpr = spr;
			if(spr2 != null & animTime >= 250) drawSpr = spr2;
			
            drawSpr.setTransformation(pos, null);
            drawSpr.setCamera(world.m, e3d.invCam);
            drawSpr.render(e3d);
        }
    }

}
