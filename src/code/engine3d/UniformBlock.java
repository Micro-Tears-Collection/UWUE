package code.engine3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class UniformBlock {

    int buffer, bindingPoint;
    private int size;
    
    public UniformBlock(int size, int bindingPoint) {
        this.size = size;
        this.bindingPoint = bindingPoint;
        
		GL33C.glGetError();
		
        buffer = GL33C.glGenBuffers();

        if(buffer != 0) {
            GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, buffer);
            GL33C.glBufferData(GL33C.GL_UNIFORM_BUFFER, size, GL33C.GL_STREAM_DRAW);
            GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, 0);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.out.println("Can't create Uniform block, error: "+error);
            }
            
            GL33C.glBindBufferRange(GL33C.GL_UNIFORM_BUFFER, bindingPoint, buffer, 0, size);
            
            error = GL33C.glGetError();
            if(error != 0) {
                System.out.println("Can't bind Uniform block to binding point, error: "+error);
            }
        }
    }
    
    public void destroy() {
        if(buffer != 0) {
            //todo unbound???
            GL33C.glDeleteBuffers(buffer);
        }
    }
    
    public final void bind() {
        if(buffer != 0) {
            GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, buffer);
        }
    }
    
    public final void unbind() {
        if(buffer != 0) GL33C.glBindBuffer(GL33C.GL_UNIFORM_BUFFER, 0);
    }
    
    public final void sendData(IntBuffer data, int offset) {
        if(buffer != 0) {
            GL33C.glBufferSubData(GL33C.GL_UNIFORM_BUFFER, offset, data);
        }
    }
    
    public final void sendData(FloatBuffer data, int offset) {
        if(buffer != 0) {
            GL33C.glBufferSubData(GL33C.GL_UNIFORM_BUFFER, offset, data);
        }
    }

}
