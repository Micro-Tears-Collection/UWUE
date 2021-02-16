package code.utils.assetManager;

/**
 *
 * @author Roman Lahin
 */
public class ReusableContent extends DisposableContent {
    
    public boolean using = true;
    
    public ReusableContent use() {using = true; return this;}
    public ReusableContent free() {using = false; return this;}

}
