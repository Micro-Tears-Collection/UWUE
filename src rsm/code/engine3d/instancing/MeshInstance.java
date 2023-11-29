package code.engine3d.instancing;

import code.engine3d.E3D;
import code.engine3d.Material;
import code.engine3d.Mesh;
import code.math.Vector3D;
import code.utils.IniFile;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Roman Lahin
 */
public class MeshInstance extends RenderInstance {
    
	//todo remove drawMatrix somehow
    public FloatBuffer modelMatrix;
    
    public Mesh mesh;
	public Material[] mats;
    
    public Vector3D min, max;
    public Vector3D middle;

    public boolean collision = true, visible = true;
    
    public MeshInstance(Mesh mesh, Material[] mats) {
        this.mesh = mesh;
		this.mats = mats;
		
		for(Material mat : mats) mat.use();
        mesh.use();
		
        load(mesh.ini);
        
        min = new Vector3D(mesh.min);
        max = new Vector3D(mesh.max);
        
        middle = new Vector3D(mesh.min);
        middle.add(mesh.max); middle.div(2,2,2);
        
        modelMatrix = MemoryUtil.memAllocFloat(4*4);
    }
    
    public void load(IniFile ini) {
        super.load(ini);
        
        collision = ini.getInt("collision", 1) == 1;
        visible = ini.getInt("visible", 1) == 1;
    }
    
    public void destroy() {
        mesh.free();
		mesh = null;
		for(Material mat : mats) mat.free();
		mats = null;
		
        min = max = middle = null;
        
        MemoryUtil.memFree(modelMatrix);
        
        modelMatrix = null;
    }
    
    public void fastIdentityCamera(FloatBuffer invcam) {
        for(int i=0; i<16; i++) modelMatrix.put(i, 0);
        modelMatrix.put(0, 1);
        modelMatrix.put(5, 1);
        modelMatrix.put(10, 1);
        modelMatrix.put(15, 1);
        
        min.set(mesh.min); max.set(mesh.max);
        
		tmpMat.set(invcam);
        updateZ(tmpMat);
    }
    
    public void setTransformation(Vector3D pos, Vector3D rot) {
        tmpMat.identity();
        buildMatrix(pos, rot, tmpMat);
        tmpMat.get(modelMatrix);
        updateBB(modelMatrix);
    }
    
    public void setCamera(Matrix4f tmp, Matrix4f invCam) {
        tmpMat.set(invCam);
        tmp.set(modelMatrix);
        tmpMat.mul(tmp);
        updateZ(tmpMat);
    }
    
    private void updateBB(FloatBuffer mat) {
        Vector3D tmp = new Vector3D();
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        
        for(int i=0; i<8; i++) {
            tmp.set(i>=4?mesh.max.x:mesh.min.x, 
                    ((i/2)&1)==1?mesh.max.y:mesh.min.y, 
                    (i&1)==1?mesh.max.z:mesh.min.z);
            tmp.transform(mat);
            
            min.min(tmp);
            max.max(tmp);
        }
    }
    
    private void updateZ(Matrix4f tmp) {
        middle.set(mesh.min);
        middle.add(mesh.max);
        middle.mul(0.5f, 0.5f, 0.5f);
        middle.transform(tmp.get(tmpMatf));
        
        sortZ = middle.z;
    }
    
    public void render(E3D e3d) {
        if(visible) super.render(e3d);
    }
    
    public void renderImmediate(E3D e3d) {
        //todo somehow check do we need to bind lights?
        //maybe check materials on mesh creation and write result to boolean?
		//or maybe we can send to materials do we need to enable lighting?
        bindLight(e3d, 
                    (min.x+max.x)/2f, (min.y+max.y)/2f, (min.z+max.z)/2f,
                    (max.x-min.x)/2, (max.y-min.y)/2, (max.z-min.z)/2f);
        
        mesh.renderImmediate(e3d, mats, time, modelMatrix);
        
		//maybe instead of unbinding light sources and sending stuff to gpu we can enable GLOW?
        unbindLight(e3d);
    }
}
