package code.utils.assetManager;


/**
 *
 * @author Roman Lahin
 */
public class CachedText extends ReusableContent {
    
    String code;

    public CachedText(String code) {
        this.code = code;
    }
    
    public static CachedText get(String path) {
        CachedText cachedText = (CachedText) AssetManager.get("TXT_" + path);
        if(cachedText != null) return cachedText;
        
        cachedText = new CachedText(AssetManager.loadString(path));
        AssetManager.add("TXT_" + path, cachedText);
        
        return cachedText;
    }
    
    public String toString() {
        return code;
    }
    
}
