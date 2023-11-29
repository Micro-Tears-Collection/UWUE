package code.ui.itemList;

import code.engine3d.HudRender;
import code.ui.TextView;
import code.utils.font.BMFont;
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class TextItem extends ListItem {
    
    private String text;
    private char lineDivider = '\0';
    private boolean hCenter;
    
    private ArrayList<String> lines;
    private BMFont font;
    private int width;
    
    public TextItem(String text, BMFont font, char lineDivider) {
        lines = new ArrayList<>();
        
        this.text = text;
        this.font = font;
        this.lineDivider = lineDivider;
    }
    
    public TextItem(String text, BMFont font) {
        lines = new ArrayList<>();
        
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
        list.setYScroll(yScroll);
        list.limitYScroll();
    }
    
    public void updateHeight(int w) {
        width = w;
        lines.clear();
        TextView.createLines(text, lines, lineDivider, font, w);
        
        height = lines.size() * font.getHeight();
    }
    
    public void draw(HudRender hudRender, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {
        
        TextView.draw(hudRender, lines, font, 
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
    
    public boolean leftRightCheck(int x, int y, int mx, int my) {
        String ch = getCharUnderCursor(x, y, mx, my);
        
        if("<".equals(ch)) {
            onLeft();
            return true;
        } else if(">".equals(ch)) {
            onRight();
            return true;
        }
        
        return false;
    }
    
    public void onClick(int x, int y, int mx, int my) {
        if(!leftRightCheck(x, y, mx, my)) super.onClick(x, y, mx, my);
    }

}
