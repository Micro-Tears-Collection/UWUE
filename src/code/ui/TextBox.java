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
    protected long openTime;
    
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
    
    public int getHeight() {
        return font.getHeight() + 4;
    }
    
    public void draw(E3D e3d, boolean focused, int focusedColor) {
        int h = getHeight();
        e3d.drawRect(null, x, y, w, h, 0, 0.5f);
        
        int color = (focused || selected) ? focusedColor : 0xffffff;
        
        if(selected || focused) {
            int lineWidth = selected?2:1;
            float alpha = selected?1:0.5f;
            
            e3d.drawRect(null, x, y, w, lineWidth, color, alpha);
            e3d.drawRect(null, x, y+h-lineWidth, w, lineWidth, color, alpha);
            
            e3d.drawRect(null, x, y+lineWidth, lineWidth, h-lineWidth*2, color, alpha);
            e3d.drawRect(null, x+w-lineWidth, y+lineWidth, lineWidth, h-lineWidth*2, color, alpha);
        }
        
        e3d.pushClip();
        e3d.clip(x, y, w, h);
        
        int textWidth = font.stringWidth(text);
        
        int fontScale = Math.max(1, (int)font.baseScale);
        int textWithStuffWidth = textWidth + (selected?fontScale*3:0);
        int textX = 2+Math.min(0, w-4 - textWithStuffWidth);
        
        font.drawString(text, x + textX, y+2, 1, color);
        
        if(selected) {
            int time = (int)(((System.currentTimeMillis() - openTime)/500)&1);
            if(time == 0) {
                e3d.drawRect(null, 
                        x + textX+textWidth+fontScale, y+2+fontScale*3, 
                        fontScale, h-4-fontScale*3, 
                        focusedColor, 1);
            }
            
        }
        
        e3d.popClip();
    }
    
    public boolean isInBox(int mx, int my) {
        return mx>=x && my>=y && mx<x+w && my<y+getHeight();
    }
    
    public void open() {
        backup = text;
        selected = true;
        openTime = System.currentTimeMillis();
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
