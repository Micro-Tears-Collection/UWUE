package code.engine3d;

import code.utils.Asset;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 *
 * @author Roman Lahin
 */
public class Shader {
    
    int vertShader, fragShader;
    int program;

    public Shader(String path) {
        GL11.glGetInteger(GL20.GL_FRAGMENT_SHADER_DERIVATIVE_HINT);
        if(GL11.glGetError() != 0) {
            System.out.println("Can't create shader! Cancelling");
            return;
        }
        
        vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertShader, Asset.loadString(path+".vert"));
        GL20.glCompileShader(vertShader);
        if(compileCheck(vertShader)) return;
        System.out.println("vertex shader "+path+" created");
        
        fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragShader, Asset.loadString(path+".frag"));
        GL20.glCompileShader(fragShader);
        if(compileCheck(fragShader)) {
            GL20.glDeleteProgram(vertShader);
            return;
        }
        System.out.println("fragment shader "+path+" created");
        
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        
        GL20.glLinkProgram(program);
        
        GL20.glDetachShader(program, vertShader);
        GL20.glDetachShader(program, fragShader);
        System.out.println("shader program "+path+" created succesfully");
    }
    
    private boolean compileCheck(int shader) {
        int isCompiled = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        
        if(isCompiled == GL20.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            
            System.err.println("Shader compilation error");
            System.err.println(log);

            GL20.glDeleteShader(shader);
            
            return true;
        }
        
        return false;
    }
    
    public void destroy() {
        if(program != 0) GL20.glDeleteProgram(program);
        
        if(vertShader != 0) GL20.glDeleteProgram(vertShader);
        if(fragShader != 0) GL20.glDeleteProgram(fragShader);
    }
    
    public boolean isCompiled() {
        return program != 0;
    }
    
    public void bind() {
        if(program != 0) GL20.glUseProgram(program);
    }
    
    public void unbind() {
        if(program != 0) GL20.glUseProgram(0);
    }
    
    public void addTextureUnit(int unit) {
        if(program != 0) setUniformi(findUniform("texUnit"+unit), unit);
    }
    
    public int findUniform(String name) {
        if(program != 0) {
            int location = GL20.glGetUniformLocation(program, name);
            
            int error = GL11.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't find uniform "+name);
            }
            
            return location;
        }
        return 0;
    }
    
    public void setUniformf(int uniform, float value) {
        if(program != 0) {
            GL20.glUniform1f(uniform, value);
            
            int error = GL11.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform");
            }
        }
    }
    
    public void setUniformi(int uniform, int value) {
        if(program != 0) {
            GL20.glUniform1i(uniform, value);
            
            int error = GL11.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform");
            }
        }
    }
    
}
