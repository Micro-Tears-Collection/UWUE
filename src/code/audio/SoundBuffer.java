package code.audio;

import code.Engine;
import code.utils.ReusableContent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    
    public int id;
    
    public SoundBuffer(int id) {
        this.id = id;
    }
    
    public static SoundBuffer createBuffer(String file) {
        // Load wav data into a buffer.
        int soundBuffer = AL10.alGenBuffers();

        int err;
        if((err = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            System.out.println("alGenBuffers error " + err);
            return null;
        }

        ByteBuffer bruh = null;
        try {
            FileInputStream is = new FileInputStream(new File("data", file));
            DataInputStream dis = new DataInputStream(is);
            byte[] data = new byte[dis.available()];
            dis.readFully(data);
            dis.close();
            bruh = MemoryUtil.memAlloc(data.length);
            bruh.put(data);
            bruh.rewind();
        } catch (Exception e) {
            e.printStackTrace();
            AL10.alDeleteBuffers(soundBuffer);
            if(bruh != null) MemoryUtil.memFree(bruh);
            return null;
        }
        
        IntBuffer sampleRate = MemoryUtil.memAllocInt(1);
        IntBuffer channels = MemoryUtil.memAllocInt(1);
        
        ShortBuffer decoded = STBVorbis.stb_vorbis_decode_memory(bruh, channels, sampleRate);
        MemoryUtil.memFree(bruh);

        AL10.alBufferData(soundBuffer, 
                channels.get(0)==1?AL10.AL_FORMAT_MONO16:AL10.AL_FORMAT_STEREO16, 
                decoded, sampleRate.get(0));
        MemoryUtil.memFree(sampleRate);
        MemoryUtil.memFree(channels);
        MemoryUtil.memFree(decoded);
        
        return new SoundBuffer(soundBuffer);
    }

    public void destroy() {
        AL10.alDeleteBuffers(id);
        
        int err;
        if((err = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            System.out.println("alDeleteBuffers error " + err);
        }
    }
}
