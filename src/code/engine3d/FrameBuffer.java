package code.engine3d;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Roman Lahin
 */
public class FrameBuffer {
    
    private int frameBuffer, depthBuffer;
    
    public Texture tex;
    
    public FrameBuffer(int w, int h, boolean createDepth) {
        frameBuffer = ARBFramebufferObject.glGenFramebuffers();
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, frameBuffer);
        
        if(createDepth) {
            depthBuffer = ARBFramebufferObject.glGenRenderbuffers();
            ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, depthBuffer);
            //Set as depth buffer
            ARBFramebufferObject.glRenderbufferStorage(ARBFramebufferObject.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT,
                    w, h);
            ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, 0);

            ARBFramebufferObject.glFramebufferRenderbuffer(ARBFramebufferObject.GL_FRAMEBUFFER,
                    ARBFramebufferObject.GL_DEPTH_ATTACHMENT,
                    ARBFramebufferObject.GL_RENDERBUFFER,
                    depthBuffer);
        }
        
        tex = Texture.createTexture(w, h);

        //Set as color color buffer
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.id);
        GL11.glDrawBuffer(ARBFramebufferObject.GL_COLOR_ATTACHMENT0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Set "renderedTexture" as our colour attachement #0
        ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D,
                tex.id,
                0);
        
        int status = ARBFramebufferObject.glCheckFramebufferStatus(ARBFramebufferObject.GL_FRAMEBUFFER);
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);
        
        if(status != ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE) {
            throw new Error("error in framebuffer init");
        }
    }
    
    public void destroy() {
        if(frameBuffer != 0) {
            ARBFramebufferObject.glDeleteFramebuffers(frameBuffer);
        }
        if(depthBuffer != 0) {
            ARBFramebufferObject.glDeleteRenderbuffers(depthBuffer);
        }
        tex.destroy();
    }
    
    public void bind() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, frameBuffer);
    }
    
    public void unbind() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);
    }

}
