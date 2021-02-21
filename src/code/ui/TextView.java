package code.ui;

import code.engine3d.E3D;
import code.utils.font.BMFont;
import java.util.Vector;

public class TextView {

    protected BMFont font;
    protected int w, h;
    
    protected final Vector<String> lines = new Vector();
    protected boolean hCenter = false, vCenter = false;
    protected int yScroll = 0;

    public TextView(int w, int h, BMFont font) {
        this.font = font;
        this.w = w;
        this.h = h;
    }
    
    public void setSize(int w, int h) {
        this.w = w;
        this.h = h;
        limitY();
    }

    public static void createLines(String txt, Vector<String> lines, char lineDivider, BMFont font, int w) {
        int lineWidth = 0;
        int wordStart = 0;
        int lastSpace = -1;
        
        int prevCP = 0;
        for (int i=0; i<txt.length();) {
            int cp = Character.codePointAt(txt, i);
            int charWidth = font.cpWidth(cp, prevCP);
            prevCP = cp;
            if(cp == ' ') lastSpace = i;

            int wordEnd = -1;
            if(cp == lineDivider) { //символ переноса строки
                wordEnd = i;
                i++; //пропускаем символ переноса
            } else if(lineWidth + charWidth > w) { //следующий символ не помещается
                if(lastSpace != -1) { //обрезаем по последнему пробелу
                    i = lastSpace + 1; //+1 - пропускаем последний пробел
                    wordEnd = lastSpace;
                } else wordEnd = i;
            }

            if(wordEnd != -1) {
                if(wordEnd < wordStart) {
                    //Мы не можем поместить ни одну букву... Поэтому помещаем одну
                    wordEnd = wordStart+1;
                    i = wordEnd+1;
                }
                String line = txt.substring(wordStart, wordEnd);
                lines.addElement(line);

                lineWidth = 0;
                wordStart = i;
            } else {
                lineWidth += charWidth;
                i++;
            }
        }

        if(wordStart < txt.length()) {
            lines.addElement(txt.substring(wordStart, txt.length()));
        }

        if(txt.isEmpty()) lines.addElement("");
    }
    
    public void addText(String str) {
        addText(str, '*');
    }

    public void addText(String str, char lineDivider) {
        createLines(str, lines, lineDivider, font, w);
    }
    
    public void setText(String str) {
        setText(str, '*');
    }

    public void setText(String str, char lineDivider) {
        lines.removeAllElements();
        addText(str, lineDivider);
        centralize();
    }
    
    public void draw(E3D e3d, int x, int y, int color) {
        e3d.pushClip();
        e3d.clip(x, y, w, h);
        
        TextView.draw(e3d, lines, font, x, y, w, h, yScroll, hCenter, color);

        e3d.popClip();
    }

    public static void draw(E3D e3d, Vector<String> lines, BMFont font, 
            int x, int y, int w, int h, int yScroll, boolean hCenter, int color) {

        final int stepY = font.getHeight();
        int i = Math.max(0, -yScroll / stepY);
        int posY = yScroll + i*stepY;
        
        for(; i < lines.size() && posY <= h; i++) {
            String str = lines.elementAt(i);
            
            int offsetX = hCenter ? (w - font.stringWidth(str)) >> 1 : 0;

            font.drawString(str, x + offsetX, y + posY, 1, color);
            
            posY += stepY;
        }
    }
    
    protected void centralize() {
        if(vCenter && getTextHeight() < h) yScroll = (h - getTextHeight()) >> 1;
        else yScroll = 0;
    }
    
    protected void limitY() {
        final int textHeight = getTextHeight();
        
        if(textHeight > h) {
            //Начало текста в начале окна
            if(yScroll > 0) yScroll = 0;
            
            //Нижний край в конце окна
            if(yScroll + textHeight < h) yScroll = h - textHeight;
        } else {
            centralize();
        }
    }

    public void scroll(int addY) {
        yScroll += addY;
        limitY();
    }

    public void setYScroll(int y) {
        yScroll = y;
    }

    public int getYScroll() {
        return yScroll;
    }

    public boolean getHCenter() {
        return hCenter;
    }

    public void setHCenter(boolean cen) {
        hCenter = cen;
    }

    public boolean getVCenter() {
        return vCenter;
    }

    public void setVCenter(boolean cen) {
        vCenter = cen;
    }

    public int getTextHeight() {
        return font.getHeight() * lines.size();
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

}
