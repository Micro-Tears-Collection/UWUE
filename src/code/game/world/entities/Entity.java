package code.game.world.entities;

import code.game.world.World;
import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class Entity extends ActivableObject {
    
    public Vector3D pos = new Vector3D();
    
    public String name, unicalID;
    
    public void update(World world) {}
    public void physicsUpdate(World world) {}
    public void collisionTest(Entity entity) {}
    
    public void render(World world) {}

}
