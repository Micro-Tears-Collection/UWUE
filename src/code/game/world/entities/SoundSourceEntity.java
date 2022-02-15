package code.game.world.entities;

import code.audio.SoundSource;

import code.game.world.World;
import java.util.Random;

/**
 *
 * @author Roman Lahin
 */
public class SoundSourceEntity extends Entity {
    
	private static final Random r = new Random();
	
    public SoundSource source;
    
    public SoundSourceEntity(SoundSource source, boolean randomOffset) {
        this.source = source;
		
		if(randomOffset) source.rewindTo(source.buffer.getLength() * r.nextFloat());
    }
    
    public void destroy() {
        super.destroy();
        source.destroy();
        source = null;
    }
    
    /*public void play() {
        source.play();
    }
    
    public boolean isPlaying() {
        return source.isPlaying();
    }
    
    public void rewind() {
        source.rewind();
    }
    
    public void stop() {
        source.stop();
    }
    
    public void setLoop(boolean loop) {
        source.setLoop(loop);
    }
    
    public void setPitch(float pitch) {
        source.setPitch(pitch);
    }
    
    public void setVolume(float volume) {
        source.setVolume(volume);
    }
    
    public boolean getLoop() {
        return source.getLoop();
    }
    
    public float getPitch() {
        return source.getPitch();
    }
    
    public float getVolume() {
        return source.getVolume();
    }
    
    public void set3D(boolean use3D) {
        source.set3D(use3D);
    }*/
    
    public void physicsUpdate(World world) {
        super.physicsUpdate(world);
        source.setPosition(pos);
    }

}
