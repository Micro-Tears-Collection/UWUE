package code.engine3d;

import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.ARBTextureCompressionBPTC;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;

import org.lwjgl.opengl.GL33C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class Texture extends ReusableContent {
    
	public static boolean bptcSupported;
	private static boolean bptcChecked;
	
    private static int oldLevel = 0;
    
    public int w = 1, h = 1, d = 0;
    public int id;
	
	public boolean hdr;
    
    Texture(int id, int w, int h, int d) {
        this.id = id;
        this.w = w;
        this.h = h;
        this.d = d;
    }

    public void destroy() {
        GL33C.glDeleteTextures(id);
    }
	
	public void setParameters(E3D e3d, boolean linear, boolean mipMapping, boolean wrapClamp) {
		int texType = d == 0 ? GL33C.GL_TEXTURE_2D : GL33C.GL_TEXTURE_3D;
		
		GL33C.glBindTexture(texType, id);
        
        int mag = linear ? GL33C.GL_LINEAR : GL33C.GL_NEAREST;
        int min = mipMapping ?
                (linear ? GL33C.GL_LINEAR_MIPMAP_LINEAR : GL33C.GL_NEAREST_MIPMAP_LINEAR)
                : mag;
        int wrap = wrapClamp?GL33C.GL_CLAMP_TO_EDGE:GL33C.GL_REPEAT;

        GL33C.glTexParameteri(texType, GL33C.GL_TEXTURE_MIN_FILTER, min);
        GL33C.glTexParameteri(texType, GL33C.GL_TEXTURE_MAG_FILTER, mag);
        
        GL33C.glTexParameteri(texType, GL33C.GL_TEXTURE_WRAP_T, wrap);
        GL33C.glTexParameteri(texType, GL33C.GL_TEXTURE_WRAP_S, wrap);
			
		if(e3d.anisotropicSupported && mipMapping && d == 0) 
			GL33C.glSamplerParameterf(id, 
					EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 
					e3d.maxAnisotropy);
		
		GL33C.glBindTexture(texType, 0);
	}
	
	public void genMipmaps() {
		int texType = d == 0 ? GL33C.GL_TEXTURE_2D : GL33C.GL_TEXTURE_3D;
		
		GL33C.glBindTexture(texType, id);
		GL33C.glGenerateMipmap(texType);
		GL33C.glBindTexture(texType, 0);
	}
    
    public void bind(int level) {
        if(level != oldLevel) {
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0+level);
            oldLevel = level;
        }
		
        GL33C.glBindTexture(d == 0 ? GL33C.GL_TEXTURE_2D : GL33C.GL_TEXTURE_3D, id);
    }
    
    public void unbind(int level) {
        if(level != oldLevel) {
            GL33C.glActiveTexture(GL33C.GL_TEXTURE0+level);
            oldLevel = level;
        }
        
        GL33C.glBindTexture(d == 0 ? GL33C.GL_TEXTURE_2D : GL33C.GL_TEXTURE_3D, 0);
    }
    
    /**
     * Should be destroyed after using!
     * @param path Path to texture
     * @return Texture or null
     */
    public static Texture loadTexture(String path) {
		
		String name = path.substring(path.lastIndexOf('/') + 1);
		String format = null;
		if(name.lastIndexOf('.') != -1) {
			format = name.substring(name.lastIndexOf('.'));
			name = name.substring(0, name.lastIndexOf('.'));
		}
		
		String textureType = null;
		if(name.lastIndexOf('.') != -1) textureType = name.substring(name.lastIndexOf('.') + 1);

		int[] w = new int[1], h = new int[1];
		int[] channels = new int[1];
		
		ByteBuffer img = null;
		FloatBuffer imgf = null;
		
		if(".hdr".equalsIgnoreCase(format)) {
			imgf = STBImage.stbi_loadf(AssetManager.toGamePath(path), w, h, channels, 4);
		} else {
			img = STBImage.stbi_load(AssetManager.toGamePath(path), w, h, channels, 4);
		}
		
		if(img == null && imgf == null) {
			return null;
		}

		int texChannels = 1;
		if(img != null) {
			
			for(int i = 0; i < w[0] * h[0]; i++) {
				for(int rgba = texChannels; rgba < 4; rgba++) {
					
					int val = img.get(i * 4 + rgba) & 0xff;

					if(rgba == 1 && val != 0) texChannels = 2;
					else if(rgba == 2 && val != 0) texChannels = 3;
					else if(rgba == 3 && val < 255) texChannels = 4;
				}
				
				if(texChannels == 4) break;
			}
		} else {
			
			for(int i = 0; i < w[0] * h[0]; i++) {
				for(int rgba = texChannels; rgba < 4; rgba++) {
					
					float val = imgf.get(i * 4 + rgba);

					if(rgba == 1 && val != 0) texChannels = 2;
					else if(rgba == 2 && val != 0) texChannels = 3;
					else if(rgba == 3 && val < 1) texChannels = 4;
				}
				
				if(texChannels == 4) break;
			}
		}
		
		if(!bptcChecked) {
			bptcChecked = true;
			
			int extCount = GL33C.glGetInteger(GL33C.GL_NUM_EXTENSIONS);
			
			for(int i=0; i<extCount; i++) {
				String extension = GL33C.glGetStringi(GL33C.GL_EXTENSIONS, i);
				
				if(extension.equals("GL_ARB_texture_compression_bptc")) {
					//bptcSupported = true;
					break;
				}
			}
		}
		
		boolean useCompression = name != null && !name.startsWith("raw.");
		//System.out.println(path + " " + textureType + " " + useCompression);
		
		if("norm".equals(textureType) || "rg".equals(textureType)) {
			texChannels = Math.min(texChannels, 2);
		} else if("rough".equals(textureType) || "spec".equals(textureType) || "r".equals(textureType) || "height".equals(textureType)) {
			texChannels = Math.min(texChannels, 1);
		}
		
		int texFormat;
		
		if(img != null) {
			
			if(texChannels == 4) texFormat = useCompression ? GL33C.GL_COMPRESSED_RGBA : GL33C.GL_RGBA;
			else if(texChannels == 3) texFormat = useCompression ? GL33C.GL_COMPRESSED_RGB : GL33C.GL_RGB;
			else if(texChannels == 2) texFormat = useCompression ? GL33C.GL_COMPRESSED_RG : GL33C.GL_RG;
			else texFormat = useCompression ? GL33C.GL_COMPRESSED_RED : GL33C.GL_RED;
			
			if(bptcSupported && useCompression && texChannels >= 3 && texChannels <= 4) 
				texFormat = ARBTextureCompressionBPTC.GL_COMPRESSED_RGBA_BPTC_UNORM_ARB;
		} else {
			
			if(texChannels == 4) texFormat = GL33C.GL_RGBA16F;
			else if(texChannels == 3) texFormat = useCompression ? GL33C.GL_RGB9_E5 : GL33C.GL_RGB16F;
			else if(texChannels == 2) texFormat = GL33C.GL_RG16F;
			else texFormat = GL33C.GL_R16F;
			
			if(bptcSupported && useCompression && texChannels >= 2 && texChannels <= 3) 
				texFormat = ARBTextureCompressionBPTC.GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_ARB;
		}

		int id = GL33C.glGenTextures();

		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, id);
		GL33C.glPixelStorei(GL33C.GL_UNPACK_ALIGNMENT, 1);

		if(img != null) {
			GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, texFormat, w[0], h[0], 0, 
				GL33C.GL_RGBA, GL33C.GL_UNSIGNED_BYTE, img);
			MemoryUtil.memFree(img);
		} else {
			GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, texFormat, w[0], h[0], 0, 
				GL33C.GL_RGBA, GL33C.GL_FLOAT, imgf);
			MemoryUtil.memFree(imgf);
		}
		
		GL33C.glGenerateMipmap(GL33C.GL_TEXTURE_2D);
		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);

		Texture tex = new Texture(id, w[0], h[0], 0);
		if(imgf != null) tex.hdr = true;
		
		return tex;
    }
    
    /**
     * Should be destroyed after using!
     * @param w Texture width
     * @param h Texture height
     * @return Texture
     */
    public static Texture createTexture(int w, int h) {
        return Texture.createTexture(w, h, 0, GL33C.GL_RGB8, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE);
    }
    
    /**
     * Should be destroyed after using!
     * @param w Texture width
     * @param h Texture height
     * @param d Texture depth
	 * @param internalFormat OpenGL data
	 * @param format OpenGL data
	 * @param type OpenGL data
     * @return Texture
     */
    public static Texture createTexture(int w, int h, int d, int internalFormat, int format, int type) {
        int gltex = GL33C.glGenTextures();
		
		if(d == 0) {
			GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, gltex);
			GL33C.glTexImage2D(GL33C.GL_TEXTURE_2D, 0, internalFormat, w, h, 0, format, type, 0);
			GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);
		} else {
			GL33C.glBindTexture(GL33C.GL_TEXTURE_3D, gltex);
			GL33C.glTexImage3D(GL33C.GL_TEXTURE_3D, 0, internalFormat, w, h, d, 0, format, type, 0);
			GL33C.glBindTexture(GL33C.GL_TEXTURE_3D, 0);
		}
        
        return new Texture(gltex, w, h, d);
    }
    
}
