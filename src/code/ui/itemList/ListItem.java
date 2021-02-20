package code.ui.itemList;

import code.engine3d.E3D;

/**
 *
 * @author Roman Lahin
 */
public class ListItem {
    
    int y, height;
    boolean skip;
    
    public ListItem() {}
    
    public ListItem setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }
    
    public void updateHeight(int w) {}
    
    public void draw(E3D e3d, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {}
    
    public void onSelected() {}
    public void onEnter() {}
    public void onClick(int x, int y, int mx, int my) {onEnter();}
    
    public void onLeft() {}
    public void onRight() {}

}
