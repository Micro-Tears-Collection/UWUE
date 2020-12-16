package code.ui;

import code.engine3d.E3D;
import code.utils.font.BMFont;
import java.util.Vector;

public class TextView {

    private final Vector lines = new Vector();
    private BMFont font;
    private int w, h;
    private boolean center = false, vCenter;
    private int yOffset = 0;

    public TextView(String str, int w, int h, BMFont font) {
        this.font = font;
        this.w = w;
        this.h = h;
        if(str != null) setString(str);
    }

    private void createLines(String txt, Vector lines, char lineDivider) {
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
            } else if(lineWidth + charWidth > w) { //следующий символ не умещается
                if(lastSpace != -1) { //обрезаем по последнему пробелу
                    i = lastSpace + 1; //+1 - пропускаем последний пробел
                    wordEnd = lastSpace;
                } else wordEnd = i;
            }

            if(wordEnd != -1) {
                if(wordEnd < wordStart) {
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

    }
    
    public void addString(String str) {
        addString(str, '*');
    }

    public void addString(String str, char lineDivider) {
        createLines(str, lines, lineDivider);
        int textHeight = getTextHeight();
        yOffset = (vCenter||h>textHeight)?(h-textHeight)/2:0;
    }
    
    public void setString(String str) {
        setString(str, '*');
    }

    public void setString(String str, char lineDivider) {
        lines.removeAllElements();
        createLines(str, lines, lineDivider);
        int textHeight = getTextHeight();
        yOffset = (vCenter||h>textHeight)?(h-textHeight)/2:0;
    }
    
    public void removeText() {
        lines.removeAllElements();
    }

    public void paint(E3D e3d, int x, int y, int color) {
        e3d.pushClip();
        e3d.clip(x, y, w, h);

        final int stepY = font.getHeight();
        int i = Math.max(0, -yOffset / stepY);
        int posY = yOffset + i*stepY;
        
        for(; i < lines.size(); i++) {
            if(posY > h) break;
            String str = (String) lines.elementAt(i);
            
            int offsetX = center ? (w - font.stringWidth(str)) >> 1 : 0;

            font.drawString(str, x + offsetX, y + posY, 1, color);
            
            posY += stepY;
        }

        e3d.popClip();
    }
    
    public boolean isInBox(int x, int y, int mx, int my) {
        return mx>=x && my>=y && mx<x+w && my<y+h;
    }

    public void scroll(int dy) {
        yOffset += dy;

        final int textHeight = getTextHeight();
        if(textHeight > h) {
            //Начало текста в начале окна
            if(yOffset > 0) yOffset = 0;
            
            //Нижний край в конце окна
            if(yOffset < h - textHeight) {
                yOffset = h - textHeight;
            }
        } else {
            yOffset = (vCenter||h>textHeight)?(h-textHeight)/2:0;
        }
    }

    public int getCountString() {
        return lines.size();
    }

    public int getTextHeight() {
        return font.getHeight() * lines.size();
    }

    public void setY(int y) {
        yOffset = y;
    }

    public int getY() {
        return yOffset;
    }

    public void setYLimited(int y) {
        scroll(y - yOffset);
    }

    public boolean getCenter() {
        return center;
    }

    public void setCenter(boolean cen) {
        center = cen;
    }

    public boolean getVCenter() {
        return vCenter;
    }

    public void setVCenter(boolean cen) {
        vCenter = cen;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

}
