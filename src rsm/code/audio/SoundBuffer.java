package code.audio;

import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class SoundBuffer extends ReusableContent {
    
	private float length;
    int id;
    
    private SoundBuffer(int id, float length) {
        this.id = id;
		this.length = length;
    }

    public void destroy() {
        AL10.alDeleteBuffers(id);
        
        int err;
        if((err = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            System.out.println("alDeleteBuffers error " + err);
        }
    }
	
	public float getLength() {
		return length;
	}
    
    public static SoundBuffer get(String file) {
        SoundBuffer sound = (SoundBuffer)AssetManager.get("SOUNDBUFF_" + file);
        
        if(sound == null) {
            sound = loadBuffer(file);
            
            if(sound != null) {
                AssetManager.add("SOUNDBUFF_" + file, sound);
            }
        }
        
        return sound;
    }
    
    /**
     * Buffer should be destroyed after using!
     * @param file
     * @return SoundBuffer with loaded file or null
     */
    public static SoundBuffer loadBuffer(String file) {
        // Load vorbis data into a buffer.
        int soundBuffer = AL10.alGenBuffers();

        int err;
        if((err = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            System.out.println("alGenBuffers error " + err);
            return null;
        }
        
        IntBuffer sampleRate = MemoryUtil.memAllocInt(1);
        IntBuffer channels = MemoryUtil.memAllocInt(1);
        
        ShortBuffer decoded = STBVorbis.stb_vorbis_decode_filename(AssetManager.toGamePath(file), channels, sampleRate);

		if(decoded == null) {
			MemoryUtil.memFree(sampleRate);
			MemoryUtil.memFree(channels);
			return null;
		}

        AL10.alBufferData(soundBuffer, 
                channels.get(0)==1?AL10.AL_FORMAT_MONO16:AL10.AL_FORMAT_STEREO16, 
                decoded, sampleRate.get(0));
		
		float length = decoded.capacity() / (float)sampleRate.get(0) / (float)channels.get(0);
		
        MemoryUtil.memFree(sampleRate);
        MemoryUtil.memFree(channels);
        MemoryUtil.memFree(decoded);
        
        return new SoundBuffer(soundBuffer, length);
    }
}
