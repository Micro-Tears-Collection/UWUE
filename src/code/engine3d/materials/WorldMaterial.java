package code.engine3d.materials;

import code.engine3d.Material;
import code.engine3d.E3D;
import code.engine3d.Shader;
import code.engine3d.shaders.WorldShaderPack;
import code.engine3d.Texture;
import code.utils.IniFile;
import code.utils.assetManager.ReusableContent;

/**
 *
 * @author Roman Lahin
 */
public class WorldMaterial extends Material {
    
    public static final int UNDEFINED = -2, DEFAULT = -1;
    //private static boolean lightingWasEnabled;
    
    public Texture tex;
    public WorldShaderPack shaderPack;
    
    public boolean alphaTest, linearInterpolation, mipMapping, wrapClamp, glow;
    
    public float scrollXSpeed, scrollYSpeed;
    
    public WorldMaterial(E3D e3d) {
        super(e3d);
        init(e3d);
    }

    public WorldMaterial(E3D e3d, Texture tex) {
        super(e3d);
        this.tex = tex;
        init(e3d);
    }
    
    private void init(E3D e3d) {
        shaderPack = WorldShaderPack.get(e3d, "world", new String[][]{null});
    }
    
    public void load(IniFile ini) {
        String tmp = ini.get("alpha_test");
        
        if(tmp != null && tmp.equals("1")) {
            alphaTest = true; blendMode = OFF;
        } else if(tmp != null && tmp.equals("blend")) {
            alphaTest = true; blendMode = BLEND;
        }
        super.load(ini);
        
        linearInterpolation = ini.getInt("linear", 0) == 1;
        mipMapping = ini.getInt("mipmap", 1) == 1;
        wrapClamp = ini.getDef("wrap", "repeat").equals("clamp");
        
        scrollXSpeed = ini.getFloat("scroll_x", 0);
        scrollYSpeed = ini.getFloat("scroll_y", 0);
        
        glow = ini.getInt("glow", 0) == 1;
    }
    
    public ReusableContent use() {
        if(using == 0) {
            if(tex != null) tex.use();
            if(shaderPack != null) shaderPack.use();
        }
        
        super.use();
        return this;
    }
    
    public ReusableContent free() {
        if(using == 1) {
            if(tex != null) tex.free();
            if(shaderPack != null) shaderPack.free();
        }
        
        super.free();
        return this;
    }
    
    public void destroy() {
        tex = null;
        shaderPack = null;
    }
    
    public void bind(E3D e3d, long time) {
        Shader shader = shaderPack.shaders[0];
        shader.bind();
        
        if(tex != null) tex.bind(linearInterpolation, mipMapping, wrapClamp, 0);
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shaderPack.uvOffset, time * scrollXSpeed / 1000, -time * scrollYSpeed / 1000);
        }
        
        if(alphaTest) {
            /*GL33C.glEnable(GL33C.GL_ALPHA_TEST);
            GL33C.glAlphaFunc(GL33C.GL_GREATER, blendMode == OFF?0.5f:0);*/
        }
        
        if(glow) {
            /*lightingWasEnabled = GL33C.glGetInteger(GL33C.GL_LIGHTING) == GL33C.GL_TRUE;
            if(lightingWasEnabled) GL33C.glDisable(GL33C.GL_LIGHTING);*/
        }
        
        super.bind(e3d, time);
    }
    
    public void unbind() {
        Shader shader = shaderPack.shaders[0];
        
        if(tex != null) tex.unbind(0);
        
        if(scrollXSpeed != 0 || scrollYSpeed != 0) {
            shader.setUniform2f(shaderPack.uvOffset, 0, 0);
        }
        
        if(alphaTest) {
            /*GL33C.glDisable(GL33C.GL_ALPHA_TEST);
            GL33C.glAlphaFunc(GL33C.GL_ALWAYS, 0);*/
        }
        
        //if(glow && lightingWasEnabled) GL33C.glEnable(GL33C.GL_LIGHTING);
        
        super.unbind();
        shader.unbind();
    }

}
