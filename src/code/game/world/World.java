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
    
    ArrayList<Entity> objects;
    ArrayList<Node> renderNodes;
    MeshInstance[] allMeshes, skybox;
    int skyColor;
    
    public static final int LINEAR = 1, EXP = 2;
    int fogMode = 0;
    float[] fogColor;
    float fogStart, fogEnd, fogDensity;
    
    public boolean fallDeath = true;
    
    long renderTime;
    SpriteObject sobj;
    
    public World(E3D e3d, MeshInstance[] meshes, int skyColor, MeshInstance[] skybox, boolean debug) {
        allMeshes = meshes;
        makeNodes();
        
        this.skybox = skybox;
        this.skyColor = skyColor;
        
        objects = new ArrayList<Entity>();
        
        if(debug) {
            sobj = new SpriteObject(new Sprite(e3d.getMaterial("/images/test.png;alpha_test=1;lightgroup=0"), 
                    false, 20, 20, Sprite.CENTER));
        }
        
        m = new Matrix4f();
        tmp = MemoryUtil.memAllocFloat(4*4);
    }

    public void destroy() {
        for(MeshInstance mesh : allMeshes) mesh.destroy();
        if(skybox != null) for(MeshInstance mesh : skybox) mesh.destroy();
        
        for(Entity obj : objects) {
            if(!(obj instanceof Player)) obj.destroy();
        }
        
        sobj.destroy();
        MemoryUtil.memFree(tmp);
        m = null;
        tmp = null;
    }
    
    void makeNodes() {
        renderNodes = new ArrayList<Node>();
        
        Node[] allNodes = new Node[allMeshes.length];
        for(int i=0; i<allNodes.length; i++) {
            allNodes[i] = new Node(allMeshes[i]);
        }
        
        for(int i=0; i<allNodes.length; i++) {
            Node node = allNodes[i];
            MeshInstance mesh = node.mesh;
            
            float minSize = Float.MAX_VALUE;
            Node selected = null;
            for(int x=0; x<allNodes.length; x++) {
                if(x == i) continue;
                Node node2 = allNodes[x];
                if(node.hasChild(node2)) continue;
                MeshInstance mesh2 = node2.mesh;
                
                if(mesh.min.x < mesh2.min.x || mesh.max.x > mesh2.max.x ||
                        mesh.min.y < mesh2.min.y || mesh.max.y > mesh2.max.y ||
                        mesh.min.z < mesh2.min.z || mesh.max.z > mesh2.max.z) continue;
                
                float size = 
                        (mesh2.max.x - mesh2.min.x) * 
                        (mesh2.max.y - mesh2.min.y) * 
                        (mesh2.max.z - mesh2.min.z);
                
                if(size < minSize) {
                    minSize = size;
                    selected = node2;
                }
            }
            
            if(selected != null) {
                selected.childs.add(node);
            } else renderNodes.add(node);
            
        }
    }
    
    public void pausedAnimate(Entity except) {
        for(Entity object : objects) {
            if(object != except) object.animate(FPS.frameTime, true, null);
        }
    }
    
    public void update(Player player) {
        for(Entity object : objects) object.update(this);
        
        for(int i=0; i<objects.size(); i++) {
            Entity obj1 = objects.get(i);
            
            for(int j=i+1; j<objects.size(); j++) {
                Entity obj2 = objects.get(j);
                obj1.collisionTest(obj2);
            }
        }
        
        for(Entity object : objects) object.physicsUpdate(this);
        
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
        for(Node node : renderNodes) {
            node.sphereCast(sphere);
        }
        
        for(Entity obj : objects) {
            if(obj == skip) continue;
            obj.meshSphereCast(sphere);
        }
    }
    
    public Entity rayCast(Ray ray, boolean onlyMeshes) {
        return rayCast(ray, onlyMeshes, null);
    }

    public Entity rayCast(Ray ray, boolean onlyMeshes, Entity skip) {
        for(Node node : renderNodes) {
            node.rayCast(ray);
        }
        
        float minDist = ray.distance;
        Entity got = null;
        
        for(Entity obj : objects) {
            if(obj == skip) continue;
            
            if(obj.rayCast(ray, onlyMeshes) && ray.distance < minDist) {
                minDist = ray.distance;
                got = obj;
            }
        }
        
        return got;
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
        
        for(Node node : renderNodes) node.render(e3d, e3d.invCamf, this, renderTime);
        
        //Check objects
        for(Entity object : objects) object.render(e3d, this);
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
        
        for(Entity obj : objects) {
            if(obj.canBeActivated(got, ray, click)) {
                if(obj.activate(main)) break;
            }
        }
        
        ray.reset();
    }

    //copy pasting sucks but i just dont know
    public Entity findObjectToActivate(Player player, boolean click) {
        Entity toActivate = null;
        
        ray.start.set(player.pos);
        ray.start.add(0, player.eyeHeight, 0);
        ray.dir.setDirection(player.rotX, player.rotY);
        ray.dir.mul(1000, 1000, 1000); //hand distance lmao
        
        Entity got = rayCast(ray, false, player);
        
        for(Entity obj : objects) {
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
        for(Entity obj : objects) {
            if(name.equals(obj.unicalID) || name.equals(obj.name)) return obj;
        }
        
        return null;
    }

}
