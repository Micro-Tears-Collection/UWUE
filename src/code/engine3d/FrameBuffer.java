package code.engine3d;

import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class FrameBuffer {
    
    private int frameBuffer, depthBuffer;
    
    public Texture[] texs;
	public Texture depthTex;
	
    public FrameBuffer(int w, int h) {
		set(w, h, null, true, new Texture[] {Texture.createTexture(w, h)});
	}
    
    public FrameBuffer(int w, int h, Texture depthTex, boolean createDepth, Texture[] texs) {
		set(w, h, depthTex, createDepth, texs);
    }
	
	private void set(int w, int h, Texture depthTex, boolean createDepth, Texture[] texs) {
		GL33C.glGetError();
		
        frameBuffer = GL33C.glGenFramebuffers();
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, frameBuffer);
        
		this.depthTex = depthTex;
		
		if(depthTex != null) {
			
			GL33C.glFramebufferTexture(GL33C.GL_FRAMEBUFFER,
					GL33C.GL_DEPTH_ATTACHMENT,
					depthTex.id,
					0);
			
		} else if(createDepth) {
            depthBuffer = GL33C.glGenRenderbuffers();

            GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, depthBuffer);
            //Set as depth buffer
            GL33C.glRenderbufferStorage(GL33C.GL_RENDERBUFFER, GL33C.GL_DEPTH_COMPONENT,
                    w, h);
            GL33C.glBindRenderbuffer(GL33C.GL_RENDERBUFFER, 0);

            GL33C.glFramebufferRenderbuffer(GL33C.GL_FRAMEBUFFER,
                    GL33C.GL_DEPTH_ATTACHMENT,
                    GL33C.GL_RENDERBUFFER,
                    depthBuffer);
        }
		
		this.texs = texs;
		
		if(texs != null) {
			int[] attachements = new int[texs.length];
			for(int i=0; i<texs.length; i++) attachements[i] = GL33C.GL_COLOR_ATTACHMENT0 + i;
			
			
			//Set as color buffer
			for(int i=0; i<texs.length; i++) {
				Texture tex = texs[i];
				
				// Set tex as our colour attachement #i
				if(tex.d == 0) {
					GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER,
							GL33C.GL_COLOR_ATTACHMENT0 + i,
							GL33C.GL_TEXTURE_2D,
							tex.id,
							0);
				} else {
					GL33C.glFramebufferTexture3D(GL33C.GL_FRAMEBUFFER,
							GL33C.GL_COLOR_ATTACHMENT0 + i,
							GL33C.GL_TEXTURE_3D,
							tex.id,
							0,
							0);
				}
			}
			
			GL33C.glDrawBuffers(attachements);
		} else {
			GL33C.glDrawBuffer(GL33C.GL_NONE);
		}
        
        int status = GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER);
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);
        
        if(status != GL33C.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer creation isn't completed");
        }
        
        int error = GL33C.glGetError();
        if(error != 0) System.out.println("GL framebuffer creation error "+error);
	}
    
    public void destroy() {
        GL33C.glDeleteFramebuffers(frameBuffer);
        if(depthBuffer != 0) GL33C.glDeleteRenderbuffers(depthBuffer);
        
		if(texs != null) for(Texture tex : texs) tex.destroy();
		if(depthTex != null) depthTex.destroy();
		
        texs = null;		
        depthTex = null;
    }
    
    public void bind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, frameBuffer);
    }
    
    public void unbind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);
    }
	
	public void set3DTexZ(int texId, int z) {
		GL33C.glFramebufferTexture3D(GL33C.GL_FRAMEBUFFER,
				GL33C.GL_COLOR_ATTACHMENT0 + texId,
				GL33C.GL_TEXTURE_3D,
				texs[texId].id,
				0,
				z);
	}

}
