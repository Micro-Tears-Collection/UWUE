package code.game.world;

import code.audio.AudioEngine;

import code.engine3d.E3D;
import code.engine3d.instancing.Sprite;

import code.game.Main;
import code.game.world.entities.Entity;
import code.game.world.entities.Player;
import code.game.world.entities.SpriteObject;

import code.math.collision.Ray;
import code.math.collision.Sphere;
import code.engine3d.instancing.MeshInstance;
import code.math.Culling;
import code.math.Vector3D;

import code.utils.FPS;
import java.nio.FloatBuffer;

import java.util.ArrayList;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class World {
    
    public Matrix4f m; 
    public FloatBuffer tmp;
    
    private ArrayList<Entity> entities;
    Node node;
    MeshInstance[] allMeshes, skybox;
    int skyColor;
    
    public static final int LINEAR = 1, EXP = 2;
    int fogMode = 0;
    float[] fogColor;
    float fogStart, fogEnd, fogDensity;
	
	public float drawDistance = 40000;
    public boolean fallDeath = true;
    
    long renderTime;
    SpriteObject sobj;
    
    public World(E3D e3d, MeshInstance[] meshes, int skyColor, MeshInstance[] skybox, boolean debug) {
        allMeshes = meshes;
        makeNodes();
        
        this.skybox = skybox;
        this.skyColor = skyColor;
        
        entities = new ArrayList<Entity>();
        
        if(debug) {
            sobj = new SpriteObject(new Sprite(e3d.getMaterial("/images/test;alpha_test=1;lightgroup=0", null), 
                    false, 20, 20, Sprite.CENTER));
        }
        
        m = new Matrix4f();
        tmp = MemoryUtil.memAllocFloat(4*4);
    }

    public void destroy() {
        for(MeshInstance mesh : allMeshes) mesh.destroy();
        if(skybox != null) for(MeshInstance mesh : skybox) mesh.destroy();
        
        for(Entity obj : entities) {
            if(!(obj instanceof Player)) obj.destroy();
        }
		
		node.destroy();
        
        if(sobj != null) sobj.destroy();
        MemoryUtil.memFree(tmp);
        m = null;
        tmp = null;
    }
    
    void makeNodes() {
		Vector3D min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3D max = new Vector3D(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		for(MeshInstance mesh : allMeshes) {
			min.min(mesh.min);
			max.max(mesh.max);
		}
		
		min.sub(200, 200, 200);
		max.add(200, 200, 200);
		
		node = new Node(null, min, max, 1000);
		
		for(MeshInstance mesh : allMeshes) {
			node.addMesh(mesh);
		}
    }
	
	public void addEntity(Entity entity) {
		entities.add(entity);
		entity.node = node.addEntity(entity);
	}
	
    public void pausedAnimate(Entity except) {
        for(Entity object : entities) {
            if(object != except) object.animate(FPS.frameTime, true, null);
        }
    }
    
    public void update(Player player) {
        for(Entity object : entities) object.update(this);
        
        for(int i=0; i<entities.size(); i++) {
            Entity obj1 = entities.get(i);
            
			node.collisionTest(obj1);
        }
        
        for(Entity entity : entities) {
			entity.physicsUpdate(this);
			entity.node = entity.node.addEntity(entity);
		}
        
        updateListener(player);
    }
    
    public static void updateListener(Player player) {
        player.pos.add(0, player.eyeHeight, 0);
        player.speed.add(0, 8F * FPS.frameTime / 50, 0);
        AudioEngine.setListener(player.pos, player.speed, player.rotY);
        player.pos.sub(0, player.eyeHeight, 0);
        player.speed.sub(0, 8F * FPS.frameTime / 50, 0);
    }

    public void sphereCast(Sphere sphere) {
        sphereCast(sphere, null);
    }

    public void sphereCast(Sphere sphere, Entity skip) {
        node.sphereCast(sphere, skip);
    }
    
    public Entity rayCast(Ray ray, boolean onlyMeshes) {
        return rayCast(ray, onlyMeshes, null);
    }

    public Entity rayCast(Ray ray, boolean onlyMeshes, Entity skip) {
        return node.rayCast(ray, onlyMeshes, skip);
    }
    
    public void render(E3D e3d, int w, int h) {
        e3d.prepare3D(0, 0, w, h);
        
        e3d.clearZbuffer();
        e3d.clearColor(skyColor);
        
        //Draw skybox
        if(skybox != null) {
            m.set(e3d.invCam);
            m.setTranslation(0, 0, 0);
            m.get(tmp);

            for(MeshInstance mesh : skybox) {
                mesh.fastIdentityCamera(tmp);
                mesh.animate(renderTime, true);
                mesh.renderImmediate(e3d);
            }
            
            e3d.clearZbuffer();
        }
        
        if(fogMode == 0) e3d.disableFog();
        else if(fogMode == LINEAR) e3d.setLinearFog(fogStart, fogEnd, fogColor);
        else if(fogMode == EXP) e3d.setExpFog(fogDensity, fogColor);
        
        //Check all location meshes
        Culling.set(e3d.invCamf, e3d.projf);
        
		//Render world + objects using octree
        node.render(e3d, e3d.invCamf, this, renderTime);
		
        if(sobj != null) sobj.render(e3d, this);
        
        e3d.postRender();
        renderTime += FPS.frameTime;
    }
    
    static final Ray ray = new Ray();

    public void activateObject(Main main, Player player, boolean click) {
        ray.start.set(player.pos);
        ray.start.add(0, player.eyeHeight, 0);
        ray.dir.setDirection(player.rotX, player.rotY);
        ray.dir.mul(1000, 1000, 1000); //hand distance lmao
        
        Entity got = rayCast(ray, false, player);
        
		//todo use octree
        for(Entity obj : entities) {
            if(obj.canBeActivated(got, ray, click)) {
                if(obj.activate(main)) break;
            }
        }
        
        ray.reset();
    }

    //todo copy pasting sucks but i just dont know
    public Entity findObjectToActivate(Player player, boolean click) {
        Entity toActivate = null;
        
        ray.start.set(player.pos);
        ray.start.add(0, player.eyeHeight, 0);
        ray.dir.setDirection(player.rotX, player.rotY);
        ray.dir.mul(1000, 1000, 1000); //hand distance lmao
        
        Entity got = rayCast(ray, false, player);
        
		//todo use octree
        for(Entity obj : entities) {
            if(obj.canBeActivated(got, ray, click)) {
                toActivate = obj;
                break;
            }
        }
        
        ray.reset();
        
        return toActivate;
    }
    
    public void debugPos(Player player) {
        ray.start.set(player.pos);
        ray.start.add(0, player.eyeHeight, 0);
        ray.dir.setDirection(player.rotX, player.rotY);
        ray.dir.mul(100000, 100000, 100000);
        
        rayCast(ray, true, player);
        
        if(ray.collision) {
            sobj.pos.set(ray.dir);
            sobj.pos.setLength(ray.distance);
            sobj.pos.add(ray.start.x, ray.start.y, ray.start.z);
            System.out.println(sobj.pos.x+", "+sobj.pos.y+", "+sobj.pos.z);
        }
        
        ray.reset();
    }
    
    public Entity findObject(String name) {
		//todo hashmap?
        for(Entity obj : entities) {
            if(name.equals(obj.unicalID) || name.equals(obj.name)) return obj;
        }
        
        return null;
    }

}
