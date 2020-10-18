package code.utils;

/**
 *
 * @author Roman Lahin
 */
public class DisposableContent {

    public boolean neverUnload = false;
    
    public void destroy() {}
    public DisposableContent lock() {neverUnload = true; return this;}
    public DisposableContent unlock() {neverUnload = false; return this;}
    
}
