package code.game.world.entities;

import code.engine3d.E3D;
import code.engine3d.Model;

import code.game.world.World;

import code.math.collision.Ray;
import code.math.collision.RayCast;
import code.math.collision.Sphere;
import code.math.collision.SphereCast;
import code.engine3d.instancing.MeshInstance;
import code.math.Vector3D;

import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class MeshObject extends PhysEntity {
    
    public MeshInstance mesh;
    public boolean meshCollision = true;
    public boolean visible = true;
    
    public MeshObject(MeshInstance mesh) {
        this.mesh = mesh;
        setSize(Math.max(Math.max(mesh.max.z,-mesh.min.z), Math.max(mesh.max.x,-mesh.min.x)), Math.max(0, mesh.max.y));
        physics = false;
        pushable = false;
        canPush = false;
    }
    
    public void destroy() {
        super.destroy();
        mesh.destroy();
        mesh = null;
    }
    
    public void physicsUpdate(World world) {
        super.physicsUpdate(world);
    }
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(!meshCollision || !mesh.collision) return super.rayCast(ray, onlyMeshes);
        
        mesh.setTransformation(pos, new Vector3D(0, rotY, 0));
        if(RayCast.isRayAABBCollision(ray, 
                mesh.min.x, mesh.min.y,  mesh.min.z, 
                mesh.max.x, mesh.max.y,  mesh.max.z)) {
			
            RayCast.rayCast(mesh.mesh, 
					mesh.mesh.poses, mesh.mesh.normalsPerFace, 
					mesh.modelMatrix, ray);
            
            if(ray.mesh == mesh.mesh) return true;
        }
        
        return false;
    }
    
    public boolean meshSphereCast(Sphere sphere) {
        if(!meshCollision || !mesh.collision) return false;
        
        mesh.setTransformation(pos, new Vector3D(0, rotY, 0));
        if(SphereCast.isSphereAABBCollision(sphere, 
                mesh.min.x, mesh.min.y,  mesh.min.z, 
                mesh.max.x, mesh.max.y,  mesh.max.z)) {
			
            SphereCast.sphereCast(mesh.mesh, 
					mesh.mesh.poses, mesh.mesh.normalsPerFace,
					mesh.modelMatrix, sphere);
            
            if(sphere.mesh == mesh.mesh) return true;
        }
        
        return false;
    }
    
    public void update(World world) {
        animate(FPS.frameTime, false, null);
    }
    
    public void animate(long step, boolean paused, Entity teteAtete) {
        if(!paused || animateWhenPaused || teteAtete == this) mesh.animate(step, false);
    }
    
    public void render(E3D e3d, World world) {
        if(visible) {
            mesh.setTransformation(pos, new Vector3D(0, rotY, 0));
            mesh.setCamera(world.m, e3d.invCam);
            mesh.render(e3d);
        }
    }

}
