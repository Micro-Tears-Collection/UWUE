package code.game.world;

import code.engine3d.E3D;

import code.math.collision.Ray;
import code.math.collision.RayCast;
import code.math.collision.Sphere;
import code.math.collision.SphereCast;
import code.engine3d.instancing.MeshInstance;
import code.game.world.entities.Entity;
import code.game.world.entities.PhysEntity;

import code.math.Culling;
import code.math.Vector3D;
import java.nio.FloatBuffer;

import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class Node {
    
	Node parent;
	Vector3D min, max;
	Vector3D realMin, realMax;
    ArrayList<Node> childs;
    ArrayList<MeshInstance> meshes;
    ArrayList<Entity> entities;
	
	int totalMeshes;
	int totalEntities;
	int totalStuff;
    
    Node(Node parent, Vector3D min, Vector3D max, float minimalSize) {
		this.parent = parent;
		
        childs = new ArrayList<Node>();
        meshes = new ArrayList<MeshInstance>();
        entities = new ArrayList<Entity>();
		
		realMin = new Vector3D(min);
		realMax = new Vector3D(max);
		
		this.min = new Vector3D(min);
		this.max = new Vector3D(max);
		
		Vector3D size = new Vector3D(realMax);
		size.sub(realMin);
		
		this.min.sub(size.x / 2, size.y / 2, size.z / 2);
		this.max.add(size.x / 2, size.y / 2, size.z / 2);
		
		boolean splitX = size.x / 2 >= minimalSize,
				splitY = size.y / 2 >= minimalSize,
				splitZ = size.z / 2 >= minimalSize;
		
		if(splitX || splitY || splitZ) {
			Vector3D middle = new Vector3D(realMin);
			middle.add(realMax);
			middle.div(2, 2, 2);
			
			Vector3D maxtmp = new Vector3D(
					splitX ? middle.x : realMax.x,
					splitY ? middle.y : realMax.y,
					splitZ ? middle.z : realMax.z);
			
			for(int x=0; x<=(splitX ? 1 : 0); x++) {
				for(int y=0; y<=(splitY ? 1 : 0); y++) {
					for(int z=0; z<=(splitZ ? 1 : 0); z++) {
						Vector3D tmp = new Vector3D(realMin);
						Vector3D tmp2 = new Vector3D(maxtmp);
						
						if(x == 1) {
							tmp.x = middle.x;
							tmp2.x = realMax.x;
						}

						if(y == 1) {
							tmp.y = middle.y;
							tmp2.y = realMax.y;
						}

						if(z == 1) {
							tmp.z = middle.z;
							tmp2.z = realMax.z;
						}
						
						childs.add(new Node(this, tmp, tmp2, minimalSize));
					}
				}
			}
			
		}
		
    }
	
	void destroy() {
		parent = null;
		min = max = null;
		realMin = realMax = null;
		meshes = null;
		entities = null;
		
		for(Node node : childs) node.destroy();
		childs = null;
	}
	
	boolean isInside(Vector3D min, Vector3D max) {
		return this.min.x < min.x && this.min.y < min.y && this.min.z < min.z &&
				this.max.x > max.x && this.max.y > max.y && this.max.z > max.z;
	}
	
	boolean isIntersects(Vector3D min, Vector3D max) {
		return !(this.min.x > max.x || this.min.y > max.y || this.min.z > max.z ||
				this.max.x < min.x || this.max.y < min.y || this.max.z < min.z);
	}
	
	void addMesh(MeshInstance mesh) {
		totalMeshes++;
		totalStuff++;
		
		for(Node node : childs) {
			if(node.isInside(mesh.min, mesh.max)) {
				node.addMesh(mesh);
				return;
			}
		}
		
		meshes.add(mesh);
	}
	
	Node addEntity(Entity entity) {
		Vector3D eMin = entity.getMin(),
				eMax = entity.getMax();
		
		if(!isInside(eMin, eMax)) {
			if(parent != null) {
				if(entity.node == this) {
					entities.remove(entity);
					removeEntity();
				}
				
				return parent.addEntity(entity);
			}
			
		} else {
			for(Node node : childs) {
				if(node.isInside(eMin, eMax)) {
					if(entity.node == this) {
						entities.remove(entity);
						removeEntity();
					}

					return node.addEntity(entity);
				}
			}
			
		}
		
		if(entity.node != this) {
			entities.add(entity);
			addEntity();
		}
		
		return this;
	}
	
	private void removeEntity() {
		totalEntities--;
		totalStuff--;
		
		if(parent != null) parent.removeEntity();
	}
	
	private void addEntity() {
		totalEntities++;
		totalStuff++;
		
		if(parent != null) parent.addEntity();
	}
    //todo stuff checking when != 1 can lead to high culling checking on kids(maybe?)
    void render(E3D e3d, FloatBuffer invCam, World world, long renderTime) {
		if(totalStuff == 0) return;
		
		if(totalStuff != 1 && Culling.visible(min, max) == Culling.INVISIBLE) return;
		
		for(MeshInstance mesh : meshes) {
			mesh.fastIdentityCamera(invCam);
			if(Culling.visible(mesh.min, mesh.max) == Culling.INVISIBLE) continue;
			
			mesh.animate(renderTime, true);
			mesh.render(e3d);
		}
		
		for(Entity entity : entities) {
			entity.render(e3d, world);
		}
		
		for(Node node : childs) node.render(e3d, invCam, world, renderTime);
    }
    
    void sphereCast(Sphere sphere, Entity skip) {
		if(totalStuff == 0) return;
		
		if(totalStuff != 1 && !SphereCast.isSphereAABBCollision(sphere, min, max)) return;
		
		for(MeshInstance mesh : meshes) {
			if(!mesh.collision || 
					!SphereCast.isSphereAABBCollision(sphere, mesh.min, mesh.max)) continue;
		
			SphereCast.sphereCast(mesh.mesh,
					mesh.mesh.poses, mesh.mesh.normalsPerFace,
					sphere);
		}
		
		for(Entity entity : entities) {
			if(entity != skip) entity.meshSphereCast(sphere);
		}
		
		for(Node node : childs) node.sphereCast(sphere, skip);
    }
    
    Entity rayCast(Ray ray, boolean onlyMeshes, Entity skip) {
		if(totalStuff == 0) return null;
		
		if(totalStuff != 1 && !RayCast.isRayAABBCollision(ray, min, max)) return null;
		
		for(MeshInstance mesh : meshes) {
			if(!mesh.collision || 
					!RayCast.isRayAABBCollision(ray, mesh.min, mesh.max)) continue;
		
			RayCast.rayCast(mesh.mesh,
					mesh.mesh.poses, mesh.mesh.normalsPerFace,
					ray);
		}
		
		Entity got = null;
		float minDist = ray.distance;
		
		for(Entity entity : entities) {
			if(entity == skip) continue;
            
            if(entity.rayCast(ray, onlyMeshes) && ray.distance < minDist) {
                minDist = ray.distance;
                got = entity;
            }
		}
		
		for(Node node : childs) {
			Entity got2 = node.rayCast(ray, onlyMeshes, skip);
			if(got2 != null && ray.distance < minDist) got = got2;
		}
		
		return got;
    }
	
	void collisionTest(PhysEntity obj1) {
		if(totalEntities == 0) return;
		
		if((totalEntities == 1 || !entities.isEmpty()) && !isIntersects(obj1.getMin(), obj1.getMax())) return;
		
		for(Entity obj2 : entities) {
			if(obj1 != obj2) obj1.collisionTest(obj2);
		}
		
		for(Node node : childs) node.collisionTest(obj1);
	}

}
