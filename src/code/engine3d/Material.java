package code.engine3d;

import code.utils.IniFile;
import code.utils.assetManager.ReusableContent;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class Material extends ReusableContent {
    
    public static final int OFF = 0, BLEND = 1, ADD = 2, SUB = 3, SCR = 4, MAX = 5, MUL = 6;
    
    protected int blendMode = OFF;
	
	protected boolean zWrite;
	protected int depthFunc;
    
    public Material(E3D e3d) {
        
    }
    
    public void load(E3D e3d, String name, IniFile ini) {
		String tmp = ini.getDef("blend", blendMode == BLEND ? "blend" : "0");
        
        if(tmp.equals("blend")) blendMode = BLEND;
        else if(tmp.equals("add")) blendMode = ADD;
        else if(tmp.equals("sub")) blendMode = SUB;
        else if(tmp.equals("scr")) blendMode = SCR;
        else if(tmp.equals("max")) blendMode = MAX;
        else if(tmp.equals("mul")) blendMode = MUL;
        else blendMode = OFF;
		
		zWrite = ini.getInt("z_write", 1) == 1;
		
		tmp = ini.getDef("depth_func", "gequal");
		
		if(tmp.equals("always")) depthFunc = GL33C.GL_ALWAYS;
		else if(tmp.equals("never")) depthFunc = GL33C.GL_NEVER;
		else if(tmp.equals("less")) depthFunc = GL33C.GL_LESS;
		else if(tmp.equals("greater")) depthFunc = GL33C.GL_GREATER;
		else if(tmp.equals("equal")) depthFunc = GL33C.GL_EQUAL;
		else if(tmp.equals("notequal")) depthFunc = GL33C.GL_NOTEQUAL;
		else if(tmp.equals("lequal")) depthFunc = GL33C.GL_LEQUAL;
		else if(tmp.equals("gequal")) depthFunc = GL33C.GL_GEQUAL;
    }
    
    public void setBlendMode(int mode) {
        this.blendMode = mode;
    }
    
    public void bind(E3D e3d, long time) {
        if(blendMode != OFF) {
            GL33C.glEnable(GL33C.GL_BLEND);
            GL33C.glBlendEquation(GL33C.GL_FUNC_ADD);

            if(blendMode == BLEND) {
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == ADD) {
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_ONE, GL33C.GL_ONE,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == SUB) {
				GL33C.glBlendEquation(GL33C.GL_FUNC_REVERSE_SUBTRACT);
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_ONE, GL33C.GL_ONE,
                        GL33C.GL_ZERO, GL33C.GL_ONE);

            } else if(blendMode == SCR) {
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_ONE, GL33C.GL_ONE_MINUS_SRC_COLOR,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == MAX) {
                GL33C.glBlendEquation(GL33C.GL_MAX);
                //GL33C.glBlendFunc(GL33C.GL_ONE_MINUS_DST_COLOR, GL33C.GL_DST_COLOR);
                GL33C.glBlendFuncSeparate(GL33C.GL_ONE, GL33C.GL_ONE,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == MUL) {
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_DST_COLOR, GL33C.GL_ZERO,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            }
        }
		
		if(!zWrite) GL33C.glDepthMask(false);
		
		//gequal is default depth func
		if(depthFunc != GL33C.GL_GEQUAL) {
			GL33C.glDepthFunc(depthFunc);
		}
    }
    
    public void unbind(E3D e3d) {
        if(blendMode != OFF) {
            GL33C.glDisable(GL33C.GL_BLEND);
        }
		
		if(!zWrite) GL33C.glDepthMask(true);
		
		if(depthFunc != GL33C.GL_GEQUAL) {
			GL33C.glDepthFunc(GL33C.GL_GEQUAL);
		}
    }

}
