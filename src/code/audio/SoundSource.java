package code.audio;

import code.math.Vector3D;

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.SOFTDirectChannels;
import org.lwjgl.openal.SOFTDirectChannelsRemix;

public class SoundSource {
    public static final float MIN_LINEAR_DIST = 0, MAX_LINEAR_DIST = 1000;
	public static final float MIN_DIST = 100, MAX_DIST = Float.MAX_VALUE;
	public static final boolean LINEAR_DIST = false, CLAMP = false;

    public String soundName;
    public SoundBuffer buffer;
    
    private int soundSource;
    private int soundType;
    private float volume = 1;
    private static final float[] sourcePos = new float[3], sourceSpeed = new float[3];
    private boolean use3D = true;
    
    public SoundSource() {
        init();
    }
    
    public SoundSource(String file) {
        init();
        loadFile(file);
    }
    
    private void init() {
        // Bind the buffer with the source.
        AudioEngine.sources.add(this);
        soundSource = AL11.alGenSources();
        
        set3D(true);
        setVolume(volume);
        
        AL11.alSourcef(soundSource, AL11.AL_ROLLOFF_FACTOR, 1f);
		AL11.alSourcef(soundSource, EXTEfx.AL_AIR_ABSORPTION_FACTOR, 0.01f);
        
        setDistance(MIN_DIST, MAX_DIST, false, CLAMP);
    }
    
    public void destroy() {
        AudioEngine.sources.remove(this);
		stop();
        free();
        AL11.alDeleteSources(soundSource);
    }
    
    public SoundSource beMusicPlayer() {
        setLoop(true);
        set3D(false);
        
        return this;
    }

    public void loadFile(String file) {
        soundName = null;
        
        if(buffer != null) free();
        buffer = SoundBuffer.get(file);
        if(buffer != null) {
            buffer.use();
            AL11.alSourcei(soundSource, AL11.AL_BUFFER, buffer.id);
            soundName = file;
        } else AL11.alSourcei(soundSource, AL11.AL_BUFFER, 0);
    }
    
    public void setSoundType(int type) {
        soundType = type;
        setVolume(volume);
    }
    
    public int getSoundType() {
        return soundType;
    }

    public void setLoop(boolean loop) {
        AL11.alSourcei(soundSource, AL11.AL_LOOPING, loop ? AL11.AL_TRUE : AL11.AL_FALSE);
    }

    public boolean getLoop() {
        return AL11.alGetSourcei(soundSource, AL11.AL_LOOPING) == AL11.AL_TRUE;
    }

    public void setVolume(float gain) {
        volume = gain;
        AL11.alSourcef(soundSource, AL11.AL_GAIN, gain*AudioEngine.getSoundTypeVolume(soundType));
    }

    public float getVolume() {
        return volume;
    }

    public void setPitch(float pitch) {
        AL11.alSourcef(soundSource, AL11.AL_PITCH, pitch);
    }

    public float getPitch() {
        return AL11.alGetSourcef(soundSource, AL11.AL_PITCH);
    }
    
    public void setPosition(Vector3D pos) {
        if(!use3D) return;
        
        sourcePos[0] = pos.x; sourcePos[1] = pos.y; sourcePos[2] = pos.z;
        AL11.alSourcefv(soundSource, AL11.AL_POSITION, sourcePos);
    }
    
    public void setSpeed(Vector3D speed) {
        if(!use3D) return;
        
        sourceSpeed[0] = speed.x; sourceSpeed[1] = speed.y; sourceSpeed[2] = speed.z;
        AL11.alSourcefv(soundSource, AL11.AL_VELOCITY, sourceSpeed);
    }
    
    public void setDistance(float min, float max, boolean linear, boolean clamp) {
        AL11.alSourcef(soundSource, AL11.AL_REFERENCE_DISTANCE, min);
        AL11.alSourcef(soundSource, AL11.AL_MAX_DISTANCE, max);
		
		AL11.alSourcei(soundSource, AL11.AL_DISTANCE_MODEL, 
			clamp ? 
			(linear ? AL11.AL_LINEAR_DISTANCE_CLAMPED : AL11.AL_INVERSE_DISTANCE_CLAMPED)
			:
			(linear ? AL11.AL_LINEAR_DISTANCE : AL11.AL_INVERSE_DISTANCE)
		);
    }
    
	//todo fix mono sources
    public void set3D(boolean use3D) {
        this.use3D = use3D;
		
		AL11.alSourcei(soundSource, AL11.AL_SOURCE_RELATIVE, use3D?AL11.AL_FALSE:AL11.AL_TRUE);
        
        if(!use3D) {
            sourcePos[0] = sourcePos[1] = sourcePos[2] = 0;
            AL11.alSourcefv(soundSource, AL11.AL_POSITION, sourcePos);
            sourceSpeed[0] = sourceSpeed[1] = sourceSpeed[2] = 0;
            AL11.alSourcefv(soundSource, AL11.AL_VELOCITY, sourceSpeed);
        }
        
        AL11.alSourcei(
			soundSource, 
			SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, 
			use3D ? AL11.AL_FALSE : SOFTDirectChannelsRemix.AL_REMIX_UNMATCHED_SOFT
		);
        
        if(Audio3DEffects.auxEffectSlot != 0) {
            AL11.alSource3i(soundSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, 
                    use3D?Audio3DEffects.auxEffectSlot:EXTEfx.AL_EFFECTSLOT_NULL, 0, EXTEfx.AL_FILTER_NULL);
        }
    }

    public void play() {
        AL11.alSourcePlay(soundSource);
    }

    public void pause() {
        AL11.alSourcePause(soundSource);
    }

    public void stop() {
        AL11.alSourceStop(soundSource);
    }
    
    public boolean isPlaying() {
        int[] out = new int[1];
        AL11.alGetSourcei(soundSource, AL11.AL_SOURCE_STATE, out);
        return out[0] == AL11.AL_PLAYING;
    }
    
    public void rewind() {
        AL11.alSourceRewind(soundSource);
        AL11.alSourcePlay(soundSource);
    }
    
    public void free() {
        AL11.alSourcei(soundSource, AL11.AL_BUFFER, 0);
        if(buffer != null) buffer.free();
        buffer = null;
    }

    public int getID() {
        return soundSource;
    }

	public void rewindTo(float seconds) {
		AL11.alSourcef(soundSource, AL11.AL_SEC_OFFSET, seconds);
	}
}