package code.engine3d.game;

import code.engine3d.E3D;
import code.engine3d.Shader;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class WorldShaderPack {
    
    public int uvOffset, alphaThreshold;
	
	public Shader getShader(E3D e3d, WorldMaterial mat) {
		ArrayList<String> defs = new ArrayList<>();
		
		if(!mat.glow) {
			defs.add("LIGHT");
			
			/*if(mat.normalMap != null) defs.add("NORMALMAP");
			if(mat.specular != null || mat.specularMap != null) {
				defs.add("SPECULAR");
				if(mat.specularMap != null) defs.add("SPECULARMAP");
				
				if(mat.roughnessMap != null) defs.add("ROUGHNESSMAP");
			}*/
		}
		
		String[] defsarr = defs.toArray(new String[defs.size()]);
		Shader shader = e3d.getShader("world", defsarr);
		
		if(e3d.isShaderWasCreated()) {
			shader.bind();
			
			shader.addTextureUnit("albedoMap", 0);
			
			if(defs.contains("LIGHT")) {
				shader.addUniformBlock(e3d.lights, "lights");
				/*if(defs.contains("NORMALMAP")) shader.addTextureUnit("normalMap", 1);
			
				if(defs.contains("SPECULAR")) {
					if(defs.contains("SPECULARMAP")) shader.addTextureUnit("specularMap", 2);
					else specular = shader.getUniformIndex("specular");
				
					if(defs.contains("ROUGHNESSMAP")) shader.addTextureUnit("roughnessMap", 3);
					else roughness = shader.getUniformIndex("roughness");
				}*/
			}
			
			shader.addUniformBlock(e3d.fog, "fog");
			
			uvOffset = shader.getUniformIndex("uvOffset");
			alphaThreshold = shader.getUniformIndex("alphaThreshold");
			
			shader.unbind();
		}
		
		return shader;
	}

}
