package code.engine3d;

import code.utils.assetManager.ReusableContent;

/**
 *
 * @author Roman Lahin
 */
public class Model extends ReusableContent {
    
    private Mesh[] meshes;
    
    Model(Mesh[] meshes) {
        this.meshes = meshes;
    }
    
    public ReusableContent use() {return this;}
    public ReusableContent free() {return this;}
    
    public void destroy() {
        for(Mesh mesh : meshes) {
            mesh.destroy();
        }
        
        meshes = null;
    }
    
    public int getUsingCount() {
        int max = 0;
        
        for(Mesh mesh : meshes) {
            max = Math.max(max, mesh.getUsingCount());
        }
        
        return max;
    }
    
    public Mesh[] getMeshes() {
        return meshes;
    }
    
    public Mesh get(int index) {
        return meshes[index];
    }

}
