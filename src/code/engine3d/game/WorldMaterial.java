package code.engine3d.game;

import code.engine3d.Material;
import code.engine3d.E3D;
import code.engine3d.Sampler;
import code.engine3d.Shader;
import code.engine3d.Texture;
import code.utils.IniFile;
import code.utils.assetManager.ReusableContent;

/**
 *
 * @author Roman Lahin
 */
public class WorldMaterial extends Material {
    
    public static final int UNDEFINED = -2, DEFAULT = -1;
	public static boolean disableMipmapping;
    
    public Texture tex;//, normalmap;
	public Sampler sampler;
    public WorldShaderPack shaderPack;
    
    private boolean alphaTest, linear, mipMapping, wrapClamp, glow;
    private float scrollXSpeed, scrollYSpeed;
    
    public WorldMaterial(E3D e3d) {
        super(e3d);
		sampler = new Sampler(e3d);
        init(e3d);
    }
    
    private void init(E3D e3d) {
        /*shaderPack = WorldShaderPack.get(e3d, "world", 
				new String[][]{null, new String[]{"GLOW"}, new String[]{"NORMALMAP"}});*/
        shaderPack = WorldShaderPack.get(e3d, "world", 
				new String[][]{null, new String[]{"GLOW"}});
    }
    
    public void load(E3D e3d, String name, IniFile ini) {
        String tmp = ini.get("alpha_test");
        
        if(tmp != null && tmp.equals("1")) {
            alphaTest = true; blendMode = OFF;
        } else if(tmp != null && tmp.equals("blend")) {
            alphaTest = true; blendMode = BLEND;
        }
        super.load(e3d, name, ini);
        
        linear = ini.getInt("linear", 0) == 1;
        mipMapping = ini.getInt("mipmap", 1) == 1;
        wrapClamp = ini.getDef("wrap", "repeat").equals("clamp");
		
		updateSamplerProperties(e3d);
        
        scrollXSpeed = ini.getFloat("scroll_x", 0);
        scrollYSpeed = ini.getFloat("scroll_y", 0);
        
        glow = ini.getInt("glow", 0) == 1;
		
		tex = e3d.getTexture(ini.get("albedo"));
		/*tmp = ini.get("normalmap");
		if(tmp != null) normalmap = e3d.getTexture(tmp);*/
    }
	
	public void updateSamplerProperties(E3D e3d) {
		if(sampler != null) sampler.setProperties(e3d, linear, disableMipmapping ? false : mipMapping, wrapClamp);
	}
    
    public ReusableContent use() {
        if(using == 0) {
            if(tex != null) tex.use();
            //if(normalmap != null) normalmap.use();
            if(shaderPack != null) shaderPack.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1) {
            if(tex != null) tex.free();
            //if(normalmap != null) normalmap.free();
            if(shaderPack != null) shaderPack.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        tex = null;
		if(sampler != null) sampler.destroy();
        shaderPack = null;
    }
    
    public void bind(E3D e3d, long time) {
        //Shader shader = shaderPack.shaders[glow?1:(normalmap != null ? 2 : 0)];
		Shader shader = shaderPack.shaders[glow?1:0];
        shader.bind();
        
        if(tex != null) {
			sampler.bind(0);
			tex.bind(0);
		}
        
        /*if(normalmap != null) {
			sampler.bind(1);
			normalmap.bind(1);
		}*/
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shaderPack.uvOffset, time * scrollXSpeed / 1000, -time * scrollYSpeed / 1000);
        }
        
        if(alphaTest) {
			shader.setUniformf(shaderPack.alphaThreshold, blendMode == OFF?0.5f:0);
        }
        
        super.bind(e3d, time);
    }
    
    public void unbind() {
        //Shader shader = shaderPack.shaders[glow?1:(normalmap != null ? 2 : 0)];
		Shader shader = shaderPack.shaders[glow?1:0];
        
        if(tex != null) {
			sampler.unbind(0);
			tex.unbind(0);
		}
        
        /*if(normalmap != null) {
			sampler.unbind(1);
			normalmap.unbind(1);
		}*/
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shaderPack.uvOffset, 0, 0);
        }
        
        if(alphaTest) {
			shader.setUniformf(shaderPack.alphaThreshold, -1);
        }
        
        super.unbind();
        shader.unbind();
    }

}
