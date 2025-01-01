package code.game.world.entities;

import code.audio.SoundSource;
import code.game.Configuration;
import code.game.world.World;
import code.math.Vector3D;
import code.utils.FPS;

import code.utils.Keys;
import java.util.Random;

/**
 *
 * @author Roman Lahin
 */
public class Player extends PhysEntity {
    
    public static int WALK_FORWARD, WALK_BACKWARD, STRAFE_LEFT, STRAFE_RIGHT, JUMP, RUN, CROUCH, NOCLIP_TOGGLE,
            INTERACT;
    
    public float eyeHeight;
    public float rotX;
	
	private float distanceToStep = 0;
	private SoundSource[] steps;
	private int stepIndex = 0;
	
	private final float stepLength;
	
	private float fallHeight = 0;
	
	private Random r = new Random();
    
    public Player() {
        name = "player";
        setSize(25, 180);
        eyeHeight = 180-15;
		
		stepLength = 180 * 0.415f * 2;
		distanceToStep = stepLength;
		
		steps = new SoundSource[] {
			new SoundSource("/sounds/stone/step1.ogg"),
			new SoundSource("/sounds/stone/step2.ogg"),
			new SoundSource("/sounds/stone/step3.ogg"),
			new SoundSource("/sounds/stone/step4.ogg")
		};
		
		for(SoundSource step : steps) {
			//step.set3D(false);
			step.setVolume(0.35f);
			//step.setDistance(eyeHeight, Float.MAX_VALUE, false);
			step.setSoundType(Configuration.FOOTSTEP);
		}
    }
	
	public void destroy() {
		for(SoundSource step : steps) {
			step.destroy();
		}
		
		r = null;
		
		super.destroy();
	}
    
    public void update(World world) {
        float speed = 12.75f * (Keys.isPressed(RUN) ? 1.7f : 1);//Keys.isPressed(RUN) ? 25.5f : 19;
		//40 and 30 is release speed
        //35 is pre release speed
		
		if(!noclip) {
			walk(
				((Keys.isPressed(WALK_FORWARD)?1:0) - (Keys.isPressed(WALK_BACKWARD)?1:0)) * speed,
				((Keys.isPressed(STRAFE_RIGHT)?1:0) - (Keys.isPressed(STRAFE_LEFT)?1:0)) * speed
			);
		} else {
			speed = 19;
			if(Keys.isPressed(RUN)) speed *= 5;
			if(Keys.isPressed(CROUCH)) speed /= 5;
			
			float front = ((Keys.isPressed(WALK_FORWARD)?1:0) - (Keys.isPressed(WALK_BACKWARD)?1:0)) * speed;
			float right = ((Keys.isPressed(STRAFE_RIGHT)?1:0) - (Keys.isPressed(STRAFE_LEFT)?1:0)) * speed;

			float sin = (float) Math.sin(Math.toRadians(rotY));
			float cos = (float) Math.cos(Math.toRadians(rotY));

			float upSin = (float) Math.sin(Math.toRadians(rotX));
			float upCos = (float) Math.cos(Math.toRadians(rotX));

			if(front != 0 && right != 0) {
				//1 / (2^0.5) ~= 0.70710678
				front *= 0.70710678;
				right *= 0.70710678;
			}

			this.speed.x += (-sin * front * upCos + cos * right) * FPS.frameTime / 50f;
			this.speed.z += (-cos * front * upCos - sin * right) * FPS.frameTime / 50f;
			this.speed.y += front * upSin * FPS.frameTime / 50f;
		}
        
        if(Keys.isPressed(JUMP)) jump(50);
        
        super.update(world);
    }
	
	public void physicsUpdate(World world) {
		Vector3D oldPos = new Vector3D(pos);
		boolean oldOnGround = onGround;
		
		super.physicsUpdate(world);
		
		if(onGround) distanceToStep -= Math.sqrt((oldPos.x - pos.x) * (oldPos.x - pos.x) + (oldPos.z - pos.z) * (oldPos.z - pos.z));
		
		if(!oldOnGround && onGround && fallHeight > 20) distanceToStep = 0;

		if(distanceToStep <= 0) {
			distanceToStep = stepLength;
			stepIndex = (stepIndex + 1) % steps.length;

			SoundSource step = steps[stepIndex];

			step.stop();
			step.setPitch(1 + r.nextFloat() * 0.2f - 0.1f);
			//step.setPosition(pos);
			step.play();
		}
		
		if(!noclip && !onGround) fallHeight += Math.max(0, oldPos.y - pos.y);
		else fallHeight = 0;
		
		/*for(SoundSource step : steps) {
			step.setPosition(pos);
			step.setSpeed(new Vector3D(speed.x, speed.y + 8F * FPS.frameTime / 50, speed.z));
		}*/
	}
    
    public static void initKeys(int w, int s, int a, int d, int space, int shift, int control, int v, int e) {
        WALK_FORWARD = Keys.addKeyToBinding(WALK_FORWARD, w);
        WALK_BACKWARD = Keys.addKeyToBinding(WALK_BACKWARD, s);
        STRAFE_LEFT = Keys.addKeyToBinding(STRAFE_LEFT, a);
        STRAFE_RIGHT = Keys.addKeyToBinding(STRAFE_RIGHT, d);
        JUMP = Keys.addKeyToBinding(JUMP, space);
        RUN = Keys.addKeyToBinding(RUN, shift);
        CROUCH = Keys.addKeyToBinding(CROUCH, control);
		
		NOCLIP_TOGGLE = Keys.addKeyToBinding(NOCLIP_TOGGLE, v);
        
        INTERACT = Keys.addKeyToBinding(INTERACT, e);
    }

}
