package code.engine3d;

import code.engine.Window;
import code.engine3d.game.WorldMaterial;
import code.engine3d.instancing.MeshInstance;
import code.engine3d.instancing.RenderInstance;
import code.math.Vector3D;
import code.utils.IniFile;
import code.utils.StringTools;
import code.utils.assetManager.AssetManager;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ArrayList;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class E3D {
    //todo split game stuff from e3d somehow
	
	/*
	4 AMBIENT COLOR + padding

	Lights count x
	4 POSITION + DIR/POINT
	4 COLOR + padding
	4 SPOT_DIR + SPOT_CUTOFF
	*/
	public static final int LIGHT_SIZE = 12, MAX_LIGHTS = 16;
	
    private Window win;
	
	public int maxAA;
	public boolean anisotropicSupported;
	public float maxAnisotropy;
    
    public boolean mode2D;
    public float w, h;
    public float fovX, fovY;
    public Matrix4f tmpM, invCam, proj;
    public Matrix3f tmpM3;
    public FloatBuffer tmpMf, tmpM3f, invCamf, projf;
    
    int rectCoordVBO, rectuvVBO, rectuvMVBO, rectNormals;
    public int rectVAO, spriteVAO;
    
    public UniformBlock matrices;
	private FloatBuffer fogf;
    public UniformBlock fog;
	private FloatBuffer lightsf;
    public UniformBlock lights;
	
    private ArrayList<RenderInstance> postDraw;
	
    public E3D(Window win) {
        this.win = win;
        postDraw = new ArrayList<RenderInstance>();
		
        maxAA = GL33C.glGetInteger(GL33C.GL_MAX_SAMPLES);
		
		GL33C.glGetError();
		maxAnisotropy = GL33C.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		anisotropicSupported = GL33C.glGetError() == GL33C.GL_NO_ERROR;
        
        tmpM = new Matrix4f();
        tmpMf = MemoryUtil.memAllocFloat(4*4);
        
        tmpM3 = new Matrix3f();
        tmpM3f = MemoryUtil.memAllocFloat(3*3);
        
        invCam = new Matrix4f();
        invCamf = MemoryUtil.memAllocFloat(4*4);
        
        proj = new Matrix4f();
        projf = MemoryUtil.memAllocFloat(4*4);
        
		//Create stuff for sprites/billboards and 2d rendering
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
		
        GL33C.glEnableVertexAttribArray(0);
        
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
                    0, 0, 1, 0, 0, 0, 1, 0,
                    0, 0, 1, 0, 0, 0, 1, 0
                }, GL33C.GL_STATIC_DRAW);

        GL33C.glVertexAttribPointer(2, 4, GL33C.GL_SHORT, false, 0, 0);
        
        GL33C.glEnableVertexAttribArray(0);
        GL33C.glEnableVertexAttribArray(1);
        GL33C.glEnableVertexAttribArray(2);
        
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0);
        GL33C.glBindVertexArray(0);
        
        matrices = new UniformBlock((4*4 * 2) * 4, 0);
		
		fog = new UniformBlock((6) * 4, 1);
        fogf = MemoryUtil.memAllocFloat(6);
		disableFog();
		
		lights = new UniformBlock((4 + LIGHT_SIZE * MAX_LIGHTS) * 4, 2);
        lightsf = MemoryUtil.memAllocFloat(4 + LIGHT_SIZE * MAX_LIGHTS);
		setAmbientLight(1, 1, 1);
		for(int i=0; i<MAX_LIGHTS; i++) disableLight(i);
		sendLights();
    }
    
    public void destroy() {
		postDraw.clear();
		postDraw = null;
		
        MemoryUtil.memFree(tmpMf);
        MemoryUtil.memFree(tmpM3f);
        MemoryUtil.memFree(invCamf);
        MemoryUtil.memFree(projf);
        tmpMf = tmpM3f = invCamf = projf = null;
        
        matrices.destroy();
        matrices = null;
        
        fog.destroy();
        fog = null;
		MemoryUtil.memFree(fogf);
		fogf = null;
		
		lights.destroy();
		lights = null;
		MemoryUtil.memFree(lightsf);
		lightsf = null;
        
        GL33C.glDeleteVertexArrays(rectVAO);
        GL33C.glDeleteVertexArrays(spriteVAO);
        
        GL33C.glDeleteBuffers(rectCoordVBO);
        GL33C.glDeleteBuffers(rectuvVBO);
        GL33C.glDeleteBuffers(rectuvMVBO);
        GL33C.glDeleteBuffers(rectNormals);
    }
    
    public void setCam(Vector3D camera, float rotX, float rotY) {
        invCam.identity();
        invCam.rotateY((float) Math.toRadians(rotY));
        invCam.rotateX((float) Math.toRadians(rotX));
        invCam.setTranslation(camera.x, camera.y, camera.z);
        
        invCam.invert();
        invCam.get(invCamf);
    }
    
    public void setProjectionPers(float fov, int w, int h, float znear, float zfar) {
        proj.identity().perspective((float) Math.toRadians(fov), (float) w / h, znear, zfar);
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
		//GL33C.glEnable(GL33C.GL_FRAMEBUFFER_SRGB);
    }
    
    public void prepare2D(int xx, int yy, int ww, int hh) {
        w = ww; h = hh;
        mode2D = true;
        
        GL33C.glViewport(xx, yy, ww, hh);
        setProjectionOrtho(ww, hh);
        
        GL33C.glDisable(GL33C.GL_DEPTH_TEST);
        GL33C.glDisable(GL33C.GL_CULL_FACE);
		//GL33C.glDisable(GL33C.GL_FRAMEBUFFER_SRGB);
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

	private static final float SRGB_ALPHA = 0.055f;

	// Converts a single linear channel to srgb
	public static final float linear_to_srgb(float channel) {
		return channel;
		/*if(channel <= 0.0031308)
			return 12.92f * channel;
		else
			return (float) ((1.0 + SRGB_ALPHA) * Math.pow(channel, 1.0 / 2.4) - SRGB_ALPHA);*/
	}

	// Converts a single srgb channel to rgb
	public static final float srgb_to_linear(float channel) {
		return channel;
		/*if(channel <= 0.04045)
			return channel / 12.92f;
		else
			return (float) Math.pow((channel + SRGB_ALPHA) / (1.0 + SRGB_ALPHA), 2.4);*/
	}
    
    public void disableFog() {
		fogf.put(0, 0);
		fogf.put(1, 0);
		fogf.put(2, 0);
		
		fogf.put(3, 0);
        fogf.put(4, 1);
		fogf.put(5, 0);
		
		fog.bind();
		fog.sendData(fogf, 0);
		fog.unbind();
    }
    
    public void setLinearFog(float start, float end, float[] col) {
		for(int x=0; x<3; x++) fogf.put(x, srgb_to_linear(col[x]));
		fogf.put(3, 0);
		
        fogf.put(4, end / (end - start));
		fogf.put(5, 1 / (end - start));
		
		fog.bind();
		fog.sendData(fogf, 0);
		fog.unbind();
    }
    
    public void setExpFog(float density, float[] col) {
		for(int x=0; x<3; x++) fogf.put(x, srgb_to_linear(col[x]));
		fogf.put(3, 1);
		
        fogf.put(4, density);
		
		fog.bind();
		fog.sendData(fogf, 0);
		fog.unbind();
    }
	
	public void setAmbientLight(float r, float g, float b) {
		lightsf.put(0, r);
		lightsf.put(1, g);
		lightsf.put(2, b);
	}
	
	public void setPointLight(int i, Vector3D pos, float[] col) {
		int offset = 4 + i * LIGHT_SIZE;
		
		Vector3D tmp = new Vector3D(pos);
		tmp.transform(invCamf);
		
		lightsf.put(offset + 0, tmp.x);
		lightsf.put(offset + 1, tmp.y);
		lightsf.put(offset + 2, tmp.z);
		lightsf.put(offset + 3, 1);
		
		for(int x=0; x<3; x++) lightsf.put(offset + x + 4, col[x]);
		
		lightsf.put(offset + 8 + 3, -1);
	}
	
	public void setSpotLight(int i, Vector3D pos, Vector3D dir, float spotCutOff, float[] col) {
		int offset = 4 + i * LIGHT_SIZE;
		
		Vector3D tmp = new Vector3D(pos);
		tmp.transform(invCamf);
		
		lightsf.put(offset + 0, tmp.x);
		lightsf.put(offset + 1, tmp.y);
		lightsf.put(offset + 2, tmp.z);
		lightsf.put(offset + 3, 1);
		
		for(int x=0; x<3; x++) lightsf.put(offset + 4 + x, col[x]);
		
		tmp.set(dir);
		tmp.transform(invCamf, false);
		
		lightsf.put(offset + 8 + 0, tmp.x);
		lightsf.put(offset + 8 + 1, tmp.y);
		lightsf.put(offset + 8 + 2, tmp.z);
		lightsf.put(offset + 8 + 3, (float) Math.cos(Math.toRadians(spotCutOff)));
	}
	
	public void setDirectionalLight(int i, Vector3D dir, float[] col) {
		int offset = 4 + i * LIGHT_SIZE;
		
		Vector3D tmp = new Vector3D(dir);
		tmp.transform(invCamf, false);
		
		lightsf.put(offset + 0, tmp.x);
		lightsf.put(offset + 1, tmp.y);
		lightsf.put(offset + 2, tmp.z);
		lightsf.put(offset + 3, 0);
		
		for(int x=0; x<3; x++) lightsf.put(offset + x + 4, col[x]);
		
		lightsf.put(offset + 8 + 3, -1);
	}
	
	public void disableLight(int i) {
		int offset = 4 + i * LIGHT_SIZE;
		
		lightsf.put(offset, 0);
		lightsf.put(offset + 1, 0);
		lightsf.put(offset + 2, 1);
		
		lightsf.put(offset + 3, 0);
		for(int x=0; x<3; x++) lightsf.put(offset + x + 4, 0);
		
		lightsf.put(offset + 8 + 0, 0);
		lightsf.put(offset + 8 + 1, 0);
		lightsf.put(offset + 8 + 2, 0);
		lightsf.put(offset + 8 + 3, -1);
	}
	
	public void sendLights() {
		lights.bind();
		lights.sendData(lightsf, 0);
		lights.unbind();
	}
	
    public void add(RenderInstance obj) {
        postDraw.add(obj);
    }
    
    public void postRender() {
        //Finally draw 3d
        sort(postDraw);
        for(RenderInstance object : postDraw) object.renderImmediate(this);
        
        postDraw.clear();
    }
    
    private static void sort(ArrayList<RenderInstance> list) {
        for(int i=list.size()-1; i>=1; i--) {
            RenderInstance nearest = null;
            int pos = 0;
            
            for(int x=0; x<=i; x++) {
                RenderInstance m1 = list.get(x);
                
                //m1 ближе чем m2
                if(nearest == null || 
                        (m1.sortZ > nearest.sortZ && m1.drawOrder >= nearest.drawOrder) ||
                        m1.drawOrder > nearest.drawOrder) {
                    nearest = m1;
                    pos = x;
                }
            }
            
            list.set(pos, list.get(i));
            list.set(i, nearest);
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

        if(sendUVM) GL33C.glEnableVertexAttribArray(1); //uvm
        
        GL33C.glDrawArrays(GL33C.GL_TRIANGLE_FAN, 0, 4);
        
        if(sendUVM) GL33C.glDisableVertexAttribArray(1); //uvm

        GL33C.glBindVertexArray(0);
        GL33C.glDisable(GL33C.GL_BLEND);
    }

    public void takeScreenshot() {
		GL33C.glGetError();
		
		int imgW = win.getWidth(), imgH = win.getHeight();
        ByteBuffer buffer = MemoryUtil.memAlloc(imgW * imgH * 3);
		
        GL33C.glReadBuffer(GL33C.GL_FRONT);
        GL33C.glReadPixels(0, 0, imgW, imgH, GL33C.GL_RGB, GL33C.GL_UNSIGNED_BYTE, buffer);
        
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
            
			String path = "screenshots/" + date + ".png";
			
			//Flip image vertically
			for(int y = 0, y2 = imgH-1; y < imgH/2; y++, y2--) {
				for(int x = 0; x > imgW; x++) {
					
					for(int rgb=0; rgb<3; rgb++) {
						byte tmp = buffer.get((x + y*imgW) * 3 + rgb);
						buffer.put((x + y*imgW) * 3 + rgb, buffer.get((x + y2*imgW) * 3 + rgb));
						buffer.put((x + y2*imgW) * 3 + rgb, tmp);
					}
					
				}
			}
			
			boolean success = STBImageWrite.stbi_write_png(path, imgW, imgH, 3, buffer, 0);
            MemoryUtil.memFree(buffer);
			
			if(!success) throw new Exception("stbi_write_png failed to write image " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Loading scripts
    
    public Model getModel(String path, String currentDirectory) {
		path = AssetManager.updatePath(path, currentDirectory);
        Model model = (Model) AssetManager.get("MODEL_" + path);

        if(model != null) return model;
        
        model = new Model(ModelLoader.loadObj(this, path));
        AssetManager.add("MODEL_" + path, model);
		
		String modelDirectory = AssetManager.getDirectory(path);
		for(Mesh mesh : model.getMeshes()) {
			String[] mats = mesh.mats;
			
			for(int i=0; i<mats.length; i++) {
				mats[i] = AssetManager.updatePath(mats[i], modelDirectory);
			}
		}

        return model;
    }
	
	public MeshInstance[] getMeshInstances(String mdlName, String currentDirectory) {
		return getMeshInstancesImpl(mdlName, currentDirectory, false);
	}
	
	public MeshInstance getMeshInstance(String mdlName, String currentDirectory) {
		return getMeshInstancesImpl(mdlName, currentDirectory, true)[0];
	}
	
	private MeshInstance[] getMeshInstancesImpl(String mdlName, String currentDirectory, boolean one) {
		String[] mdlNameSplit = StringTools.cutOnStrings(mdlName, '|');
		Model mdl = getModel(mdlNameSplit[0], currentDirectory);
		
		HashMap<String, String> toReplace = new HashMap();
		for(int i=0; i<(mdlNameSplit.length-1)/2; i++) {
			String oldMat = mdlNameSplit[1 + i*2];
			String newMat = mdlNameSplit[1 + i*2 + 1];
			
			toReplace.put(oldMat, AssetManager.updatePath(newMat, currentDirectory));
		}
		
		MeshInstance[] instances = new MeshInstance[one ? 1 : mdl.getMeshes().length];
		for(int i=0; i<instances.length; i++) {
			Mesh mesh = mdl.get(i);
			
			//todo dont duplicate mat arrays?
			Material[] mats = new Material[mesh.mats.length];
			for(int x=0; x<mats.length; x++) {
				mats[x] = getMaterial(mesh.mats[x], currentDirectory, toReplace);
			}
			
			instances[i] = new MeshInstance(mesh, mats);
		}
		
		return instances;
	}
    
	private boolean shaderWasCreated = false;
	
    public Shader getShader(String path) {
        return getShader(path, null);
    }
    
    public Shader getShader(String path, String[] defs) {
		if(defs != null) Arrays.sort(defs);
        String defsName = (defs != null) ? String.valueOf(Arrays.hashCode(defs)) : "";
        
        Shader shader = (Shader) AssetManager.get("SHRD_" + path + defsName);
        
		shaderWasCreated = shader == null;
        if(shaderWasCreated) {
            shader = new Shader(path, defs);
            AssetManager.add("SHRD_" + path + defsName, shader);
        }
        
        return shader;
    }
	
	public boolean isShaderWasCreated() {
		return shaderWasCreated;
	}
    
    public Texture getTexture(String path, String currentDirectory) {
		path = AssetManager.updatePath(path, currentDirectory);
		
        Texture tex = (Texture) AssetManager.get("TEX_" + path);
        if(tex != null) return tex;
        
        if(path.equals("null")) {
            tex = new Texture(0, 1, 1, 0);
            tex.lock();
        } else {
            tex = Texture.loadTexture(path);
        }
        
        if(tex != null) {
            AssetManager.add("TEX_" + path, tex);
            return tex;
        }
        
        return getTexture("null", currentDirectory);
    }
    
    public Material getMaterial(String name, String currentDirectory) {
        return getMaterial(name, currentDirectory, null);
    }
    
    public Material getMaterial(String name, String currentDirectory, HashMap<String,String> replace) {
        String[] lines = StringTools.cutOnStrings(name, ';');
		
		//Add current directory to path
		name = AssetManager.updatePath(name, currentDirectory);
        
		//Replace material if it in replace lift
        String path = lines[0];
        String replaced = replace != null ? replace.get(path) : null;
        if(replaced != null) path = replaced;
		
		//Change name if we replaced material
		name = path + name.substring(lines[0].length());
		
		//Get material
        Material mat = (Material) AssetManager.get("MAT_" + name);
        if(mat != null) return mat;
        
		//No material found - create new
		//Get material preferences from ini
		IniFile matIni = new IniFile(lines, false);
		MaterialsFile matsFile = getMaterialsFile(path, currentDirectory);
		matsFile.copyMaterialIni(path, matIni);
		
        mat = new WorldMaterial(this);
        mat.load(this, name, matIni);
        
        AssetManager.add("MAT_" + name, mat);
        
        return mat;
    }
	
	public MaterialsFile getMaterialsFile(String matPath, String currentDirectory) {
		String matsFilePath = matPath.substring(0, matPath.lastIndexOf('/') + 1) + "materials.ini";
		
		//Add current directory to path
		matsFilePath = AssetManager.updatePath(matsFilePath, currentDirectory);
		
        MaterialsFile matsFile = (MaterialsFile) AssetManager.get("MATFILE_" + matsFilePath);
        if(matsFile != null) return matsFile;
        
        matsFile = new MaterialsFile(matsFilePath);
        
        AssetManager.add("MATFILE_" + matsFilePath, matsFile);
        
        return matsFile;
	}

}
