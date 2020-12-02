package code.engine3d;

import code.math.Vector3D;
import code.utils.IniFile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 *
 * @author Roman Lahin
 */
public class Material {
    
    public static final int OFF = 0, BLEND = 1, ADD = 2, SUB = 3, SCR = 4, MAX = 5;
    public static final int UNDEFINED = -2, DEFAULT = -1;
    
    public Texture tex;
    
    public boolean mipMapping;
    public boolean linearInterpolation;
    public boolean alphaTest;
    public boolean wrapClamp;
    
    public int blendMode = OFF;
    public String lightGroupName;
    public LightGroup lightGroup = null;

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
        
        tmp = ini.getDef("lightgroup", "default");
        if(tmp.equals("0")) lightGroupName = null;
        else lightGroupName = tmp;
        
        wrapClamp = ini.getDef("wrap", "repeat").equals("clamp");
    }
    
    private void bindImpl() {
        int id = tex.id;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        
        int mag = linearInterpolation ? GL11.GL_LINEAR : GL11.GL_NEAREST;
        int interp = mipMapping ?
                (linearInterpolation ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_NEAREST_MIPMAP_LINEAR)
                : mag;

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, interp);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mag);
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapClamp?GL11.GL_CLAMP:GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapClamp?GL11.GL_CLAMP:GL11.GL_REPEAT);
        
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
    }
    
    public void bind() {
        bindImpl();
        GL11.glDisable(GL11.GL_LIGHTING);
    }
    
    public void bind(E3D e3d, float x, float y, float z, float xs, float ys, float zs) {
        bindImpl();
        
        if(!LightGroup.lightgroups.isEmpty() && lightGroupName != null) {
            GL11.glEnable(GL11.GL_LIGHTING);
            
            if(lightGroup == null) {
                for(LightGroup lightGroup2 : LightGroup.lightgroups) {
                    if(lightGroup2.name.equals(lightGroupName)) {
                        lightGroup = lightGroup2;
                        break;
                    }
                }

                if(lightGroup == null) {
                    lightGroupName = null;
                    GL11.glDisable(GL11.GL_LIGHTING);
                    return;
                }
            }
            
            lightGroup.bind(e3d, x, y, z, xs, ys, zs);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        
    }

}
