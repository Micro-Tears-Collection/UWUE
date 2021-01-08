package code.engine3d;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Roman Lahin
 */
public class FrameBuffer {
    
    int frameBuffer, depthBuffer;
    
    Texture frameBufferTex;
    
    public FrameBuffer(int w, int h, boolean createDepth) {
        frameBuffer = ARBFramebufferObject.glGenFramebuffers();
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, frameBuffer);
        
        frameBufferTex = Texture.createTexture(320, 240);
        
        if(createDepth) {
            depthBuffer = ARBFramebufferObject.glGenRenderbuffers();
            ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, depthBuffer);
            ARBFramebufferObject.glRenderbufferStorage(ARBFramebufferObject.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT,
                    frameBufferTex.w, frameBufferTex.h);

            ARBFramebufferObject.glFramebufferRenderbuffer(ARBFramebufferObject.GL_FRAMEBUFFER,
                    ARBFramebufferObject.GL_DEPTH_ATTACHMENT,
                    ARBFramebufferObject.GL_RENDERBUFFER,
                    depthBuffer);
        }

        // Set "renderedTexture" as our colour attachement #0
        ARBFramebufferObject.glFramebufferTexture2D(
                ARBFramebufferObject.GL_FRAMEBUFFER,
                ARBFramebufferObject.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D,
                frameBufferTex.id,
                0);

        // Set the list of draw buffers.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBufferTex.id);
        GL11.glDrawBuffer(ARBFramebufferObject.GL_COLOR_ATTACHMENT0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        
        if(ARBFramebufferObject.glCheckFramebufferStatus(ARBFramebufferObject.GL_FRAMEBUFFER) 
                != ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE) {
            throw new Error("error in framebuffer init");
        }
    }
    
    public void bind() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, frameBuffer);
    }
    
    public void unbind() {
        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);
    }

}
