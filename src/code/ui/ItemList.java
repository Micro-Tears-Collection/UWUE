package code.ui;

import code.engine3d.E3D;
import code.utils.font.BMFont;

public class ItemList {
    
    private BMFont font;
    private int w, h;
    private String[] items;
    private boolean[] midSel;
    private boolean center = true, skipMid = true;
    boolean wasInBox = false;
    
    private int index = -1, yOffset;

    public ItemList(String[] items, int w, int h, BMFont font) {
        this.items = items;
        this.w = w; this.h = h;
        this.font = font;
        yOffset = (h - getHeight()) >> 1;
    }
    
    public ItemList(String[] items, int w, int h, BMFont font, boolean[] ms) {
        this.items = items;
        this.w = w; this.h = h;
        this.font = font;
        this.midSel = ms;
        yOffset = (h - getHeight()) >> 1;
    }
    
    public void draw(E3D e3d, int x, int y, 
            int color, int selColor, boolean drawBck) {
        if(drawBck) e3d.drawWindow(x, y, w, h, font);
        
        e3d.pushClip();
        e3d.clip(x, y, w, h);
        
        final int stepY = font.getHeight();
        int i = Math.max(0, -yOffset / stepY);
        int posY = yOffset + i*stepY;
        
        for(; i < items.length; i++) {
            if(posY > h) break;
            String str = items[i];
            
            boolean inMiddle = (midSel != null && midSel[i] == true);
            int offsetX = (center || inMiddle) ? (w - font.stringWidth(str)) >> 1 : 0;

            font.drawString(str, x + offsetX, y + posY, 1, index==i?selColor:color);
            
            posY += stepY;
        }

        e3d.popClip();
    }
    
    public boolean isInBox(int x, int y, int mx, int my) {
        if(h > getHeight()) return mx>=x && my>=y+(h-getHeight())/2 && mx<x+w && my<y+h-(h-getHeight())/2;
        return mx>=x && my>=y && mx<x+w && my<y+h;
    }
    
    public void mouseUpdate(int x, int y, int mouseX, int mouseY) {
        if(!isInBox(x, y, mouseX, mouseY)) {
            if(wasInBox) {
                wasInBox = false;
                index = -1;
            }
            return;
        }
        wasInBox = true;
        
        int listY = y + yOffset;
        int newId = (mouseY - listY) / font.getHeight();
        
        if(newId >= 0 && newId < items.length &&
                (!skipMid || midSel == null || !midSel[newId])) {
            if(newId != index) {
                index = newId;
                itemSelected();
            }
        } else index = -1;
    }
    
    public void scrollDown() {scroll(1);}
    
    public void scrollUp() {scroll(-1);}
    
    public void scroll(int i) {
        int stepY = font.getHeight();
        
        if(index == -1) index = Math.min(items.length-1, Math.max(0, (h/2-yOffset) / stepY));
        else index += i;
        
        if(index < 0) index = items.length - 1;
        else index %= items.length;
        
        if(midSel != null && midSel[index] && skipMid) {
            scroll(i);
            return;
        }
        
        yOffset = h/2 - index*stepY - stepY/2;
        limitY();
        itemSelected();
    }
    
    private void limitY() {
        int elsHeight = getHeight();
        
        if(elsHeight > h) {
            yOffset = Math.max(h - elsHeight, Math.min(0, yOffset));
        } else yOffset = (h - elsHeight) >> 1;
    }
    
    public int getHeight() {
        return items.length*font.getHeight();
    }
    
    public void scrollY(int y) {
        yOffset += y;
        limitY();
    }

    public void setY(int y) {
        yOffset = y;
    }

    public int getY() {
        return yOffset;
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
        itemSelected();
    }

    public boolean getCenter() {
        return center;
    }

    public void setCenter(boolean cen) {
        center = cen;
    }
    
    public final String getCurrentItem() {
        return items[index];
    }
    
    public final String[] getItems() {
        return items;
    }
    
    public void itemSelected() {}
}
