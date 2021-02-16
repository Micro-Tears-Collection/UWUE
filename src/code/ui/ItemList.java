package code.ui;

import code.engine3d.E3D;
import code.utils.font.BMFont;

public class ItemList extends TextView {
    
    private int prevMX = Integer.MIN_VALUE, prevMY = Integer.MIN_VALUE;
    private boolean skipMid = true;
    private boolean wasInBox = false;
    
    private String[] items;
    private boolean[] itemHCenter;
    
    private int index = -1;
    private int[] itemsToLine, itemsLinesCount;
    private int[] linesToItem;

    public ItemList(int w, int h, BMFont font) {
        super(w, h, font);
        hCenter = true;
        vCenter = true;
    }
    
    public static ItemList createItemList(int w, int h, BMFont font, final code.audio.SoundSource selectSound) {
        
        return new ItemList(w, h, font) {
            public void itemSelected() {
                selectSound.play();
            }
        };
    }

    public void setItems(String[] items) {
        this.items = items;
        itemsToLine = new int[items.length];
        itemsLinesCount = new int[items.length];
        
        removeText();
        for(int i=0; i<items.length; i++) {
            String item = items[i];
            
            int itemLine = lines.size();
            if(item == null) item = "";
            addText(item, '\0');
            
            itemsToLine[i] = itemLine;
            itemsLinesCount[i] = lines.size() - itemLine;
        }
        
        linesToItem = new int[lines.size()];
        for(int i=0; i<items.length; i++) {
            int start = itemsToLine[i];
            int end = start + itemsLinesCount[i];
            
            for(int x=start; x<end; x++) {
                linesToItem[x] = i;
            }
        }
        
        centralize();
    }
    
    public void setItems(String[] items, boolean[] ms) {
        setItems(items);
        this.itemHCenter = ms;
    }
    
    public void setItem(String item, int index) {
        lines.setElementAt(item, itemsToLine[index]);
    }
    
    public void draw(E3D e3d, int x, int y, 
            int color, int selColor, boolean drawBck) {
        if(drawBck) e3d.drawWindow(x, y, w, h, font);
        
        e3d.pushClip();
        e3d.clip(x, y, w, h);

        final int stepY = font.getHeight();
        int i = Math.max(0, -yScroll / stepY);
        int posY = yScroll + i*stepY;
        
        for(; i < lines.size() && posY <= h; i++) {
            String str = (String) lines.elementAt(i);
            
            boolean inMiddle = (itemHCenter != null && itemHCenter[linesToItem[i]] == true);
            int offsetX = (hCenter || inMiddle) ? (w - font.stringWidth(str)) >> 1 : 0;

            font.drawString(str, x + offsetX, y + posY, 1, index==linesToItem[i]?selColor:color);
            
            posY += stepY;
        }

        e3d.popClip();
    }
    
    public void mouseUpdate(int x, int y, int mouseX, int mouseY) {
        if(prevMX == mouseX && prevMY == mouseY) return;
        prevMX = mouseX; prevMY = mouseY;
        
        if(!isInBox(x, y, mouseX, mouseY)) {
            if(wasInBox) {
                wasInBox = false;
                index = -1;
            }
            return;
        }
        wasInBox = true;
        
        int listY = y + yScroll;
        int newId = (mouseY - listY) / font.getHeight();
        newId = (newId >= 0 && newId < lines.size()) ? linesToItem[newId] : -1;
        
        if(newId != -1 && (!skipMid || itemHCenter == null || !itemHCenter[newId])) {
            if(newId != index) {
                index = newId;
                itemSelected();
            }
        } else index = -1;
    }
    
    public void down() {scrollIndex(1);}
    public void up() {scrollIndex(-1);}
    
    private void scrollIndex(int i) {
        int stepY = font.getHeight();
        
        if(index == -1) index = linesToItem[Math.min(lines.size()-1, Math.max(0, (h/2-yScroll) / stepY))];
        else index += i;
        
        if(index < 0) index = items.length - 1;
        else index %= items.length;
        
        if(skipMid && itemHCenter != null && itemHCenter[index]) {
            scrollIndex(i);
            return;
        }
        
        scrollYToCurrentIndex();
        itemSelected();
    }

    private void scrollYToCurrentIndex() {
        int stepY = font.getHeight();
        
        yScroll = h/2 - itemsToLine[index]*stepY - itemsLinesCount[index]*stepY/2;
        limitY();
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
        itemSelected();
    }

    public void setIndexLimited(int i) {
        index = i;
        scrollYToCurrentIndex();
        itemSelected();
    }

    public boolean getSkipMiddle() {
        return skipMid;
    }

    public void setSkipMiddle(boolean bol) {
        skipMid = bol;
    }
    
    public final int getItemsCount() {
        return items.length;
    }
    
    public final String getCurrentItem() {
        return items[index];
    }
    
    public void itemSelected() {}
}
