package code.engine3d;

import code.math.Vector3D;
import java.util.Vector;
import org.lwjgl.opengl.GL33C;

/**
 *
 * @author Roman Lahin
 */
public class HudRender {
    
    private E3D e3d;
    
    private int windowColVBO, arrowVBO, cubeVBO;
    private int windowVAO, arrowVAO, cubeVAO;
    
    private Shader texShader, noTexShader, vertColShader;
    private int uvOffMulUni, clipUni, colorUniform;
    
    public HudRender(E3D e3d) {
        this.e3d = e3d;
        clips = new Vector();
        
        arrowVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, arrowVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    -1, -1, 0, 1, 0, 0,
                    -1, 1, 0,
                }, GL33C.GL_STATIC_DRAW);
        
        windowColVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, windowColVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new float[]{
                    1, 1, 1, 0.75f, 1, 1, 1, 0.75f,
                    1, 1, 1, 0, 1, 1, 1, 0
                }, GL33C.GL_STATIC_DRAW);
        
        cubeVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, cubeVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    0, 0, 0, 0, 0, 1,
                    0, 0, 0, 0, 1, 0,
                    0, 0, 0, 1, 0, 0,
                    
                    1, 1, 1, 1, 1, 0,
                    1, 1, 1, 1, 0, 1,
                    1, 1, 1, 0, 1, 1,
                    
                    1, 0, 1, 1, 0, 0,
                    1, 0, 1, 0, 0, 1,
                    
                    0, 1, 0, 1, 1, 0,
                    0, 1, 0, 0, 1, 1,
                    
                    0, 0, 1, 0, 1, 1,
                    1, 0, 0, 1, 1, 0,
                }, GL33C.GL_STATIC_DRAW);
        
        //Window vao
        windowVAO = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(windowVAO);
            
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, e3d.rectCoordVBO);
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_SHORT, false, 0, 0);

        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, windowColVBO);
        GL33C.glVertexAttribPointer(3, 4, GL33C.GL_FLOAT, false, 0, 0);
        
        //Arrow vao
        arrowVAO = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(arrowVAO);
            
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, arrowVBO);
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_SHORT, false, 0, 0);
        
        //Cube vao
        cubeVAO = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(cubeVAO);
            
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, cubeVBO);
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_SHORT, false, 0, 0);
        
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
        GL33C.glBindVertexArray(0); //Unloads the current VAO when done.
        
        //Shaders
        noTexShader = e3d.getShader("hud");
        texShader = e3d.getShader("hud", new String[]{"TEXTURED"});
        vertColShader = e3d.getShader("hud", new String[]{"VERTEX_COLOR"});
        
        noTexShader.use();
        texShader.use();
        vertColShader.use();
        
        //Uniforms
        noTexShader.addUniformBlock(e3d.matrices, "Mats");
        texShader.addUniformBlock(e3d.matrices, "Mats");
        vertColShader.addUniformBlock(e3d.matrices, "Mats");
        
        texShader.bind();
        
        uvOffMulUni = texShader.getUniformIndex("uvOffMul");
        clipUni = texShader.getUniformIndex("clipXY");
        colorUniform = texShader.getUniformIndex("color");
        texShader.addTextureUnit(0);
        
        texShader.unbind();
    }
    
    public void destroy() {
        noTexShader.free();
        texShader.free();
        vertColShader.free();
        noTexShader = texShader = vertColShader = null;
        
        GL33C.glDeleteVertexArrays(windowVAO);
        GL33C.glDeleteVertexArrays(arrowVAO);
        GL33C.glDeleteVertexArrays(cubeVAO);
        
        GL33C.glDeleteBuffers(windowColVBO);
        GL33C.glDeleteBuffers(arrowVBO);
        GL33C.glDeleteBuffers(cubeVBO);
        
        clips = null;
        e3d = null;
    }
    
    public final void drawRect(Texture tex, float x, float y, float w, float h, 
            float u1, float v1, float u2, float v2,
            int color, float a) {
        
        texShader.bind();
        texShader.setUniform4f(uvOffMulUni, u1, v1, u2-u1, v2-v1);
        
        drawRect(tex, x, y, w, h,
                ((color>>16)&255) / 255f,
                ((color>>8)&255) / 255f,
                (color&255) / 255f,
                a,
                texShader, colorUniform);
        
        texShader.unbind();
    }
    
    public final void drawRect(Texture tex, float x, float y, float w, float h, 
            int color, float a) {
        drawRect(tex, x, y, w, h, 0, 0, 1, 1, color, a);
    }
    
    public final void drawRect(float x, float y, float w, float h, int color, float a) {
        noTexShader.bind();
        
        drawRect(null, x, y, w, h,
                ((color>>16)&255) / 255f,
                ((color>>8)&255) / 255f,
                (color&255) / 255f,
                a,
                noTexShader, colorUniform);
        
        noTexShader.unbind();
    }
    
    public final void drawRect(Texture tex, float x, float y, float w, float h, 
            float r, float g, float b, float a,
            Shader shader, int colorUniform) {
        
        if(shader != null && colorUniform != -1) {
            shader.setUniform4f(colorUniform, r, g, b, a);
        }
        
        if(tex != null) tex.bind(false, false, false, 0);

        e3d.drawRect(x, y, w, h, tex != null);
        
        if(tex != null) tex.unbind(0);
    }
    
    public void drawWindow(float x, float y, float w, float h) {
        vertColShader.bind();
        
        vertColShader.setUniform4f(colorUniform, 0, 0, 0, 1);
        
        GL33C.glEnable(GL33C.GL_BLEND);
        GL33C.glBlendEquation(GL33C.GL_FUNC_ADD);
        GL33C.glBlendFunc(GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA);
        
        GL33C.glBindVertexArray(windowVAO);
        GL33C.glEnableVertexAttribArray(0);
        GL33C.glEnableVertexAttribArray(3);
        
        e3d.tmpM.identity();
        e3d.tmpM.translate(x, y, 0);
        e3d.tmpM.scale(w, -getWindowYBorder(), 0);
        e3d.setModelView(e3d.tmpM.get(e3d.tmpMf));
        
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
        
        e3d.tmpM.identity();
        e3d.tmpM.translate(x, y+h, 0);
        e3d.tmpM.scale(w, getWindowYBorder(), 0);
        e3d.setModelView(e3d.tmpM.get(e3d.tmpMf));
        
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
        
        GL33C.glDisableVertexAttribArray(0);
        GL33C.glDisableVertexAttribArray(3);
        GL33C.glBindVertexArray(0);
        
        vertColShader.unbind();
        
        drawRect(x, y, w, h, 0, 0.75f);
    }
    
    public void drawCube(Vector3D min, Vector3D max, int color, float a) {
        noTexShader.bind();
        
        e3d.tmpM.identity();
        e3d.tmpM.translate(min.x, min.y, min.z);
        e3d.tmpM.scale(max.x-min.x, max.y-min.y, max.z-min.z);
        e3d.invCam.mul(e3d.tmpM);
        
        e3d.setModelView(e3d.tmpM.get(e3d.tmpMf));
        e3d.invCam.set(e3d.invCamf);
        
        float r = ((color>>16)&255) / 255f;
        float g = ((color>>8)&255) / 255f;
        float b = (color&255) / 255f;
        
        noTexShader.setUniform4f(colorUniform, r, g, b, a);
        
        GL33C.glEnable(GL33C.GL_BLEND);
        GL33C.glBlendEquation(GL33C.GL_FUNC_ADD);
        GL33C.glBlendFunc(GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA);
        
        GL33C.glBindVertexArray(cubeVAO);
        GL33C.glEnableVertexAttribArray(0);
        
        GL33C.glDrawArrays(GL33C.GL_LINES, 0, 24);
        
        GL33C.glDisableVertexAttribArray(0);
        GL33C.glBindVertexArray(0);
        
        GL33C.glDisable(GL33C.GL_BLEND);
        
        noTexShader.unbind();
    }
    
    public final int getWindowYBorder() {
        return 15;
    }
    
    public final void drawArrow(float x, float y, float w, float h, float rot, int color, float a) {
        noTexShader.bind();
        
        e3d.tmpM.identity();
        e3d.tmpM.scale(w/2, h/2, 0);
        e3d.tmpM.rotate((float)Math.toRadians(rot), 0, 0, 1);
        e3d.tmpM.setTranslation(x, y, 0);
        
        e3d.setModelView(e3d.tmpM.get(e3d.tmpMf));
        
        float r = ((color>>16)&255) / 255f;
        float g = ((color>>8)&255) / 255f;
        float b = (color&255) / 255f;
        
        noTexShader.setUniform4f(colorUniform, r, g, b, a);
        
        GL33C.glEnable(GL33C.GL_BLEND);
        GL33C.glBlendEquation(GL33C.GL_FUNC_ADD);
        GL33C.glBlendFunc(GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA);
        
        GL33C.glBindVertexArray(arrowVAO);
        GL33C.glEnableVertexAttribArray(0);
        
        GL33C.glDrawArrays(GL33C.GL_TRIANGLES, 0, 3);
        
        GL33C.glDisableVertexAttribArray(0);
        GL33C.glBindVertexArray(0);
        
        GL33C.glDisable(GL33C.GL_BLEND);
        noTexShader.unbind();
    }
    
    private boolean clipEnabled;
    private float cx1, cy1, cx2, cy2;
    
    public final void clip(float x, float y, float cw, float ch) {
        clipImpl(
                x / e3d.w * 2 - 1, 
                y / e3d.h * 2 - 1,
                (x+cw) / e3d.w * 2 - 1, 
                (y+ch) / e3d.h * 2 - 1);
    }
    
    private final void clipImpl(float cx1, float cy1, float cx2, float cy2) {
        GL33C.glEnable(GL33C.GL_CLIP_DISTANCE0);
        GL33C.glEnable(GL33C.GL_CLIP_DISTANCE1);
        GL33C.glEnable(GL33C.GL_CLIP_DISTANCE2);
        GL33C.glEnable(GL33C.GL_CLIP_DISTANCE3);
        
        this.cx1 = cx1; this.cy1 = cy1;
        this.cx2 = cx2; this.cy2 = cy2;
        clipEnabled = true;
        
        texShader.bind();
        texShader.setUniform4f(clipUni, cx1, cy1, cx2, cy2);
        noTexShader.bind();
        noTexShader.setUniform4f(clipUni, cx1, cy1, cx2, cy2);
        vertColShader.bind();
        vertColShader.setUniform4f(clipUni, cx1, cy1, cx2, cy2);
        vertColShader.unbind();
    }
    
    public final void disableClip() {
        GL33C.glDisable(GL33C.GL_CLIP_DISTANCE0);
        GL33C.glDisable(GL33C.GL_CLIP_DISTANCE1);
        GL33C.glDisable(GL33C.GL_CLIP_DISTANCE2);
        GL33C.glDisable(GL33C.GL_CLIP_DISTANCE3);
        clipEnabled = false;
    }
    
    public Vector clips;
    
    public final void pushClip() {
        clips.add(new Float(cx1));
        clips.add(new Float(cy1));
        clips.add(new Float(cx2));
        clips.add(new Float(cy2));
        clips.add(new Boolean(clipEnabled));
    }
    
    public final void popClip() {
        clipEnabled = (Boolean) clips.elementAt(clips.size()-1);
        cy2 = (Float) clips.elementAt(clips.size()-2);
        cx2 = (Float) clips.elementAt(clips.size()-3);
        cy1 = (Float) clips.elementAt(clips.size()-4);
        cx1 = (Float) clips.elementAt(clips.size()-5);
        
        clips.removeElementAt(clips.size()-1);
        clips.removeElementAt(clips.size()-1);
        clips.removeElementAt(clips.size()-1);
        clips.removeElementAt(clips.size()-1);
        clips.removeElementAt(clips.size()-1);
        
        if(clipEnabled) clipImpl(cx1, cy1, cx2, cy2);
        else disableClip();
    }

}
