package code.ui;

import code.engine.Screen;
import code.engine3d.E3D;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class TextBox {
    
    protected Screen scr;
    protected BMFont font;
    
    protected String text = "";
    protected String backup;
    protected boolean onlyDigits;
    
    public int x, y, w;
    public boolean selected;
    
    public TextBox(Screen scr, BMFont font) {
        this.scr = scr;
        this.font = font;
    }
    
    public TextBox(Screen scr, BMFont font, String text) {
        this.scr = scr;
        this.font = font;
        this.text = text;
    }
    
    public TextBox setText(String text) {
        this.text = text;
        return this;
    }
    
    public TextBox setXYW(int x, int y, int w) {
        this.x = x;
        this.y = y;
        this.w = w;
        return this;
    }
    
    public void onlyDigits(boolean set) {
        this.onlyDigits = set;
    }
    
    public void draw(E3D e3d, boolean focused, int focusedColor) {
        e3d.drawRect(null, x, y, w, font.getHeight(), 0, 0.5f);
        int color = focused && !selected ? focusedColor : 0xffffff;
        
        if(selected || focused) {
            e3d.drawRect(null, x, y, w, 1, color, 0.5f);
            e3d.drawRect(null, x, y+font.getHeight()-1, w, 1, color, 0.5f);
            
            e3d.drawRect(null, x, y+1, 1, font.getHeight()-2, color, 0.5f);
            e3d.drawRect(null, x+w-1, y+1, 1, font.getHeight()-2, color, 0.5f);
        }
        
        e3d.pushClip();
        e3d.clip(x, y, w, font.getHeight());
        
        int xx = Math.min(0, w - font.stringWidth(text));
        font.drawString(text, x + xx, y, 1, color);
        
        e3d.popClip();
    }
    
    public boolean isInBox(int mx, int my) {
        return mx>=x && my>=y && mx<x+w && my<y+font.getHeight();
    }
    
    public void open() {
        backup = text;
        selected = true;
    }
    
    public void onCancel() {
        scr.closeTextBox();
        text = backup;
    }
    
    public void onEnter() {
        scr.closeTextBox();
    }
    
    public void onMouseUnfocus() {
        scr.closeTextBox();
    }
    
    public void addChars(char[] chrs) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(text);
        
        if(onlyDigits) {
            for(char ch : chrs) {
                if(Character.isDigit(ch)) buffer.append(ch);
            }
        } else {
            buffer.append(chrs);
        }
        
        text = buffer.toString();
    }
    
    public void erase()  {
        if(text.length() > 0) text = text.substring(0, text.length() - 1);
    }
    
    public int toInteger() {
        try {
            return Integer.valueOf(text);
        } catch(NumberFormatException e) {
            return 0;
        }
    }

}
