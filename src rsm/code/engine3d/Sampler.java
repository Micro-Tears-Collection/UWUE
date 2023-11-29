package code.engine3d;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class Sampler {
	
	private int id = 0;
	
	public Sampler(E3D e3d) {
		init();
		setProperties(e3d, false, true, false);
	}
	
	public Sampler(E3D e3d, boolean linear, boolean mipMapping, boolean clamp) {
		init();
		setProperties(e3d, linear, mipMapping, clamp);
	}
	
	public Sampler(E3D e3d, boolean linear, boolean mipMapping, boolean clamp, boolean anisotropic) {
		init();
		setProperties(e3d, linear, mipMapping, clamp, anisotropic);
	}
	
	private void init() {
		id = GL33C.glGenSamplers();
	}
	
	public void destroy() {
		GL33C.glDeleteSamplers(id);
	}
	
	public void setProperties(E3D e3d, boolean linear, boolean mipMapping, boolean clamp) {
		setProperties(e3d, linear, mipMapping, clamp, true);
	}
	
	public void setProperties(E3D e3d, boolean linear, boolean mipMapping, boolean clamp, boolean anisotropic) {
		if(id != 0) {
			int mag = linear ? GL33C.GL_LINEAR : GL33C.GL_NEAREST;
			int min = mipMapping ?
			       (linear ? GL33C.GL_LINEAR_MIPMAP_LINEAR : GL33C.GL_NEAREST_MIPMAP_LINEAR)
				    : mag;
			int wrap = clamp?GL33C.GL_CLAMP_TO_EDGE:GL33C.GL_REPEAT;
		
			GL33C.glSamplerParameteri(id, GL33C.GL_TEXTURE_MAG_FILTER, mag);
			GL33C.glSamplerParameteri(id, GL33C.GL_TEXTURE_MIN_FILTER, min);
			
			GL33C.glSamplerParameteri(id, GL33C.GL_TEXTURE_WRAP_T, wrap);
			GL33C.glSamplerParameteri(id, GL33C.GL_TEXTURE_WRAP_S, wrap);
			
			if(e3d.anisotropicSupported && anisotropic) {
				GL33C.glSamplerParameterf(
						id, 
						EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 
						mipMapping ? e3d.maxAnisotropy : 1
				);
			}
		}
	}
	
	public void setShadowMap() {
		if(id == 0) return;
		
		GL33C.glSamplerParameteri(id, GL33C.GL_TEXTURE_COMPARE_MODE, GL33C.GL_COMPARE_REF_TO_TEXTURE);
	}
	
	public void bind(int unit) {
		GL33C.glBindSampler(unit, id);
	}
	
	public void unbind(int unit) {
		GL33C.glBindSampler(unit, 0);
	}

}
