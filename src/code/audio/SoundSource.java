package code.audio;

import code.math.Vector3D;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.SOFTDirectChannels;
import org.lwjgl.openal.SOFTDirectChannelsRemix;

public class SoundSource {
    public static final float defRefDist = 1, defMaxDist = 1000;

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
        soundSource = AL10.alGenSources();
        
        set3D(true);
        setVolume(volume);
        
        //AL10.alSourcef(soundSource, AL10.AL_ROLLOFF_FACTOR, 0.01f);
        //AL10.alSourcef(soundSource, AL10.AL_ROLLOFF_FACTOR, 0.4f);
        
        setDistance(defRefDist, defMaxDist);
    }
    
    public void destroy() {
        AudioEngine.sources.remove(this);
		stop();
        free();
        AL10.alDeleteSources(soundSource);
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
            AL10.alSourcei(soundSource, AL10.AL_BUFFER, buffer.id);
            soundName = file;
        } else AL10.alSourcei(soundSource, AL10.AL_BUFFER, 0);
    }
    
    public void setSoundType(int type) {
        soundType = type;
        setVolume(volume);
    }
    
    public int getSoundType() {
        return soundType;
    }

    public void setLoop(boolean loop) {
        AL10.alSourcei(soundSource, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    public boolean getLoop() {
        return AL10.alGetSourcei(soundSource, AL10.AL_LOOPING) == AL10.AL_TRUE;
    }

    public void setVolume(float gain) {
        volume = gain;
        AL10.alSourcef(soundSource, AL10.AL_GAIN, gain*AudioEngine.getSoundTypeVolume(soundType));
    }

    public float getVolume() {
        return volume;
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(soundSource, AL10.AL_PITCH, pitch);
    }

    public float getPitch() {
        return AL10.alGetSourcef(soundSource, AL10.AL_PITCH);
    }
    
    public void setPosition(Vector3D pos) {
        if(!use3D) return;
        
        sourcePos[0] = pos.x; sourcePos[1] = pos.y; sourcePos[2] = pos.z;
        AL10.alSourcefv(soundSource, AL10.AL_POSITION, sourcePos);
    }
    
    public void setSpeed(Vector3D speed) {
        if(!use3D) return;
        
        sourceSpeed[0] = speed.x; sourceSpeed[1] = speed.y; sourceSpeed[2] = speed.z;
        AL10.alSourcefv(soundSource, AL10.AL_VELOCITY, sourceSpeed);
    }
    
    public void setDistance(float reference, float max) {
        AL10.alSourcef(soundSource, AL10.AL_REFERENCE_DISTANCE, reference);
        AL10.alSourcef(soundSource, AL10.AL_MAX_DISTANCE, max);
        //AL10.alSourcef(soundSource, AL10.AL_REFERENCE_DISTANCE, 100);
    }
    
    public void set3D(boolean use3D) {
        this.use3D = use3D;
        
        AL11.alSourcei(
			soundSource, 
			SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, 
			use3D ? SOFTDirectChannelsRemix.AL_REMIX_UNMATCHED_SOFT : AL11.AL_FALSE
		);
        
        if(Audio3DEffects.auxEffectSlot != 0) {
            AL11.alSource3i(soundSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, 
                    use3D?Audio3DEffects.auxEffectSlot:EXTEfx.AL_EFFECTSLOT_NULL, 0, EXTEfx.AL_FILTER_NULL);
        }
    }

    public void play() {
        AL10.alSourcePlay(soundSource);
    }

    public void pause() {
        AL10.alSourcePause(soundSource);
    }

    public void stop() {
        AL10.alSourceStop(soundSource);
    }
    
    public boolean isPlaying() {
        int[] out = new int[1];
        AL10.alGetSourcei(soundSource, AL10.AL_SOURCE_STATE, out);
        return out[0] == AL10.AL_PLAYING;
    }
    
    public void rewind() {
        AL10.alSourceRewind(soundSource);
        AL10.alSourcePlay(soundSource);
        }
    
    public void free() {
        AL10.alSourcei(soundSource, AL10.AL_BUFFER, 0);
        if(buffer != null) buffer.free();
        buffer = null;
    }

    public int getID() {
        return soundSource;
    }
}