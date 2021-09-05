package code.engine3d;

import code.utils.IniFile;
import code.utils.assetManager.ReusableContent;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class Material extends ReusableContent {
    
    public static final int OFF = 0, BLEND = 1, ADD = 2, SUB = 3, SCR = 4, MAX = 5;
    
    protected int blendMode = OFF;
    
    public Material(E3D e3d) {
        
    }
    
    public void load(E3D e3d, String name, IniFile ini) {
		String tmp = ini.getDef("blend", blendMode == BLEND ? "blend" : "0");
        
        if(tmp.equals("blend")) blendMode = BLEND;
        else if(tmp.equals("add")) blendMode = ADD;
        else if(tmp.equals("sub")) blendMode = SUB;
        else if(tmp.equals("scr")) blendMode = SCR;
        else if(tmp.equals("max")) blendMode = MAX;
        else blendMode = OFF;
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
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_ZERO, GL33C.GL_ONE_MINUS_SRC_COLOR,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == SCR) {
                GL33C.glBlendFuncSeparate(
                        GL33C.GL_ONE, GL33C.GL_ONE_MINUS_SRC_COLOR,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            } else if(blendMode == MAX) {
                GL33C.glBlendEquation(GL33C.GL_MAX);
                //GL33C.glBlendFunc(GL33C.GL_ONE_MINUS_DST_COLOR, GL33C.GL_DST_COLOR);
                GL33C.glBlendFuncSeparate(GL33C.GL_ONE, GL33C.GL_ONE,
                        GL33C.GL_ONE, GL33C.GL_ZERO);

            }
        }
    }
    
    public void unbind() {
        if(blendMode != OFF) {
            GL33C.glDisable(GL33C.GL_BLEND);
        }
    }

}
