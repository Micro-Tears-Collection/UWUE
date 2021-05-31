package code.engine3d;

import code.engine3d.instancing.Renderable;
import code.engine.Window;
import code.engine3d.Lighting.LightGroup;
import code.engine3d.materials.WorldMaterial;
import code.math.Vector3D;
import code.utils.IniFile;
import code.utils.StringTools;
import code.utils.assetManager.AssetManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class E3D {
    
    private Window win;
    
    public boolean mode2D;
    public float w, h;
    public float fovX, fovY;
    public Matrix4f tmpM, invCam, proj;
    public Matrix3f tmpM3;
    public FloatBuffer tmpMf, tmpM3f, invCamf, projf;
    
    int rectCoordVBO, rectuvVBO, rectuvMVBO, rectNormals;
    public int rectVAO, spriteVAO;
    
    public UniformBlock matrices;
    private final Vector<Renderable> postDraw;
    
    public E3D(Window win) {
        this.win = win;
        postDraw = new Vector();
        
        /*GL33C.glLightModelfv(GL33C.GL_LIGHT_MODEL_AMBIENT, new float[]{1,1,1,1});
        GL33C.glLightModeli(GL33C.GL_LIGHT_MODEL_LOCAL_VIEWER, 1);
        GL33C.glMaterialfv(GL33C.GL_FRONT, GL33C.GL_DIFFUSE, new float[]{1,1,1,1});
        maxLights = GL33C.glGetInteger(GL33C.GL_MAX_LIGHTS);*/
        
        /*for(int i=0; i<maxLights; i++) {
            GL33C.glLightf(GL33C.GL_LIGHT0+i, GL33C.GL_CONSTANT_ATTENUATION, 0);
            GL33C.glLightf(GL33C.GL_LIGHT0+i, GL33C.GL_QUADRATIC_ATTENUATION, 0.0001F * 0.1f);
        }*/
        
        tmpM = new Matrix4f();
        tmpMf = MemoryUtil.memAllocFloat(4*4);
        
        tmpM3 = new Matrix3f();
        tmpM3f = MemoryUtil.memAllocFloat(3*3);
        
        invCam = new Matrix4f();
        invCamf = MemoryUtil.memAllocFloat(4*4);
        
        proj = new Matrix4f();
        projf = MemoryUtil.memAllocFloat(4*4);
        
        //Rectangle data
        rectVAO = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(rectVAO);
        
        rectCoordVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, rectCoordVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    0, 0, 0, 1, 0, 0,
                    1, 1, 0, 0, 1, 0
                }, GL33C.GL_STATIC_DRAW);
            
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_SHORT, false, 0, 0);
        
        rectuvVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, rectuvVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    0, 0, 1, 0,
                    1, 1, 0, 1
                }, GL33C.GL_STATIC_DRAW);

        GL33C.glVertexAttribPointer(1, 2, GL33C.GL_SHORT, false, 0, 0);
        
        //Sprite data
        spriteVAO = GL33C.glGenVertexArrays();
        GL33C.glBindVertexArray(spriteVAO);
            
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, rectCoordVBO);
        GL33C.glVertexAttribPointer(0, 3, GL33C.GL_SHORT, false, 0, 0);
        
        rectuvMVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, rectuvMVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    0, 1, 1, 1,
                    1, 0, 0, 0
                }, GL33C.GL_STATIC_DRAW);

        GL33C.glVertexAttribPointer(1, 2, GL33C.GL_SHORT, false, 0, 0);
        
        rectNormals = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, rectNormals); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, 
                new short[]{
                    0, 0, 1, 0, 0, 1,
                    0, 0, 1, 0, 0, 1
                }, GL33C.GL_STATIC_DRAW);

        GL33C.glVertexAttribPointer(2, 3, GL33C.GL_SHORT, false, 0, 0);
        
        GL33C.glEnableVertexAttribArray(0);
        GL33C.glEnableVertexAttribArray(1);
        GL33C.glEnableVertexAttribArray(2);
        
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0);
        GL33C.glBindVertexArray(0);
        
        matrices = new UniformBlock((4*4 * 2) * 4, 0);
    }
    
    public void destroy() {
        MemoryUtil.memFree(tmpMf);
        MemoryUtil.memFree(tmpM3f);
        MemoryUtil.memFree(invCamf);
        MemoryUtil.memFree(projf);
        tmpMf = tmpM3f = invCamf = projf = null;
        
        matrices.destroy();
        matrices = null;
        
        LightGroup.clear(false);
        
        GL33C.glDeleteVertexArrays(rectVAO);
        GL33C.glDeleteVertexArrays(spriteVAO);
        
        GL33C.glDeleteBuffers(rectCoordVBO);
        GL33C.glDeleteBuffers(rectuvVBO);
        GL33C.glDeleteBuffers(rectuvMVBO);
        GL33C.glDeleteBuffers(rectNormals);
    }
    
    public void setInvCam(Vector3D camera, float rotX, float rotY) {
        invCam.identity();
        invCam.rotateY((float) Math.toRadians(rotY));
        invCam.rotateX((float) Math.toRadians(rotX));
        invCam.setTranslation(camera.x, camera.y, camera.z);
        
        invCam.invert();
        invCam.get(invCamf);
    }
    
    public void setProjectionPers(float fov, int w, int h) {
        proj.identity().perspective((float) Math.toRadians(fov), (float) w / h, 1f, 40000.0f);
        proj.get(projf);
        
        matrices.bind();
        matrices.sendData(projf, 4*4*4);
        matrices.unbind();
        
        fovY = fov;
        fovX = (float)Math.toDegrees(2f*Math.atan((float) (Math.tan(Math.toRadians(fovY/2f)) * w / h)));
    }
    
    public void setProjectionOrtho(int w, int h) {
        proj.identity().ortho(0, w, h, 0, 0, 40000);
        proj.get(projf);
        
        matrices.bind();
        matrices.sendData(projf, 4*4*4);
        matrices.unbind();
    }
    
    public void setModelView(FloatBuffer modelView) {
        matrices.bind();
        matrices.sendData(modelView, 0);
        matrices.unbind();
    }
    
    public void prepare3D(int xx, int yy, int ww, int hh) {
        w = ww; h = hh;
        mode2D = false;
        
        GL33C.glViewport(xx, yy, ww, hh);
        
        GL33C.glEnable(GL33C.GL_DEPTH_TEST);
        GL33C.glEnable(GL33C.GL_CULL_FACE);
        GL33C.glCullFace(GL33C.GL_BACK);
    }
    
    public void prepare2D(int xx, int yy, int ww, int hh) {
        w = ww; h = hh;
        mode2D = true;
        
        GL33C.glViewport(xx, yy, ww, hh);
        setProjectionOrtho(ww, hh);
        
        GL33C.glDisable(GL33C.GL_DEPTH_TEST);
        GL33C.glDisable(GL33C.GL_CULL_FACE);
    }
    
    public void clearZbuffer() {
        GL33C.glClear(GL33C.GL_DEPTH_BUFFER_BIT);
    }
    
    public void clearColor(int color) {
        GL33C.glClearColor(((color>>16)&255) / 255f, 
                ((color>>8)&255) / 255f, 
                (color&255) / 255f, 1);
        
        GL33C.glClear(GL33C.GL_COLOR_BUFFER_BIT);
    }
    
    public void disableFog() {
        //GL33C.glDisable(GL33C.GL_FOG);
    }
    
    public void setLinearFog(float start, float end, float[] color) {
        /*GL33C.glEnable(GL33C.GL_FOG);
        GL33C.glFogfv(GL33C.GL_FOG_COLOR, color);

        GL33C.glFogi(GL33C.GL_FOG_MODE, GL33C.GL_LINEAR);
        GL33C.glFogf(GL33C.GL_FOG_START, start);
        GL33C.glFogf(GL33C.GL_FOG_END, end);*/
    }
    
    public void setExpFog(float density, float[] color) {
        /*GL33C.glEnable(GL33C.GL_FOG);
        GL33C.glFogfv(GL33C.GL_FOG_COLOR, color);

        GL33C.glFogi(GL33C.GL_FOG_MODE, GL33C.GL_EXP);
        GL33C.glFogf(GL33C.GL_FOG_DENSITY, density);*/
    }
    
    public void add(Renderable obj) {
        postDraw.add(obj);
    }
    
    public void postRender() {
        //Finally draw 3d
        sort(postDraw);
        for(Renderable object : postDraw) object.renderImmediate(this);
        
        postDraw.removeAllElements();
    }
    
    private static void sort(Vector<Renderable> list) {
        for(int i=list.size()-1; i>=1; i--) {
            Renderable nearest = null;
            int pos = 0;
            
            for(int x=0; x<=i; x++) {
                Renderable m1 = list.elementAt(x);
                
                //m1 ближе чем m2
                if(nearest == null || 
                        (m1.sortZ > nearest.sortZ && m1.drawOrder >= nearest.drawOrder) ||
                        m1.drawOrder > nearest.drawOrder) {
                    nearest = m1;
                    pos = x;
                }
            }
            
            list.setElementAt(list.elementAt(i), pos);
            list.setElementAt(nearest, i);
        }
    }
    
    public void drawRect(float x, float y, float w, float h, boolean sendUVM) {
        tmpM.identity();
        tmpM.translate(x, y, 0);
        tmpM.scale(w, h, 0);
        tmpM.get(tmpMf);

        setModelView(tmpMf);
        
        GL33C.glEnable(GL33C.GL_BLEND);
        GL33C.glBlendEquation(GL33C.GL_FUNC_ADD);
        GL33C.glBlendFunc(GL33C.GL_SRC_ALPHA, GL33C.GL_ONE_MINUS_SRC_ALPHA);

        GL33C.glBindVertexArray(rectVAO);

        GL33C.glEnableVertexAttribArray(0); //pos
        if(sendUVM) GL33C.glEnableVertexAttribArray(1); //uvm
        
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
        
        GL33C.glDisableVertexAttribArray(0); //pos
        if(sendUVM) GL33C.glDisableVertexAttribArray(1); //uvm

        GL33C.glBindVertexArray(0);
        GL33C.glDisable(GL33C.GL_BLEND);
    }

    public void takeScreenshot() {
        GL33C.glReadBuffer(GL33C.GL_FRONT);
        ByteBuffer buffer = MemoryUtil.memAlloc(win.getWidth() * win.getHeight() * 4);
        GL33C.glReadPixels(0, 0, win.getWidth(), win.getHeight(), GL33C.GL_RGBA, GL33C.GL_UNSIGNED_BYTE, buffer);
        
        int error = GL33C.glGetError();
        if(error != 0) System.out.println("takeScreenshot GL error: " + error);
        
        try {
            File file = new File("screenshots/");
            if(!file.exists()) file.mkdir();
            
            Calendar cal = Calendar.getInstance();
            String date = cal.get(Calendar.YEAR) + "-"
                    + (cal.get(Calendar.MONTH) + 1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH) + " "
                    + cal.get(Calendar.HOUR_OF_DAY) + "." + cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);
            
            file = new File("screenshots/" + date + ".png");
            BufferedImage image = new BufferedImage(win.getWidth(), win.getHeight(), BufferedImage.TYPE_INT_RGB);
            
            for(int x = 0; x < win.getWidth(); x++) {
                for(int y = 0; y < win.getHeight(); y++) {
                    int i = (x + (win.getWidth() * y)) * 4;
                    int r = buffer.get(i) & 255;
                    int g = buffer.get(i + 1) & 255;
                    int b = buffer.get(i + 2) & 255;
                    image.setRGB(x, win.getHeight() - (y + 1), (255 << 24) | (r << 16) | (g << 8) | b);
                }
            }
            
            MemoryUtil.memFree(buffer);
            ImageIO.write(image, "PNG", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxAA() {
        return GL33C.glGetInteger(GL33C.GL_MAX_SAMPLES);
    }
    
    //Loading scripts
    
    public Model getModel(String path, String prefix, String postfix) {
        Model model = (Model) AssetManager.get("MODEL_" + path);

        if(model != null) return model;
        
        model = new Model(MeshLoader.loadObj(this, path));
        AssetManager.add("MODEL_" + path, model);

        return model;
    }
    
    public Shader getShader(String path) {
        return getShader(path, null);
    }
    
    public Shader getShader(String path, String[] defs) {
        String defsName = (defs != null) ? String.valueOf(Arrays.hashCode(defs)) : "";
        
        Shader shader = (Shader) AssetManager.get("SHRD_" + path + defsName);
        
        if(shader == null) {
            shader = new Shader(path, defs);
            AssetManager.add("SHRD_" + path + defsName, shader);
        }
        
        return shader;
    }
    
    public ShaderPack getShaderPack(String path, String[][] defs) {
        String defsName = (defs != null) ? String.valueOf(Arrays.hashCode(defs)) : "";
        
        ShaderPack shaderPack = (ShaderPack) AssetManager.get("SHDRPCK_" + path + defsName);
        if(shaderPack != null) return shaderPack;
        
        shaderPack = new ShaderPack(this, path, defs);
        AssetManager.add("SHDRPCK_" + path + defsName, shaderPack);
        
        return shaderPack;
    }
    
    public Texture getTexture(String name) {
        Texture tex = (Texture) AssetManager.get("TEX_" + name);
        if(tex != null) return tex;
        
        if(name.equals("null")) {
            tex = new Texture(0, 1, 1);
            tex.lock();
        } else {
            tex = Texture.loadTexture(name);
        }
        
        if(tex != null) {
            AssetManager.add("TEX_" + name, tex);
            return tex;
        }
        
        return getTexture("null");
    }
    
    public Material getMaterial(String name) {
        return getMaterial(name, null);
    }
    
    public Material getMaterial(String name, Hashtable<String,String> replace) {
        Material mat = (Material) AssetManager.get("MAT_" + name);
        if(mat != null) return mat;
        
        String[] lines = StringTools.cutOnStrings(name, ';');
        
        String path = lines[0];
        String replaced = replace != null ? replace.get(path) : null;
        if(replaced != null) path = replaced;
        
        Texture tex = getTexture(path);
        mat = new WorldMaterial(this, tex);
        mat.load(new IniFile(lines, false));
        
        AssetManager.add("MAT_" + name, mat);
        
        return mat;
    }

}
