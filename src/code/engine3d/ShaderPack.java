package code.engine3d;

import code.utils.assetManager.ReusableContent;

/**
 *
 * @author Roman Lahin
 */
public class ShaderPack extends ReusableContent {
    
    public Shader[] shaders;
	public String[][] defs;
    
    protected ShaderPack(E3D e3d, String name, String[][] defs) {
        shaders = new Shader[defs.length];
		this.defs = defs;
        
        for(int i=0; i<shaders.length; i++) {
            shaders[i] = e3d.getShader(name, defs[i]);
        }
    }
    
    public ReusableContent use() {
        if(using == 0 && shaders != null) {
            for(Shader shader : shaders) shader.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1 && shaders != null) {
            for(Shader shader : shaders) shader.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        shaders = null;
    }

}
