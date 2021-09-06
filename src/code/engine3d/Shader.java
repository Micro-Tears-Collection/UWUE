package code.engine3d;

import code.utils.assetManager.CachedText;
import code.utils.assetManager.ReusableContent;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class Shader extends ReusableContent {
    
    private int vertShader, fragShader;
    private int program;

    protected Shader(String path, String[] definitions) {
        init(path, definitions);
    }

    protected Shader(String path) {
        init(path, null);
    }
    
    protected void init(String path, String[] defs) {
        lock(); //We don't want to unload shaders
        
        GL33C.glGetInteger(GL33C.GL_FRAGMENT_SHADER_DERIVATIVE_HINT);
        if(GL33C.glGetError() != 0) {
            System.out.println("Can't create shader! Cancelling");
            return;
        }
        
        path = "/shaders/"+path+".glsl";
        String shader = CachedText.get(path).lock().toString();
        
        if(defs != null) {
            StringBuilder sb = new StringBuilder();
            
            for(String definition : defs) {
                sb.append("#define ");
                sb.append(definition);
                sb.append("\n");
            }
            
            sb.append(shader);
            shader = sb.toString();
        }
        
        vertShader = GL33C.glCreateShader(GL33C.GL_VERTEX_SHADER);
        
        GL33C.glShaderSource(vertShader, "#version 330 core\n#define VERT\n"+shader);
        
        GL33C.glCompileShader(vertShader);
        if(compileCheck(path, vertShader)) {
            vertShader = 0;
            return;
        }
        
        fragShader = GL33C.glCreateShader(GL33C.GL_FRAGMENT_SHADER);
        
        GL33C.glShaderSource(fragShader, "#version 330 core\n#define FRAG\n"+shader);
        
        GL33C.glCompileShader(fragShader);
        if(compileCheck(path, fragShader)) {
            GL33C.glDeleteShader(vertShader);
            vertShader = 0;
            fragShader = 0;
            return;
        }
        
        program = GL33C.glCreateProgram();
        GL33C.glAttachShader(program, vertShader);
        GL33C.glAttachShader(program, fragShader);
        
        GL33C.glLinkProgram(program);
        
        GL33C.glDetachShader(program, vertShader);
        GL33C.glDetachShader(program, fragShader);
        
        if(linkCheck(path, program)) {
            GL33C.glDeleteShader(vertShader);
            GL33C.glDeleteShader(fragShader);
            
            vertShader = 0;
            fragShader = 0;
            program = 0;
            return;
        }
    }
    
    private boolean compileCheck(String path, int shader) {
		//todo print glsl code when error is dropping
        int isCompiled = GL33C.glGetShaderi(shader, GL33C.GL_COMPILE_STATUS);
        
        if(isCompiled != GL33C.GL_TRUE) {
            String log = GL33C.glGetShaderInfoLog(shader);
            
            System.err.println("Shader compilation error: "+path);
            System.err.println(log);

            GL33C.glDeleteShader(shader);
            
            return true;
        }
        
        return false;
    }
    
    private boolean linkCheck(String path, int program) {
        int isLinked = GL33C.glGetProgrami(program, GL33C.GL_LINK_STATUS);
        
        if(isLinked != GL33C.GL_TRUE) {
            String log = GL33C.glGetProgramInfoLog(program);
            
            System.err.println("Program linking error: "+path);
            System.err.println(log);

            GL33C.glDeleteProgram(program);
            
            return true;
        }
        
        return false;
    }
    
    public boolean isCompiled() {
        return program != 0;
    }
    
    public void destroy() {
        if(program != 0) GL33C.glDeleteProgram(program);
        
        if(vertShader != 0) GL33C.glDeleteProgram(vertShader);
        if(fragShader != 0) GL33C.glDeleteProgram(fragShader);
    }
    
    public void bind() {
        if(program != 0) {
            GL33C.glUseProgram(program);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't bind shader :(");
            }
        }
    }
    
    public void unbind() {
        if(program != 0) GL33C.glUseProgram(0);
    }
    
    public void addTextureUnit(int unit) {
        if(program != 0) setUniformi(getUniformIndex("texUnit"+unit), unit);
    }
    
    public void addTextureUnit(String name, int unit) {
        if(program != 0) setUniformi(getUniformIndex(name), unit);
    }
    
    public int getUniformIndex(String name) {
        if(program != 0) {
            int location = GL33C.glGetUniformLocation(program, name);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't find uniform "+name);
            }
            
            if(location == -1) System.out.println("No such uniform "+name);
            
            return location;
        }
        return -1;
    }
    
    public void addUniformBlock(UniformBlock block, String name) {
        if(program != 0) {
            int index = getUniformBlockIndex(name);
            
            if(index >= 0) {
                GL33C.glUniformBlockBinding(program, index, block.bindingPoint);

                int error = GL33C.glGetError();
                if(error != 0) {
                    System.err.println("Error: " + error);
                    System.err.println("Can't add uniform block " + name);
                }

            }
        }
    }
    
    private int getUniformBlockIndex(String name) {
        if(program != 0) {
            int location = GL33C.glGetUniformBlockIndex(program, name);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't find uniform block "+name);
            }
            
            if(location < 0) System.out.println("No such uniform block "+name);
            
            return location;
        }
        
        return -1;
    }
    
    public void setUniformf(int uniform, float value) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniform1f(uniform, value);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform f");
            }
        }
    }
    
    public void setUniform2f(int uniform, float x, float y) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniform2f(uniform, x, y);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform 2f");
            }
        }
    }
    
    public void setUniform3f(int uniform, float x, float y, float z) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniform3f(uniform, x, y, z);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform 3f");
            }
        }
    }
    
    public void setUniform4f(int uniform, float x, float y, float z, float w) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniform4f(uniform, x, y, z, w);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform 4f");
            }
        }
    }
    
    public void setUniformi(int uniform, int value) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniform1i(uniform, value);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform i");
            }
        }
    }
    
    public void setUniformMatrix4fv(int uniform, FloatBuffer mat) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniformMatrix4fv(uniform, false, mat);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform matrix 4fv");
            }
        }
    }
    
    public void setUniformMatrix3fv(int uniform, FloatBuffer mat) {
        if(program != 0 && uniform != -1) {
            GL33C.glUniformMatrix3fv(uniform, false, mat);
            
            int error = GL33C.glGetError();
            if(error != 0) {
                System.err.println("Error: "+error);
                System.err.println("Can't set uniform matrix 3fv");
            }
        }
    }
    
}
