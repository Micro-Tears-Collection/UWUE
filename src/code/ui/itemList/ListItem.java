package code.ui.itemList;

import code.engine3d.HudRender;

/**
 *
 * @author Roman Lahin
 */
public class ListItem {
    
    int y, height;
    boolean skip, selected;
    
    public ListItem() {}
    
    public ListItem setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }
    
    public void updateHeight(int w) {}
    
    public void draw(HudRender hudRender, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {}
    
    public void onSelected() {}
    public void mouseUpdate(int x, int y, int mx, int my) {}
    public boolean onMouseScroll(int y) {return false;}
    
    public void onEnter() {}
    public void onClick(int x, int y, int mx, int my) {onEnter();}
    
    public boolean onLeft() {return false;}
    public boolean onRight() {return false;}

}
