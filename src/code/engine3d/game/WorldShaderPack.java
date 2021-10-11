package code.engine3d.game;

import code.engine3d.E3D;
import code.engine3d.Shader;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class WorldShaderPack {
    
    public int uvOffset = -1, alphaThreshold = -1;
	//public int specular = -1, roughness = -1;
	
	public Shader getShader(E3D e3d, WorldMaterial mat) {
		ArrayList<String> defs = new ArrayList<>();
		
		if(!mat.glow) {
			defs.add("LIGHT");
			defs.add("MAX_LIGHTS "+E3D.MAX_LIGHTS);
			
			/*if(mat.normalMap != null) defs.add("NORMALMAP");
			if(mat.specular != null) {
				defs.add("SPECULAR");
				
				if(mat.specularMap != null) defs.add("SPECULARMAP");
				if(mat.roughnessMap != null) defs.add("ROUGHNESSMAP");
			}
			
			if(mat.parallaxMap != null) defs.add("PARALLAXMAP");
			if(mat.emissionMap != null) defs.add("EMISSIONMAP");*/
		}
		
		String[] defsarr = defs.toArray(new String[defs.size()]);
		if(defsarr.length == 0) defsarr = null;
		Shader shader = e3d.getShader(/*mat.fastShader ? */"world"/* : "fragworld"*/, defsarr);
		
		if(e3d.isShaderWasCreated()) {
			shader.bind();
			
			shader.addTextureUnit("albedoMap", 0);
			
			if(defs.contains("LIGHT")) {
				shader.addUniformBlock(e3d.lights, "lights");
				/*if(defs.contains("NORMALMAP")) shader.addTextureUnit("normalMap", 1);
			
				if(defs.contains("SPECULAR")) {
					if(specular == -1) specular = shader.getUniformIndex("specular");
					if(roughness == -1) roughness = shader.getUniformIndex("roughness");
					
					if(defs.contains("SPECULARMAP")) shader.addTextureUnit("specularMap", 2);
					if(defs.contains("ROUGHNESSMAP")) shader.addTextureUnit("roughnessMap", 3);
				}
				
				if(defs.contains("PARALLAXMAP")) shader.addTextureUnit("parallaxMap", 4);
				if(defs.contains("EMISSIONMAP")) shader.addTextureUnit("emissionMap", 5);*/
			}
			
			shader.addUniformBlock(e3d.fog, "fog");
			
			if(uvOffset == -1) uvOffset = shader.getUniformIndex("uvOffset");
			if(alphaThreshold == -1) alphaThreshold = shader.getUniformIndex("alphaThreshold");
			
			shader.unbind();
		}
		
		return shader;
	}

}
