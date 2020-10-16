package code.utils;

/**
 *
 * @author Roman Lahin
 */
public class CachedContent {
    
    public boolean using = true, neverUnload = false;
    
    public void destroy() {}
    public void free() {using = false;}
    public void lock() {neverUnload = true;}
    public void inlock() {neverUnload = false;}

}
