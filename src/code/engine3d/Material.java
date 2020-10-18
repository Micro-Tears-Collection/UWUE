package code.engine3d;

import code.utils.IniFile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 *
 * @author Roman Lahin
 */
public class Material {
    
    public static final int OFF = 0, BLEND = 1, ADD = 2, SUB = 3, SCR = 4, MAX = 5;
    
    public Texture tex;
    
    public boolean mipMapping;
    public boolean linearInterpolation;
    public boolean alphaTest;
    
    public int blendMode = OFF;

    public Material(Texture tex) {
        this.tex = tex;
    }
    
    public void load(IniFile ini) {
        linearInterpolation = ini.getInt("linear", 0) == 1;
        
        String tmp = ini.get("alpha_test");
        boolean defBlend = false;
        
        if(tmp != null && tmp.equals("1")) {
            alphaTest = true; defBlend = false;
        } else if(tmp != null && tmp.equals("blend")) {
            alphaTest = true; defBlend = true;
        }
        
        mipMapping = ini.getInt("mipmap", 1) == 1;
        
        tmp = ini.getDef("blend", defBlend ? "blend" : "0");
        
        if(tmp.equals("blend")) blendMode = BLEND;
        else if(tmp.equals("add")) blendMode = ADD;
        else if(tmp.equals("sub")) blendMode = SUB;
        else if(tmp.equals("scr")) blendMode = SCR;
        else if(tmp.equals("max")) blendMode = MAX;
        else blendMode = OFF;
    }
    
    public void bind() {
        int id = tex.id;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        
        int mag = linearInterpolation ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        int interp = mipMapping ?
                (linearInterpolation ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_NEAREST_MIPMAP_LINEAR)
                : mag;

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interp);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mag);
        
        if(alphaTest) {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, blendMode == OFF?0.5f:0);
        } else {
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_ALWAYS, 0);
        }
        
        if(blendMode == OFF) {
            GL11.glDisable(GL11.GL_BLEND);
        } else { 
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendEquation(GL14.GL_FUNC_ADD);
            
            if(blendMode == BLEND) GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            else if(blendMode == ADD) GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            else if(blendMode == SUB) GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
            else if(blendMode == SCR) GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
            else if(blendMode == MAX) {
                GL14.glBlendEquation(GL14.GL_MAX);
                //GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_DST_COLOR);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            }
        }
        
        /*GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, 0);
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION, 0.00001F);
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, new float[]{1,1,1,1});
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, new float[]{0,0,0,1});
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, new float[]{0,485,0,1});
        
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, new float[]{0,0,0,1});
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, new float[]{1,1,1,1});
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, new float[]{0.2f,0.2f,0.2f,1});
        GL11.glEnable(GL11.GL_LIGHT0);*/
        
    }

}
