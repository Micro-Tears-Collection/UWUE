package code.engine3d;

import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Texture extends ReusableContent {
    
    public static int oldLevel = 0;
    
    public int w = 1, h = 1;
    public int id;
    
    private Texture(int id) {
        this.id = id;
    }
    
    public void bind(boolean linearInterpolation, boolean mipMapping, boolean wrapClamp, int level) {
        if(level != oldLevel) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0+level);
            oldLevel = level;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        
        int mag = linearInterpolation ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        int interp = mipMapping ?
                (linearInterpolation ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_NEAREST_MIPMAP_LINEAR)
                : mag;
        int wrap = wrapClamp?GL11.GL_CLAMP:GL11.GL_REPEAT;

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interp);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mag);
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
    }
    
    public void unbind(int level) {
        if(level != oldLevel) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0+level);
            oldLevel = level;
        }
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void destroy() {
        GL11.glDeleteTextures(id);
    }
    
    public static Texture get(String name) {
        Texture tex = (Texture) AssetManager.get("TEX_" + name);
        if(tex != null) {
            tex.use();
            return tex;
        }
        
        if(name.equals("null")) {
            tex = new Texture(0);
            tex.lock();
        } else {
            tex = loadTexture(name);
        }
        
        if(tex != null) {
            AssetManager.addReusable("TEX_" + name, tex);
            return tex;
        }
        
        return get("null");
    }
    
    
    /**
     * Should be destroyed after using!
     * @param path Path to texture
     * @return Texture or null
     */
    public static Texture loadTexture(String path) {
        try {
            File file = new File("data", path);
            
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

            int id = GL11.glGenTextures();
            Texture tex = new Texture(id);
            
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE); 
            
            boolean hasAlpha = false;
            for(int i=w[0]*h[0]*4-1; i>=0; i-=4) {
                if((img.get(i)&0xff) < 0xff) {
                    hasAlpha = true;
                    break;
                }
            }
            
            int textureFormat = hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB;

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, textureFormat, w[0], h[0], 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, img);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            
            MemoryUtil.memFree(img);
            
            tex.w = w[0];
            tex.h = h[0];
            
            return tex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static Texture createTexture(int w, int h) {
        int tex = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_RGB8, 320, 240, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        
        return new Texture(tex);
    }
    
}
