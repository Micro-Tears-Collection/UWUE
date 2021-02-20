package code.ui.itemList;

import code.engine.Screen;
import code.engine3d.E3D;
import code.ui.TextBox;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class TextBoxItem extends ListItem {
    
    private Screen scr;
    private BMFont font;
    private TextBox[] boxes;
    
    public TextBoxItem(Screen scr, BMFont font, final int boxesCount) {
        this.scr = scr;
        this.font = font;
        boxes = new TextBox[boxesCount];
        
        for(int i=0; i<boxesCount; i++) {
            
            final int index = i;
            boxes[i] = new TextBox(scr, font) {
                public void onEnter() {
                    super.onEnter();
                    
                    if(index != boxesCount-1) {
                        this.scr.openTextBox(boxes[index+1]);
                    }
                }
            };
        }
    }
    
    public TextBoxItem setText(String text) {
        this.boxes[0].setText(text);
        return this;
    }
    
    public TextBoxItem setText(String[] text) {
        for(int i=0; i<boxes.length; i++) {
            boxes[i].setText(text[i]);
        }
        return this;
    }
    
    public TextBoxItem setOnlyDigit(boolean set) {
        for(int i=0; i<boxes.length; i++) {
            boxes[i].onlyDigits(set);
        }
        return this;
    }
    
    public TextBox getBox(int id) {
        return boxes[id];
    }
    
    public void updateHeight(int w) {
        int divW = font.stringWidth("x");
        int boxW = (w-divW*(boxes.length-1)) / boxes.length;
            
        for(TextBox box : boxes) {
            box.setXYW(0, 0, boxW);
        }
        
        height = font.getHeight();
    }
    
    public void draw(E3D e3d, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {
        int divW = font.stringWidth("x");
            
        for(int i=0; i<boxes.length; i++) {
            TextBox box = boxes[i];
            
            box.x = windowX+(box.w+divW)*i;
            box.y = windowY+y+yScroll;
            box.draw(e3d, selected, selColor);
            
            if(i != boxes.length-1) font.drawString("x", box.x+box.w, box.y, 1, selected?selColor:color);
        }
    }
    
    public void onEnter() {
        scr.openTextBox(boxes[0]);
    }
    
    public void onClick(int x, int y, int mx, int my) {
        for(TextBox box : boxes) {
            
            if(box.isInBox(mx, my)) {
                scr.openTextBox(box);
                break;
            }
        }
    }

}
