package code.game.world.entities;

import code.game.world.World;

import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class Player extends PhysEntity {
    
    public static int WALK_FORWARD, WALK_BACKWARD, STRAFE_LEFT, STRAFE_RIGHT, JUMP, RUN,
            INTERACT;
    
    public float eyeHeight;
    public float rotX;
    
    public Player() {
        name = "player";
        setSize(25, 180);
        eyeHeight = 180-15;
    }
    
    public void update(World world) {
        float speed = Keys.isPressed(RUN) ? 40 : 30;
        //35
        
        walk(
            ((Keys.isPressed(WALK_FORWARD)?1:0) - (Keys.isPressed(WALK_BACKWARD)?1:0)) * speed,
            ((Keys.isPressed(STRAFE_RIGHT)?1:0) - (Keys.isPressed(STRAFE_LEFT)?1:0)) * speed,
            speed
        ); 
        
        if(Keys.isPressed(JUMP)) jump(50);
        
        super.update(world);
    }
    
    public static void initKeys(int w, int s, int a, int d, int space, int shift, int e) {
        WALK_FORWARD = Keys.addKeyToBinding(WALK_FORWARD, w);
        WALK_BACKWARD = Keys.addKeyToBinding(WALK_BACKWARD, s);
        STRAFE_LEFT = Keys.addKeyToBinding(STRAFE_LEFT, a);
        STRAFE_RIGHT = Keys.addKeyToBinding(STRAFE_RIGHT, d);
        JUMP = Keys.addKeyToBinding(JUMP, space);
        RUN = Keys.addKeyToBinding(RUN, shift);
        
        INTERACT = Keys.addKeyToBinding(INTERACT, e);
    }

}
