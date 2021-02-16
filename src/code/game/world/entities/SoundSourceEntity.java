package code.game.world.entities;

import code.audio.SoundSource;

import code.game.world.World;

/**
 *
 * @author Roman Lahin
 */
public class SoundSourceEntity extends Entity {
    
    public SoundSource source;
    
    public SoundSourceEntity(SoundSource source) {
        this.source = source;
    }
    
    /*public void play() {
        source.play();
    }
    
    public boolean isPlaying() {
        return source.isPlaying();
    }
    
    public void rewind() {
        source.stop();
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
    
    public void set3D(boolean use3D) {
        source.set3D(use3D);
    }*/
    
    public void physicsUpdate(World world) {
        super.physicsUpdate(world);
        source.setPosition(pos);
    }

}
