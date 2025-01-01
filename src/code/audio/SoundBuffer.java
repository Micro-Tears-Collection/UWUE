package code.audio;

import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;

import java.nio.ShortBuffer;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTLoopPoints;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisComment;
import org.lwjgl.stb.STBVorbisInfo;
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
        //Create OpenAL buffer
        int soundBuffer = AL10.alGenBuffers();

        int err;
        if((err = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            System.out.println("alGenBuffers error " + err);
            return null;
        }
		
        //Load vorbis data into a temporary buffer
		long vorbis = STBVorbis.stb_vorbis_open_filename(AssetManager.toGamePath(file), new int[1], null);
		
		STBVorbisInfo info = STBVorbisInfo.create();
		STBVorbis.stb_vorbis_get_info(vorbis, info);
        
		int limit = 4096 * info.channels();
        ShortBuffer samples = MemoryUtil.memAllocShort(limit);
		
		while(true) {
			int read = STBVorbis.stb_vorbis_get_frame_short_interleaved(vorbis, info.channels(), samples);
			if(read == 0) break;
			
			samples.position(samples.position() + read * info.channels());
			
			if(samples.position() + limit > samples.capacity()) {
				int newSize = samples.capacity() + Math.max(limit, samples.capacity() / info.channels() / 3 * info.channels());
				
				ShortBuffer tmpBuf = MemoryUtil.memAllocShort(newSize);
				
				samples.limit(samples.position());
				samples.position(0);
				tmpBuf.put(samples);
				
				MemoryUtil.memFree(samples);
				samples = tmpBuf;
			}
		}
		
		//Pass sample data to OpenAL buffer
		samples.limit(samples.position());
		samples.position(0);

        AL10.alBufferData(soundBuffer, 
                info.channels()==1?AL10.AL_FORMAT_MONO16:AL10.AL_FORMAT_STEREO16, 
                samples, info.sample_rate());
		
		float length = (float) samples.remaining() / info.sample_rate() / info.channels();
		
		//Read loop tags
		STBVorbisComment comments = STBVorbisComment.create();
		STBVorbis.stb_vorbis_get_comment(vorbis, comments);
		
		int loopStart = -1;
		int loopEnd = samples.remaining() / info.channels();
		int loopLength = -1;
		
		for(int i=0; i<comments.comment_list_length(); i++) {
			String comment = MemoryUtil.memASCII(comments.comment_list().get(i));
			
			try {
				if(comment.startsWith("LOOPSTART=")) {
					loopStart = Integer.parseInt(comment.substring(10));
				} else if(comment.startsWith("LOOPEND=")) {
					loopEnd = Integer.parseInt(comment.substring(8));
				} else if(comment.startsWith("LOOPLENGTH=")) {
					loopLength = Integer.parseInt(comment.substring(11));
				}
			} catch(NumberFormatException e) {}
		}
		
		if(loopStart >= 0) {
			if(loopLength >= 0) loopEnd = loopStart + loopLength;
			
			AL11.alBufferiv(soundBuffer, SOFTLoopPoints.AL_LOOP_POINTS_SOFT, new int[] {loopStart, loopEnd});
		}
		
		//Free memory
		STBVorbis.stb_vorbis_close(vorbis);
        MemoryUtil.memFree(samples);
        
        return new SoundBuffer(soundBuffer, length);
    }
}
