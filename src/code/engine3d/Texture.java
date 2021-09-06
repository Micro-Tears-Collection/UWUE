package code.engine3d;

import code.utils.assetManager.ReusableContent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;

import org.lwjgl.opengl.GL33C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Texture extends ReusableContent {
    
    private static int oldLevel = 0;
    
    public int w = 1, h = 1;
    public int id;
    
    Texture(int id, int w, int h) {
        this.id = id;
        this.w = w;
        this.h = h;
    }

    public void destroy() {
        GL33C.glDeleteTextures(id);
    }
	
	public void setParameters(E3D e3d, boolean linear, boolean mipMapping, boolean wrapClamp) {
		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, id);
        GL33C.glGetError();
        
        int mag = linear ? GL33C.GL_LINEAR : GL33C.GL_NEAREST;
        int min = mipMapping ?
                (linear ? GL33C.GL_LINEAR_MIPMAP_LINEAR : GL33C.GL_NEAREST_MIPMAP_LINEAR)
                : mag;
        int wrap = wrapClamp?GL33C.GL_CLAMP_TO_EDGE:GL33C.GL_REPEAT;

        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MIN_FILTER, min);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_MAG_FILTER, mag);
        
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_WRAP_T, wrap);
        GL33C.glTexParameteri(GL33C.GL_TEXTURE_2D, GL33C.GL_TEXTURE_WRAP_S, wrap);
			
		if(e3d.anisotropicSupported && mipMapping) 
			GL33C.glSamplerParameterf(id, 
					EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 
					e3d.maxAnisotropy);
		
		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);
        GL33C.glGetError();
	}
    
    public void bind(int level) {
        if(level != oldLevel) {
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0+level);
            oldLevel = level;
        }
		
        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, id);
        GL33C.glGetError();
    }
    
    public void unbind(int level) {
        if(level != oldLevel) {
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0+level);
            oldLevel = level;
        }
        
        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);
    }
    
    /**
     * Should be destroyed after using!
     * @param path Path to texture
     * @return Texture or null
     */
    public static Texture loadTexture(String path) {
        try {
            File file = new File("data", path);
			
			String format = file.getName();
			
			if(format.lastIndexOf('.') != -1) format = format.substring(format.lastIndexOf('.'));
			else format = null;
            
            if(!file.exists()) {
                System.out.println("No such file "+file.getAbsolutePath());
                return null;
            }
            
            FileInputStream is = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(is);
            byte[] data = new byte[dis.available()];
            dis.readFully(data);
            dis.close();
            ByteBuffer bruh = (ByteBuffer) MemoryUtil.memAlloc(data.length).put(data).rewind();
            
            int[] w = new int[1];
            int[] h = new int[1];
            int[] channels = new int[1];
            ByteBuffer img = STBImage.stbi_load_from_memory(bruh, w, h, channels, 4);
            MemoryUtil.memFree(bruh);

            int id = GL33C.glGenTextures();
            
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, id);
            GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 1);
            
            boolean hasAlpha = false;
            for(int i=w[0]*h[0]*4-1; i>=0; i-=4) {
                if((img.get(i)&0xff) < 0xff) {
                    hasAlpha = true;
                    break;
                }
            }
            
            int textureFormat = hasAlpha ? GL33C.GL_RGBA : GL33C.GL_RGB;
			if("norm".equals(format)) textureFormat = GL33C.GL_RG;

            GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, textureFormat, w[0], h[0], 0, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_BYTE, img);
            GL33C.glGenerateMipmap(GL33C.GL_TEXTURE_2D);
            GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);
            
            MemoryUtil.memFree(img);
            
            return new Texture(id, w[0], h[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Should be destroyed after using!
     * @param w Texture width
     * @param h Texture height
     * @return Texture
     */
    public static Texture createTexture(int w, int h) {
        int gltex = GL33C.glGenTextures();

        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, gltex);
        GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, GL33C.GL_RGB8, w, h, 0, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE, 0);
		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);
        
        return new Texture(gltex, w, h);
    }
    
}
