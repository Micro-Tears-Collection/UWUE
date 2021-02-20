package code.ui.itemList;

import code.engine3d.E3D;
import code.ui.TextView;
import code.utils.font.BMFont;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class TextItem extends ListItem {
    
    private String text;
    private char lineDivider = '\0';
    private boolean hCenter;
    
    private Vector<String> lines;
    private BMFont font;
    private int width;
    
    public TextItem(String text, BMFont font, char lineDivider) {
        lines = new Vector();
        
        this.text = text;
        this.font = font;
        this.lineDivider = lineDivider;
    }
    
    public TextItem(String text, BMFont font) {
        lines = new Vector();
        
        this.font = font;
        this.text = text;
    }
    
    public TextItem setHCenter(boolean hCenter) {
        this.hCenter = hCenter;
        return this;
    }
    
    public void setText(String text, ItemList list) {
        this.text = text;
        int yScroll = list.getYScroll();
        list.updateList();
        list.scroll(yScroll - list.getYScroll());
    }
    
    public void updateHeight(int w) {
        width = w;
        lines.removeAllElements();
        TextView.createLines(text, lines, lineDivider, font, w);
        
        height = lines.size() * font.getHeight();
    }
    
    public void draw(E3D e3d, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {
        
        TextView.draw(e3d, lines, font, 
                windowX, windowY, windowW, windowH, 
                yScroll+y, hCenter, selected?selColor:color);
    }
    
    private String getCharUnderCursor(int x, int y, int mx, int my) {
        int lineIndex = (my - y - this.y) / font.getHeight();
        if(lineIndex < 0 || lineIndex >= lines.size()) return null;
        
        String line = lines.get(lineIndex);
        int offsetX = hCenter ? (width - font.stringWidth(line)) >> 1 : 0;
        
        return font.getCharByX(line, mx - x + offsetX);
    }
    
    public boolean onClickCheck(int x, int y, int mx, int my) {
        String ch = getCharUnderCursor(x, y, mx, my);
        
        if("<".equals(ch)) {
            onLeft();
			return false;
        } else if(">".equals(ch)) {
            onRight();
			return false;
        }
        
        return true;
    }
    
    public void onClick(int x, int y, int mx, int my) {
        if(onClickCheck(x, y, mx, my)) super.onClick(x, y, mx, my);
    }

}
