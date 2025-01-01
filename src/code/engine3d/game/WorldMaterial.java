package code.engine3d.game;

import code.engine3d.Material;
import code.engine3d.E3D;
import code.engine3d.Sampler;
import code.engine3d.Shader;
import code.engine3d.Texture;
import code.utils.IniFile;
import code.utils.StringTools;
import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class WorldMaterial extends Material {
    
    public static final int UNDEFINED = -2, DEFAULT = -1;
	public static final int UNI_UV_OFFSET = 0, UNI_ALPHA_THRESHOLD = 1, SPECULAR = 2, ROUGHNESS = 3, LIGHT_THRU = 4;
	public static boolean disableMipmapping;
    
    public Texture tex;
	public Sampler sampler;
    public Shader shader;
    
	boolean glow; //dont change!!!
	//per pixel stuff
	//public boolean fastShader;
	public Texture normalMap, parallaxMap;//, specularMap, roughnessMap, parallaxMap, emissionMap;
	public float[] specular;
	public float roughness;
	
    boolean alphaTest, linear, mipMapping, wrapClamp;
    private float scrollXSpeed, scrollYSpeed;
	private float lightThru;
	
	private float twoLayer = -1;
	private boolean toon;
    
    public WorldMaterial(E3D e3d) {
        super(e3d);
		sampler = new Sampler(e3d);
    }
	
	public static Shader getShader(E3D e3d, WorldMaterial mat) {
		ArrayList<String> defs = new ArrayList<>();
		
		if(!mat.glow) {
			defs.add("LIGHT");
			defs.add("MAX_LIGHTS "+E3D.MAX_LIGHTS);
			
			if(mat.normalMap != null) defs.add("NORMALMAP");
			if(mat.specular != null) {
				defs.add("SPECULAR");
				
				/*if(mat.specularMap != null) defs.add("SPECULARMAP");
				if(mat.roughnessMap != null) defs.add("ROUGHNESSMAP");*/
			}
			
			if(mat.parallaxMap != null) defs.add("PARALLAXMAP");
			//if(mat.emissionMap != null) defs.add("EMISSIONMAP");
		}
		
		if(mat.twoLayer > 0) defs.add("LAYER_DEPTH " + mat.twoLayer);
		if(mat.toon) defs.add("TOON");
		if(mat.atoc) defs.add("ALPHA_TO_COVERAGE");
		if(mat.sampleShading) defs.add("SAMPLE_SHADING");
		
		String[] defsarr = defs.toArray(new String[defs.size()]);
		if(defsarr.length == 0) defsarr = null;
		Shader shader = e3d.getShader("world"/*mat.fastShader ? "fastworld" : "fragworld"*/, defsarr);
		
		if(e3d.isShaderWasCreated()) {
			shader.bind();
			
			shader.addTextureUnit("albedoMap", 0);
			
			if(defs.contains("LIGHT")) {
				shader.addUniformBlock(e3d.lights, "lights");
				if(defs.contains("NORMALMAP")) shader.addTextureUnit("normalMap", 1);
			
				if(defs.contains("SPECULAR")) {
					shader.storeUniform(SPECULAR, "specular");
					shader.storeUniform(ROUGHNESS, "roughness");
					
					/*if(defs.contains("SPECULARMAP")) shader.addTextureUnit("specularMap", 2);
					if(defs.contains("ROUGHNESSMAP")) shader.addTextureUnit("roughnessMap", 3);*/
				}
				
				if(defs.contains("PARALLAXMAP")) shader.addTextureUnit("parallaxMap", 4);
				//if(defs.contains("EMISSIONMAP")) shader.addTextureUnit("emissionMap", 5);
				
				shader.storeUniform(LIGHT_THRU, "lightThru");
			}
			
			shader.addUniformBlock(e3d.fog, "fog");
			
			shader.storeUniform(UNI_UV_OFFSET, "uvOffset");
			shader.storeUniform(UNI_ALPHA_THRESHOLD, "alphaThreshold");
			
			shader.unbind();
		}
		
		return shader;
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
		
		lightThru = ini.getFloat("light_thru", 0);
		
		String currentPath = AssetManager.getDirectory(name);
		
		tex = e3d.getTexture(ini.get("albedo"), currentPath);
		
		tmp = ini.get("normalmap");
		if(tmp != null) normalMap = e3d.getTexture(tmp, currentPath);
		
		/*tmp = ini.get("roughnessmap");
		if(tmp != null) roughnessMap = e3d.getTexture(tmp, currentPath);*/
        
        roughness = ini.getFloat("roughness", /*roughnessMap != null ? 1 : */roughness);
		
		/*tmp = ini.get("specularmap");
		if(tmp != null) specularMap = e3d.getTexture(tmp, currentPath);*/
		
        tmp = ini.getDef("specular", /*specularMap != null ? "1" : */null);
		if(tmp != null) {
			specular = new float[3];
			float[] fvs = StringTools.cutOnFloats(tmp, ',');
			
			if(fvs.length < 3) specular[0] = specular[1] = specular[2] = fvs[0];
			else System.arraycopy(fvs, 0, specular, 0, 3);
		}
		
		tmp = ini.get("parallaxmap");
		if(tmp != null) parallaxMap = e3d.getTexture(tmp, currentPath);
		
		/*tmp = ini.get("emissionmap");
		if(tmp != null) emissionMap = e3d.getTexture(tmp, currentPath);
		
		fastShader = ini.getInt("fast_shader", 0) == 1;*/
		
		twoLayer = ini.getFloat("second_layer", -1);
		toon = ini.getInt("toon", 0) == 1;
		
		shader = getShader(e3d, this);
    }
	
	public void updateSamplerProperties(E3D e3d) {
		if(sampler != null) sampler.setProperties(e3d, linear, disableMipmapping ? false : mipMapping, wrapClamp);
	}
    
    public ReusableContent use() {
        if(using == 0) {
            if(tex != null) tex.use();
            if(normalMap != null) normalMap.use();
            //if(specularMap != null) specularMap.use();
            //if(roughnessMap != null) roughnessMap.use();
            if(parallaxMap != null) parallaxMap.use();
            //if(emissionMap != null) emissionMap.use();
			shader.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1) {
            if(tex != null) tex.free();
            if(normalMap != null) normalMap.free();
            //if(specularMap != null) specularMap.free();
            //if(roughnessMap != null) roughnessMap.free();
            if(parallaxMap != null) parallaxMap.free();
            //if(emissionMap != null) emissionMap.free();
            shader.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        tex = null;
		if(sampler != null) sampler.destroy();
        shader = null;
    }
    
    public void bind(E3D e3d, long time) {
        shader.bind();
        
        if(tex != null) {
			sampler.bind(0);
			tex.bind(0);
		}
        
		if(!glow) {
			if(lightThru > 0) shader.setUniformf(shader.uniforms[LIGHT_THRU], lightThru);
			if(normalMap != null) {
				sampler.bind(1);
				normalMap.bind(1);
			}
			
			if(specular != null) {
				shader.setUniform3f(shader.uniforms[SPECULAR], specular[0], specular[1], specular[2]);
				shader.setUniformf(shader.uniforms[ROUGHNESS], roughness);
				
				/*if(specularMap != null) {
					sampler.bind(2);
					specularMap.bind(2);
				}
				
				if(roughnessMap != null) {
					sampler.bind(3);
					roughnessMap.bind(3);
				}*/
			}
	
			/*if(emissionMap != null) {
				sampler.bind(5);
				emissionMap.bind(5);
			}*/
		}
		
		if(parallaxMap != null) {
			sampler.bind(4);
			parallaxMap.bind(4);
		}
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shader.uniforms[UNI_UV_OFFSET], time * scrollXSpeed / 1000, -time * scrollYSpeed / 1000);
        }
        
        if(alphaTest) {
			shader.setUniformf(shader.uniforms[UNI_ALPHA_THRESHOLD], blendMode == OFF?0.5f:0);
        }
        
        super.bind(e3d, time);
    }
    
    public void unbind(E3D e3d) {
        if(tex != null) {
			sampler.unbind(0);
			tex.unbind(0);
		}
        
		if(!glow) {
			if(lightThru > 0) shader.setUniformf(shader.uniforms[LIGHT_THRU], 0);
			
			if(normalMap != null) {
				sampler.unbind(1);
				normalMap.unbind(1);
			}
			
			/*if(specular != null) {
				if(specularMap != null) {
					sampler.unbind(2);
					specularMap.unbind(2);
				}
				
				if(roughnessMap != null) {
					sampler.unbind(3);
					roughnessMap.unbind(3);
				}
			}
	
			if(emissionMap != null) {
				sampler.unbind(5);
				emissionMap.unbind(5);
			}*/
		}
		
		if(parallaxMap != null) {
			sampler.unbind(4);
			parallaxMap.unbind(4);
		}
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shader.uniforms[UNI_UV_OFFSET], 0, 0);
        }
        
        if(alphaTest) {
			shader.setUniformf(shader.uniforms[UNI_ALPHA_THRESHOLD], -1);
        }
        
        super.unbind(e3d);
        shader.unbind();
    }

}
