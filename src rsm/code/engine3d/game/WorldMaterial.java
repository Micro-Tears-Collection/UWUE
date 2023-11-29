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
	public static final int UNI_UV_OFFSET = 0, UNI_ALPHA_THRESHOLD = 1, SPECULAR = 2, ROUGHNESS = 3;
	public static boolean disableMipmapping;
    
    public Texture tex;
	public Sampler sampler;
    public Shader shader, shadowMapShader;
    
	boolean glow; //dont change!!!
	//per pixel stuff
	public boolean fastShader;
	public Texture normalMap, specularMap, roughnessMap, parallaxMap, emissionMap;
	public float[] specular;
	public float roughness;
	
    boolean alphaTest, linear, mipMapping, wrapClamp;
    private float scrollXSpeed, scrollYSpeed;
    
    public WorldMaterial(E3D e3d) {
        super(e3d);
		sampler = new Sampler(e3d);
    }
	
	public static Shader getShader(E3D e3d, WorldMaterial mat, boolean shadowMap, boolean hdr) {
		ArrayList<String> defs = new ArrayList<>();
		
		if(hdr) defs.add("HDR");
		
		if(!mat.glow) {
			defs.add("LIGHT");
			defs.add("MAX_LIGHTS "+E3D.MAX_LIGHTS);
			
			if(!shadowMap) {
				if(mat.normalMap != null) defs.add("NORMALMAP");
				if(mat.specular != null) {
					defs.add("SPECULAR");

					if(mat.specularMap != null) defs.add("SPECULARMAP");
					if(mat.roughnessMap != null) defs.add("ROUGHNESSMAP");
				}

				if(mat.parallaxMap != null) defs.add("PARALLAXMAP");
				if(mat.emissionMap != null) defs.add("EMISSIONMAP");
			}
		}
		
		String[] defsarr = defs.toArray(new String[defs.size()]);
		if(defsarr.length == 0) defsarr = null;
		
		String shaderName = mat.fastShader ? "fragworld" : "fragworld";
		if(shadowMap) shaderName = "shadowmap";
		
		Shader shader = e3d.getShader(shaderName, defsarr);
		
		if(e3d.isShaderWasCreated()) {
			shader.bind();
			
			shader.addTextureUnit("albedoMap", 0);
			
			if(defs.contains("LIGHT")) {
				shader.addUniformBlock(e3d.lights, "lights");
				
				if(defs.contains("NORMALMAP")) shader.addTextureUnit("normalMap", 1);
			
				if(defs.contains("SPECULAR")) {
					shader.storeUniform(SPECULAR, "specular");
					shader.storeUniform(ROUGHNESS, "roughness");
					
					if(defs.contains("SPECULARMAP")) shader.addTextureUnit("specularMap", 2);
					if(defs.contains("ROUGHNESSMAP")) shader.addTextureUnit("roughnessMap", 3);
				}
				
				if(defs.contains("PARALLAXMAP")) shader.addTextureUnit("parallaxMap", 4);
				if(defs.contains("EMISSIONMAP")) shader.addTextureUnit("emissionMap", 5);
				
				if(!shadowMap) {
					shader.addTextureUnit("shadowMap", 6);
					shader.addTextureUnit("shadowMapFlux", 7);
					shader.addTextureUnit("shadowMapNorm", 8);
					shader.addTextureUnit("shadowMapPos", 9);
				}
				
				shader.addTextureUnit("flashLightTex", 10);
			}
			
			shader.storeUniform(UNI_ALPHA_THRESHOLD, "alphaThreshold");
			
			if(!shadowMap) {
				shader.addUniformBlock(e3d.fog, "fog");
				shader.storeUniform(UNI_UV_OFFSET, "uvOffset");
			}
			shader.addUniformBlock(e3d.shadowMapData, "shadowMapData");
			
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
        
        linear = ini.getInt("linear", 1) == 1;
        mipMapping = ini.getInt("mipmap", 1) == 1;
        wrapClamp = ini.getDef("wrap", "repeat").equals("clamp");
		
		updateSamplerProperties(e3d);
        
        scrollXSpeed = ini.getFloat("scroll_x", 0);
        scrollYSpeed = ini.getFloat("scroll_y", 0);
        
        glow = ini.getInt("glow", 0) == 1;
		
		String currentPath = AssetManager.getDirectory(name);
		
		tex = e3d.getTexture(ini.get("albedo"), currentPath);
		
		tmp = ini.get("normalmap");
		if(tmp != null) normalMap = e3d.getTexture(tmp, currentPath);
		
		tmp = ini.get("roughnessmap");
		if(tmp != null) roughnessMap = e3d.getTexture(tmp, currentPath);
        
        roughness = ini.getFloat("roughness", roughnessMap != null ? 1 : roughness);
		
		tmp = ini.get("specularmap");
		if(tmp != null) specularMap = e3d.getTexture(tmp, currentPath);
		
        tmp = ini.getDef("specular", specularMap != null ? "1" : null);
		if(tmp != null) {
			specular = new float[3];
			float[] fvs = StringTools.cutOnFloats(tmp, ',');
			
			if(fvs.length < 3) specular[0] = specular[1] = specular[2] = fvs[0];
			else System.arraycopy(fvs, 0, specular, 0, 3);
		}
		
		tmp = ini.get("parallaxmap");
		if(tmp != null) parallaxMap = e3d.getTexture(tmp, currentPath);
		
		tmp = ini.get("emissionmap");
		if(tmp != null) emissionMap = e3d.getTexture(tmp, currentPath);
		
		fastShader = ini.getInt("fast_shader", 0) == 1;
		
		shader = getShader(e3d, this, false, tex.hdr);
		shadowMapShader = getShader(e3d, this, true, tex.hdr);
    }
	
	public void updateSamplerProperties(E3D e3d) {
		if(sampler != null) sampler.setProperties(e3d, linear, disableMipmapping ? false : mipMapping, wrapClamp);
	}
    
    public ReusableContent use() {
        if(using == 0) {
            if(tex != null) tex.use();
            if(normalMap != null) normalMap.use();
            if(specularMap != null) specularMap.use();
            if(roughnessMap != null) roughnessMap.use();
            if(parallaxMap != null) parallaxMap.use();
            if(emissionMap != null) emissionMap.use();
			shader.use();
			shadowMapShader.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1) {
            if(tex != null) tex.free();
            if(normalMap != null) normalMap.free();
            if(specularMap != null) specularMap.free();
            if(roughnessMap != null) roughnessMap.free();
            if(parallaxMap != null) parallaxMap.free();
            if(emissionMap != null) emissionMap.free();
            shader.free();
			shadowMapShader.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        tex = null;
		if(sampler != null) sampler.destroy();
        shader = null;
        shadowMapShader = null;
    }
    
    public void bind(E3D e3d, long time) {
		Shader shr = e3d.shadowPass ? shadowMapShader : shader;
        shr.bind();
        
        if(tex != null) {
			sampler.bind(0);
			tex.bind(0);
		}
        
		if(!e3d.shadowPass && !glow) {
			if(normalMap != null) {
				sampler.bind(1);
				normalMap.bind(1);
			}
			
			if(specular != null) {
				shr.setUniform3f(shr.uniforms[SPECULAR], specular[0], specular[1], specular[2]);
				shr.setUniformf(shr.uniforms[ROUGHNESS], roughness);
				
				if(specularMap != null) {
					sampler.bind(2);
					specularMap.bind(2);
				}
				
				if(roughnessMap != null) {
					sampler.bind(3);
					roughnessMap.bind(3);
				}
			}
	
			if(emissionMap != null) {
				sampler.bind(5);
				emissionMap.bind(5);
			}
			
			if(!e3d.shadowPass) {
				e3d.shadowSampler.bind(6);
				e3d.rsmSampler.bind(7);
				e3d.rsmSampler.bind(8);
				e3d.rsmSampler.bind(9);
				
				e3d.shadowMap.depthTex.bind(6);
				e3d.shadowMap.miniFlux.bind(7);
				e3d.shadowMap.miniNorm.bind(8);
				e3d.shadowMap.miniPos.bind(9);
			}
		}
		
		if(!glow) {
			e3d.flashLightSampler.bind(10);
			e3d.flashLightTex.bind(10);
		}
		
		if(!e3d.shadowPass && parallaxMap != null) {
			sampler.bind(4);
			parallaxMap.bind(4);
		}
        
        if(!e3d.shadowPass && (scrollXSpeed != 0 || scrollYSpeed != 0)) {
            shr.setUniform2f(shr.uniforms[UNI_UV_OFFSET], time * scrollXSpeed / 1000, -time * scrollYSpeed / 1000);
        }
        
        if(alphaTest) {
			shr.setUniformf(shr.uniforms[UNI_ALPHA_THRESHOLD], blendMode == OFF?0.5f:0);
        }
        
        super.bind(e3d, time);
    }
    
    public void unbind(E3D e3d) {
		Shader shr = e3d.shadowPass ? shadowMapShader : shader;
		
        if(tex != null) {
			sampler.unbind(0);
			tex.unbind(0);
		}
        
		if(!e3d.shadowPass && !glow) {
			if(normalMap != null) {
				sampler.unbind(1);
				normalMap.unbind(1);
			}
			
			if(specular != null) {
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
			}
			
			if(!e3d.shadowPass) {
				e3d.shadowSampler.unbind(6);
				e3d.rsmSampler.unbind(7);
				e3d.rsmSampler.unbind(8);
				e3d.rsmSampler.unbind(9);
				
				e3d.shadowMap.depthTex.unbind(6);
				e3d.shadowMap.miniFlux.unbind(7);
				e3d.shadowMap.miniNorm.unbind(8);
				e3d.shadowMap.miniPos.unbind(9);
			}
		}
		
		if(!glow) {
			e3d.flashLightSampler.unbind(10);
			e3d.flashLightTex.unbind(10);
		}
		
		if(!e3d.shadowPass && parallaxMap != null) {
			sampler.unbind(4);
			parallaxMap.unbind(4);
		}
        
        if(!e3d.shadowPass && (scrollXSpeed != 0 || scrollYSpeed != 0)) {
            shr.setUniform2f(shr.uniforms[UNI_UV_OFFSET], 0, 0);
        }
        
        if(alphaTest) {
			shr.setUniformf(shr.uniforms[UNI_ALPHA_THRESHOLD], -1);
        }
        
        super.unbind(e3d);
        shr.unbind();
    }

}
