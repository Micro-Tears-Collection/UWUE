package code.engine3d.game.lighting;

import code.engine3d.E3D;
import code.engine3d.FrameBuffer;
import code.engine3d.Shader;
import code.engine3d.Texture;
import code.math.Vector3D;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman
 */
public class RSMGrid {
	
	private Vector3D min, max;
	private int w, h, d;
	
	private FrameBuffer gridFB;
	public Texture[] grids; //destroyed in fb
	private Shader rsmShader;
	
	public RSMGrid(E3D e3d, int w, int h, int d) {
		this.w = w;
		this.h = h;
		this.d = d;
		
		grids = new Texture[6];
		
		for(int i=0; i<6; i++) {
			grids[i] = Texture.createTexture(w, h, d, GL33C.GL_R11F_G11F_B10F, GL33C.GL_RGB, GL33C.GL_UNSIGNED_INT_10F_11F_11F_REV);
		}
		
		gridFB = new FrameBuffer(w, h, null, false, grids);
		
		rsmShader = e3d.getShader("rsmgrid");
		rsmShader.use();
		
		rsmShader.bind();
		
		rsmShader.addTextureUnit("shadowMapFlux", 0);
		rsmShader.addTextureUnit("shadowMapNorm", 1);
		rsmShader.addTextureUnit("shadowMapPos", 2);
		
		rsmShader.storeUniform(0, "rsmZ");
		rsmShader.storeUniform(1, "rsmMin");
		rsmShader.storeUniform(2, "rsmMax");
		rsmShader.addUniformBlock(e3d.shadowMapData, "shadowMapData");
		
		rsmShader.unbind();
	}
	
	public void destroy() {
		gridFB.destroy();
		rsmShader.free();
	}
	
	public void updateRSM(E3D e3d, ShadowMap shadowMap, Vector3D minPos, Vector3D maxPos) {
		rsmShader.bind();
		
		e3d.rsmSampler.bind(0);
		e3d.rsmSampler.bind(1);
		e3d.rsmSampler.bind(2);
		
		shadowMap.miniFlux.bind(0);
		shadowMap.miniNorm.bind(1);
		shadowMap.miniPos.bind(2);
		
		rsmShader.setUniform3f(rsmShader.uniforms[1], minPos.x, minPos.y, minPos.z);
		rsmShader.setUniform3f(rsmShader.uniforms[2], maxPos.x, maxPos.y, maxPos.z);
		
		gridFB.bind();
		
		for(int z=0; z<d; z++) {
			for(int i=0; i<6; i++) gridFB.set3DTexZ(i, z);
			rsmShader.setUniformf(rsmShader.uniforms[0], z / (d - 1));
			
			GL33C.glBindVertexArray(e3d.rectVAO);
			GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
			GL33C.glBindVertexArray(0);
		}
		
		gridFB.unbind();
		
		e3d.rsmSampler.unbind(0);
		e3d.rsmSampler.unbind(1);
		e3d.rsmSampler.unbind(2);
		
		shadowMap.miniFlux.unbind(0);
		shadowMap.miniNorm.unbind(1);
		shadowMap.miniPos.unbind(2);
		
		rsmShader.unbind();
	}
	
}
