package code.game.world.entities;

import code.game.world.World;
import code.math.MathUtils;
import code.math.Ray;
import code.math.Sphere;
import code.math.Vector3D;
import code.utils.FPS;

/**
 *
 * @author Roman Lahin
 */
public class PhysEntity extends Entity {
    
    public float hp = 100;
    public Vector3D speed = new Vector3D();
    public float rotY;
    public float radius, height;
    
    boolean onGround;
    
    public boolean physics = true, pushable = true, canPush = true;
    
    public void setSize(float radius, float height) {
        this.radius = radius;
        this.height = height;
    }
    
    public boolean rayCast(Ray ray, boolean onlyMeshes) {
        if(onlyMeshes) return false;
        
        Vector3D tmp = new Vector3D(pos);
        tmp.add(0, height-radius, 0);
        
        return Entity.rayCastSphere(ray, tmp, radius);
    }
    
    public boolean damage(int damage, Entity attacker) {
        hp -= damage;
        
        if(hp <= 0) {
            hp = 0;
            return true;
        }
        return false;
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
    
    public void physicsUpdate(World world) {
        if(physics) move(world);
        
        double horizFriction = onGround ? 0.546 : 0.61;
        double verticalFriction = 0.98;
        horizFriction = Math.pow(horizFriction, FPS.frameTime / 50d);
        verticalFriction = Math.pow(verticalFriction, FPS.frameTime / 50d);
        
        speed.x *= horizFriction;
        speed.y *= verticalFriction;
        speed.z *= horizFriction;
        
        //gravity
        speed.y -= 8F * FPS.frameTime / 50;
    }
    
    static final Vector3D tmp = new Vector3D(), tmp2 = new Vector3D();
    static final Sphere tmpSphere = new Sphere();
    
    private void move(World world) {
        tmp2.set(speed);
        tmp2.mul(FPS.frameTime, FPS.frameTime, FPS.frameTime);
        tmp2.div(50f, 50f, 50f);
        
        tmpSphere.radius = radius;
        tmpSphere.height = height;
        
        float flen = tmp2.length();
        float r = radius * 0.95f;
        int iter = 0;
        while(flen > 0) {
            tmp.set(tmp2);
            tmp.setLength(Math.min(flen, r));

            pos.add(tmp.x, tmp.y, tmp.z);

            tmpSphere.reset();
            tmpSphere.pos.set(pos.x, pos.y + height / 2, pos.z);

            world.sphereCast(tmpSphere, this);
            onGround = false;
            if(tmpSphere.collision) {
                if(tmpSphere.onFloor) {
                    onGround = true;
                    speed.y = 0;
                }
                pos.set(tmpSphere.pos.x, tmpSphere.pos.y - height / 2, tmpSphere.pos.z);
            }
            
            flen -= r;
            iter++;
            if(iter >= 10) break;
        }
    }

    public void collisionTest(Entity go) {
        if(go instanceof PhysEntity) collisionTest(this, (PhysEntity)go);
    }
    
    private static void collisionTest(PhysEntity c1, PhysEntity c2) {
        if((!c1.pushable || !c2.canPush) && (!c2.pushable || !c1.canPush)) return;
        
        Vector3D pos1 = c1.pos;
        Vector3D pos2 = c2.pos;
        float rSum = (c1.radius + c2.radius)/2;

        float dx = pos1.x - pos2.x;
        float dy = pos1.y - pos2.y;
        float dz = pos1.z - pos2.z;

        if( Math.abs(dx)>rSum ||
            Math.abs(dy)>rSum ||
            Math.abs(dz)>rSum ) return;

        float r = dx*dx + dy*dy + dz*dz;

        if (r < rSum*rSum) {
            if(r != 0) r = (float) Math.sqrt(r);
            else dx = 1f;

            int dis = (int) (rSum - r);
            Vector3D dir = new Vector3D(dx, dy, dz);
            dir.setLength(dis / 2);
            
            if(c1.pushable && c2.canPush) c1.speed.add(dir.x, dir.y, dir.z);
            if(c2.pushable && c1.canPush) c2.speed.add(-dir.x, -dir.y, -dir.z);
        }
    }
    
    public void jump(float height) {
        if(onGround) speed.y = height;
    }
    
    public void walk(float front, float right, float maxSpeed) {
        float sin = (float) Math.sin(Math.toRadians(rotY));
        float cos = (float) Math.cos(Math.toRadians(rotY));
        
        if(!onGround) {
            front *= 0.2;
            right *= 0.2;
        }
        
        speed.x += (-sin * front + cos * right) * FPS.currentTime / 50f;
        speed.z += (-cos * front - sin * right) * FPS.currentTime / 50f;
        
        float oy = speed.y; speed.y = 0;
        if(speed.lengthSquared() > maxSpeed * maxSpeed) speed.setLength(maxSpeed);
        speed.y = oy;
    }

}
