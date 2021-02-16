package code.engine3d;

import code.utils.IniFile;
import code.utils.StringTools;
import java.util.Hashtable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 *
 * @author Roman Lahin
 */
public class Material {
    
    public static final int OFF = 0, BLEND = 1, ADD = 2, SUB = 3, SCR = 4, MAX = 5;
    public static final int UNDEFINED = -2, DEFAULT = -1;
    private static boolean lightingWasEnabled;
    
    public Texture tex;
    
    public boolean mipMapping, linearInterpolation,
            alphaTest, wrapClamp, glow;
    
    public int blendMode = OFF;
    
    public float scrollXSpeed, scrollYSpeed;
    public float scrollX, scrollY;
    
    public static Material get(String name, 
            Hashtable<String,String> replace, String prefix, String postfix) {
        String[] lines = StringTools.cutOnStrings(name, ';');
        IniFile stuff = new IniFile(lines, false);
        
        String path = lines[0];
        
        if(replace != null && replace.get(path) != null) {
            path = replace.get(path);
        } else if(prefix != null || postfix != null) {
            //Trenchbroom handling
            StringBuffer sb = new StringBuffer();
            
            if(prefix != null) sb.append(prefix);
            sb.append(path);
            if(postfix != null) sb.append(postfix);
            path = sb.toString();
        }
        
        Texture tex = Texture.get(path);
        Material mat = new Material(tex);
        
        mat.load(stuff);
        
        return mat;
    }
    
    public static Material get(String name) {
        return get(name, null, null, null);
    }

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
        
        wrapClamp = ini.getDef("wrap", "repeat").equals("clamp");
        
        scrollXSpeed = ini.getFloat("scroll_x", 0);
        scrollYSpeed = ini.getFloat("scroll_y", 0);
        
        glow = ini.getInt("glow", 0) == 1;
    }
    
    public void animate(long time) {
        scrollX = time * scrollXSpeed / 1000;
        scrollY = time * scrollYSpeed / 1000;
    }
    
    public void bind() {
        if(tex != null) tex.bind(linearInterpolation, mipMapping, wrapClamp, 0);
        
        if(scrollX != 0 || scrollY != 0) {
            int matMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glTranslatef(scrollX, -scrollY, 0);
            GL11.glMatrixMode(matMode);
        }
        
        if(alphaTest) {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, blendMode == OFF?0.5f:0);
        }
        
        if(glow) {
            lightingWasEnabled = GL11.glGetInteger(GL11.GL_LIGHTING) == GL11.GL_TRUE;
            if(lightingWasEnabled) GL11.glDisable(GL11.GL_LIGHTING);
        }
        
        if(blendMode != OFF) { 
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
    
    public void unbind() {
        if(tex != null) tex.unbind(0);
        
        if(scrollX != 0 || scrollY != 0) {
            int matMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(matMode);
        }
        
        if(alphaTest) {
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_ALWAYS, 0);
        }
        
        if(glow && lightingWasEnabled) GL11.glEnable(GL11.GL_LIGHTING);
        
        if(blendMode != OFF) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

}
