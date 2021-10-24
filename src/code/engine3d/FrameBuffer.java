package code.engine3d;

import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class FrameBuffer {
    
    private int frameBuffer, depthBuffer;
    
    public Texture tex;
    
    public FrameBuffer(int w, int h, boolean createDepth) {
        frameBuffer = GL33C.glGenFramebuffers();
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, frameBuffer);
        
        if(createDepth) {
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
        
        tex = Texture.createTexture(w, h);

        //Set as color color buffer
        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, tex.id);
        GL33C.glDrawBuffer(GL33C.GL_COLOR_ATTACHMENT0);
        GL33C.glBindTexture(GL33C.GL_TEXTURE_2D, 0);

        // Set "renderedTexture" as our colour attachement #0
        GL33C.glFramebufferTexture2D(GL33C.GL_FRAMEBUFFER,
                GL33C.GL_COLOR_ATTACHMENT0,
                GL33C.GL_TEXTURE_2D,
                tex.id,
                0);
        
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
        GL33C.glDeleteRenderbuffers(depthBuffer);
        
        tex.destroy();
        tex = null;
    }
    
    public void bind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, frameBuffer);
    }
    
    public void unbind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0);
    }

}
