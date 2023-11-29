package code.utils.assetManager;

/**
 *
 * @author Roman Lahin
 */
public class ReusableContent {
    
    protected int using = 0;
    public boolean neverUnload = false;
    
    public void destroy() {}
    
    public ReusableContent lock() {neverUnload = true; return this;}
    public ReusableContent unlock() {neverUnload = false; return this;}
    
    public ReusableContent use() {using++; return this;}
    public ReusableContent free() {using--; return this;}
    
    public int getUsingCount() {
        return using;
    }

}
