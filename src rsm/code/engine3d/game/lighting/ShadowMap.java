package code.engine3d.game.lighting;

import code.engine3d.E3D;
import code.engine3d.FrameBuffer;
import code.engine3d.HudRender;
import code.engine3d.Texture;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman
 */
public class ShadowMap {
	
	public int res;
	
	private FrameBuffer buffer;
	public Texture depthTex, fluxTex, normTex, posTex; //destroyed in frame buffer
	
	private FrameBuffer miniFluxBuf, miniNormBuf, miniPosBuf;
	public Texture miniFlux, miniNorm, miniPos;
	
	private static final int lowRes = 11;
	
	public ShadowMap(int res) {
		this.res = res;
		
		depthTex = Texture.createTexture(res, res, 0, GL33C.GL_DEPTH_COMPONENT16, GL33C.GL_DEPTH_COMPONENT, GL33C.GL_FLOAT);
		
		fluxTex = Texture.createTexture(res, res, 0, GL33C.GL_R11F_G11F_B10F, GL33C.GL_RGB, GL33C.GL_UNSIGNED_INT_10F_11F_11F_REV);
		//normTex = Texture.createTexture(res, res, GL33C.GL_RGB10_A2, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_INT_2_10_10_10_REV);
		normTex = Texture.createTexture(res, res, 0, GL33C.GL_RGB5_A1, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_SHORT_5_5_5_1);
		posTex = Texture.createTexture(res, res, 0, GL33C.GL_RGB16F, GL33C.GL_RGB, GL33C.GL_FLOAT);
		
		buffer = new FrameBuffer(res, res, depthTex, false, new Texture[] {fluxTex, normTex, posTex});
		
		miniFlux = Texture.createTexture(lowRes, lowRes, 0, GL33C.GL_R11F_G11F_B10F, GL33C.GL_RGB, GL33C.GL_UNSIGNED_INT_10F_11F_11F_REV);
		//miniNorm = Texture.createTexture(lowRes, lowRes, 0, GL33C.GL_RGB10_A2, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_INT_2_10_10_10_REV);
		miniNorm = Texture.createTexture(lowRes, lowRes, 0, GL33C.GL_RGB5_A1, GL33C.GL_RGBA, GL33C.GL_UNSIGNED_SHORT_5_5_5_1);
		miniPos = Texture.createTexture(lowRes, lowRes, 0, GL33C.GL_RGB16F, GL33C.GL_RGB, GL33C.GL_FLOAT);
		
		miniFluxBuf = new FrameBuffer(lowRes, lowRes, null, false, new Texture[] {miniFlux});
		miniNormBuf = new FrameBuffer(lowRes, lowRes, null, false, new Texture[] {miniNorm});
		miniPosBuf = new FrameBuffer(lowRes, lowRes, null, false, new Texture[] {miniPos});
	}
	
	public void destroy() {
		buffer.destroy();
		miniFluxBuf.destroy();
		miniNormBuf.destroy();
		miniPosBuf.destroy();
	}
	
	public void bind() {
		buffer.bind();
	}
	
	public void unbind() {
		buffer.unbind();
	}
	
	public void downscale(E3D e3d, HudRender hud) {
		e3d.prepare2D(0, 0, lowRes, lowRes);
		
		fluxTex.genMipmaps();
		normTex.genMipmaps();
		hud.setSampling(false, true, false);
		
		miniFluxBuf.bind();
		hud.drawRect(fluxTex, 0, 0, lowRes, lowRes, 0, 0, 1, 1, 0xffffff, 1);
		
		//hud.setSampling(false, false, false);
		
		miniNormBuf.bind();
		hud.drawRect(normTex, 0, 0, lowRes, lowRes, 0, 0, 1, 1, 0xffffff, 1);
		
		hud.setSampling(false, false, false);
		
		miniPosBuf.bind();
		hud.drawRect(posTex, 0, 0, lowRes, lowRes, 0, 0, 1, 1, 0xffffff, 1);
		
		miniPosBuf.unbind();
		
		hud.setDefaultSampling();
	}
	
}
