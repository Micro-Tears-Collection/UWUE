package code.engine3d;

import code.Engine;
import code.utils.ReusableContent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Texture extends ReusableContent {
    
    public int w = 1, h = 1;
    public int id;
    
    public Texture(int id) {
        this.id = id;
    }
    
    public static Texture createTexture(String name) {
        try {
            File file = new File("data", name);
            
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

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w[0], h[0], 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, img);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            
            MemoryUtil.memFree(img);
            
            tex.w = w[0];
            tex.h = h[0];
            
            return tex;
        } catch (Exception e) {
            Engine.printError(e);
        }
        
        return null;
    }

    public void destroy() {
        GL11.glDeleteTextures(id);
    }
}
