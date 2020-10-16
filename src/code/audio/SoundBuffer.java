package code.audio;

import code.utils.CachedContent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;

/**
 *
 * @author Roman Lahin
 */
public class SoundBuffer extends CachedContent {
    
    public int id;
    
    public SoundBuffer(int id) {
        this.id = id;
    }
    
    public static SoundBuffer createBuffer(String file) {
        // Load wav data into a buffer.
        int soundBuffer = AL10.alGenBuffers();

        if(AL10.alGetError() != AL10.AL_NO_ERROR) return null;

        ByteBuffer bruh;
        try {
            FileInputStream is = new FileInputStream(new File("data", file));
            DataInputStream dis = new DataInputStream(is);
            byte[] data = new byte[dis.available()];
            dis.readFully(data);
            dis.close();
            bruh = BufferUtils.createByteBuffer(data.length);
            bruh.put(data);
            bruh.rewind();
        } catch (Exception e) {
            e.printStackTrace();
            AL10.alDeleteBuffers(soundBuffer);
            return null;
        }
        
        IntBuffer sampleRate = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        
        ShortBuffer decoded = STBVorbis.stb_vorbis_decode_memory(bruh, channels, sampleRate);

        AL10.alBufferData(soundBuffer, 
                channels.get(0)==1?AL10.AL_FORMAT_MONO16:AL10.AL_FORMAT_STEREO16, 
                decoded, sampleRate.get(0));
        
        return new SoundBuffer(soundBuffer);
    }

    public void destroy() {
        AL10.alDeleteBuffers(id);
    }
}
