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
    private int selectedBox = 0;
    
    public TextBoxItem(Screen scr, BMFont font, final int boxesCount) {
        this.scr = scr;
        this.font = font;
        boxes = new TextBox[boxesCount];
        
        for(int i=0; i<boxesCount; i++) {
            
            final int index = i;
            boxes[i] = new TextBox(scr, font) {
                public void onEnter() {
                    super.onEnter();
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
        
        height = boxes[0].getHeight();
    }
    
    public void draw(E3D e3d, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {
        int divW = font.stringWidth("x");
            
        for(int i=0; i<boxes.length; i++) {
            TextBox box = boxes[i];
            
            box.x = windowX+(box.w+divW)*i;
            box.y = windowY+y+yScroll;
            box.draw(e3d, selected && i == selectedBox, selColor);
            
            if(i != boxes.length-1) font.drawString("x", box.x+box.w, box.y, 1, color);
        }
    }
    
    public void onSelected() {
        super.onSelected();
        if(selectedBox == -1) selectedBox = 0;
    }
    
    public void mouseUpdate(int x, int y, int mx, int my) {
        selectedBox = -1;
        
        for(int i=0; i<boxes.length; i++) {
            TextBox box = boxes[i];
            
            if(box.isInBox(mx, my)) {
                selectedBox = i;
                break;
            }
        }
    }
    
    public boolean onLeft() {
        if(selectedBox == -1) selectedBox = 0;
        else if(selectedBox == 0) selectedBox = boxes.length-1;
        else selectedBox--;
        
        return true;
    }
    
    public boolean onRight() {
        if(selectedBox == -1) selectedBox = 0;
        else if(selectedBox == boxes.length-1) selectedBox = 0;
        else selectedBox++;
        
        return true;
    }
    
    public void onEnter() {
        if(selectedBox != -1) {
            scr.openTextBox(boxes[selectedBox]);
        }
    }

}
