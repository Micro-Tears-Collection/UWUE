package code.ui;

import code.engine3d.E3D;
import code.game.Main;
import code.utils.font.BMFont;


/**
 *
 * @author Roman Lahin
 */
public class TextBox {
    
    Main main;
    BMFont font;
    public boolean focused;
    public String text = "";
    
    public int x, y, w;
    
    public TextBox(Main main, BMFont font) {
        this.main = main;
        this.font = font;
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
    
    public void draw(E3D e3d) {
        e3d.drawRect(null, x, y, w, font.getHeight(), 0, 0.5f);
        if(focused) {
            e3d.drawRect(null, x, y, w, 1, 0xffffff, 0.5f);
            e3d.drawRect(null, x, y+font.getHeight()-1, w, 1, 0xffffff, 0.5f);
            
            e3d.drawRect(null, x, y+1, 1, font.getHeight()-2, 0xffffff, 0.5f);
            e3d.drawRect(null, x+w-1, y+1, 1, font.getHeight()-2, 0xffffff, 0.5f);
        }
        
        e3d.pushClip();
        e3d.clip(x, y, w, font.getHeight());
        
        int xx = Math.min(0, w - font.stringWidth(text));
        font.drawString(text, x + xx, y, 1, 0xffffff);
        
        e3d.popClip();
    }
    
    public boolean isInBox(int mx, int my) {
        return mx>=x && my>=y && mx<x+w && my<y+font.getHeight();
    }
    
    public void cancel() {
        main.closeTextBox();
    }
    
    public void enter() {
        main.closeTextBox();
    }
    
    public void addChars(char[] chrs) {
        text += String.valueOf(chrs, 0, chrs.length);
    }

}
