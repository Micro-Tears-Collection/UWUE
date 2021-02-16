package code.game.world.entities;

import code.engine3d.E3D;
import code.engine3d.Mesh;

import code.game.world.World;

import code.math.Ray;
import code.math.RayCast;
import code.math.Sphere;
import code.math.SphereCast;
import code.math.Vector3D;

import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class MeshObject extends PhysEntity {
    
    public Mesh mesh;
    public boolean meshCollision = true;
    public boolean visible = true;
    
    public MeshObject(Mesh[] meshes) {
        this.mesh = meshes[0];
        setSize(Math.max(Math.max(mesh.max.z,-mesh.min.z), Math.max(mesh.max.x,-mesh.min.x)), Math.max(0, mesh.max.y));
        physics = false;
        pushable = false;
        canPush = false;
    }
    
    public void physicsUpdate(World world) {
        super.physicsUpdate(world);
    }
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(!meshCollision) return super.rayCast(ray, onlyMeshes);
        
        mesh.setTransformation(pos, new Vector3D(0, rotY, 0));
        if(RayCast.isRayAABBCollision(ray, 
                mesh.min.x, mesh.min.y,  mesh.min.z, 
                mesh.max.x, mesh.max.y,  mesh.max.z)) {
            RayCast.rayCast(mesh, mesh.modelMatrix, ray);
            
            if(ray.mesh == mesh) return true;
        }
        
        return false;
    }
    
    public boolean meshSphereCast(Sphere sphere) {
        if(!meshCollision) return false;
        
        mesh.setTransformation(pos, new Vector3D(0, rotY, 0));
        if(SphereCast.isSphereAABBCollision(sphere, 
                mesh.min.x, mesh.min.y,  mesh.min.z, 
                mesh.max.x, mesh.max.y,  mesh.max.z)) {
            SphereCast.sphereCast(mesh, mesh.modelMatrix, sphere);
            
            if(sphere.mesh == mesh) return true;
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
            mesh.prepareRender(e3d);
        }
    }

}
