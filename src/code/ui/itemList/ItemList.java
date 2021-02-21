package code.ui.itemList;

import code.engine3d.E3D;
import code.utils.Keys;
import code.utils.font.BMFont;
import java.util.Vector;

public class ItemList {
    private int prevMX = Integer.MIN_VALUE, prevMY = Integer.MIN_VALUE;
    private boolean updateMouse;
    private int itemPressed = -1;
    
    private int w, h, fullHeight;
    private int yScroll = 0;
    private int index = -1, topIndex;
    
    private Vector<ListItem> items;
    private boolean vCenter = true;

    public ItemList(int w, int h, BMFont font) {
        this.w = w;
        this.h = h;
        
        items = new Vector();
    }
    
    public static ItemList createItemList(int w, int h, BMFont font, final code.audio.SoundSource selectSound) {
        
        return new ItemList(w, h, font) {
            public void itemSelected() {
                super.itemSelected();
                selectSound.play();
            }
        };
    }
    
    public void setSize(int w, int h) {
        this.w = w;
        this.h = h;
        updateList();
        limitYScroll();
    }
    
    public void removeAll() {
        items.removeAllElements();
        itemPressed = -1;
        fullHeight = 0;
        topIndex = 0;
    }
    
    public void addVoid(BMFont font) {
        ListItem item = new TextItem("", font);
        item.skip = true;
        add(item);
    }
    
    public void add(ListItem item) {
        items.addElement(item);
        item.y = fullHeight;
        item.updateHeight(w);
        fullHeight += item.height;
        
        if(vCenter && fullHeight < h) yScroll = (h - fullHeight) >> 1;
    }

    public void updateList() {
        int y = 0;
        fullHeight = 0;
        
        for(int i=0; i<items.size(); i++) {
            ListItem item = items.elementAt(i);
            
            item.y = y;
            item.updateHeight(w);
            
            y += item.height;
            fullHeight += item.height;
        }
        
        centralize();
    }
    
    public void draw(E3D e3d, int x, int y, 
            int color, int selColor) {
        e3d.pushClip();
        e3d.clip(x, y, w, h);
        
        ListItem topItem = items.elementAt(topIndex);
        
        int searchStep = 0;
        if(topItem.y+topItem.height+yScroll < 0) searchStep = 1;
        else if(topItem.y+yScroll > 0) searchStep = -1;
        
        if(searchStep != 0) {
            while(topIndex >= 0 && topIndex < items.size()) {
                ListItem item = items.elementAt(topIndex);
            
                if(yScroll + item.y <= 0 && 
                        yScroll + item.y + item.height > 0) {
                    break;
                }
                topIndex += searchStep;
            }
        }
        topIndex = Math.max(0, Math.min(items.size()-1, topIndex));
        
        for(int i=topIndex; i < items.size(); i++) {
            ListItem item = items.elementAt(i);
            
            if(y+item.y+yScroll >= y+h) break;
            
            item.draw(e3d, x, y, w, h, yScroll, index == i, color, selColor);
        }

        e3d.popClip();
    }
    
    public boolean isInBox(int x, int y, int mx, int my) {
        if(mx < x || mx >= x+w) return false;
        
        if(h > fullHeight) return my>=y+(h-fullHeight)/2 && my<y+h-(h-fullHeight)/2;
        return my>=y && my<y+h;
    }
    
    public void mouseUpdate(int x, int y, int mouseX, int mouseY) {
        if(prevMX != mouseX || prevMY != mouseY) {
            prevMX = mouseX;
            prevMY = mouseY;
            updateMouse = true;
        }
        if(!updateMouse) return;
        
        if(!isInBox(x, y, mouseX, mouseY)) {
            index = -1;
            return;
        }
        
        int listY = y + yScroll;
        int newId = -1;
        for(int i=topIndex; i<items.size(); i++) {
            ListItem item = items.elementAt(i);
            if(item.skip) continue;
            
            if(mouseY >= listY+item.y && mouseY < listY+item.y+item.height) {
                newId = i;
                break;
            } 
        }
        
        if(newId != index) {
            index = newId;
            if(index != -1) itemSelected();
        }
        
        if(index != -1) items.elementAt(index).mouseUpdate(x, y+yScroll, mouseX, mouseY);
    }

    public void mouseScroll(int addY) {
        if(index != -1) {
            if(items.elementAt(index).onMouseScroll(addY)) return;
        }
        
        yScroll += addY;
        limitYScroll();
    }
    
    public void itemSelected() {
        if(index != -1) items.elementAt(index).onSelected();
    }
    
    public boolean mouseAction(int x, int y, int mx, int my, boolean pressed) {
        updateMouse = true;
        
        if(pressed){
            if(!isInBox(x, y, mx, my)) return false;
            itemPressed = index;
        }
        
        if(!pressed && index == itemPressed && index != -1) {
            items.elementAt(index).onClick(x, y+yScroll, mx, my);
        }
        return true;
    }
    
    public boolean keyPressed(int key) {
        if(index != -1 && Keys.isThatBinding(key, Keys.OK)) {
            items.elementAt(index).onEnter();
            return true;
        }
        
        return keyRepeated(key);
    }
    
    public boolean keyRepeated(int key) {
        ListItem item = getCurrentItem();
        
        if(Keys.isThatBinding(key, Keys.LEFT) && item != null) {
            if(item.onLeft()) return true;
        } else if(Keys.isThatBinding(key, Keys.RIGHT) && item != null) {
            if(item.onRight()) return true;
        } else if(Keys.isThatBinding(key, Keys.UP)) {
            scrollIndex(-1);
            updateMouse = false;
            return true;
        } else if(Keys.isThatBinding(key, Keys.DOWN)) {
            scrollIndex(1);
            updateMouse = false;
            return true;
        }
        
        return false;
    }
    
    private void scrollIndex(int i) {
        if(index == -1) index = items.size()/2;
        else index += i;
        
        if(index < 0) index = items.size() - 1;
        else index %= items.size();
        
        if(items.elementAt(index).skip) {
            scrollIndex(i);
            return;
        }
        
        scrollYToCurrentIndex();
        itemSelected();
    }

    private void scrollYToCurrentIndex() {
        ListItem item = getCurrentItem();
        yScroll = h/2 - item.y - item.height/2;
        limitYScroll();
    }
    
    public void limitYScroll() {
        if(fullHeight > h) {
            //Начало текста в начале окна
            if(yScroll > 0) yScroll = 0;
            
            //Нижний край в конце окна
            if(yScroll + fullHeight < h) yScroll = h - fullHeight;
        } else {
            //Текст по середине
            centralize();
        }
    }
    
    protected void centralize() {
        if(vCenter && fullHeight < h) yScroll = (h - fullHeight) >> 1;
        else yScroll = 0;
    }

    public void setYScroll(int y) {
        yScroll = y;
    }

    public int getYScroll() {
        return yScroll;
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
        if(index != -1) itemSelected();
    }

    public void setIndexLimited(int i) {
        setIndex(i);
        scrollYToCurrentIndex();
    }
    
    public final int getItemsCount() {
        return items.size();
    }
    
    public final ListItem getCurrentItem() {
        return index == -1 ? null : items.elementAt(index);
    }

    public void setVCenter(boolean vCenter) {
        this.vCenter = vCenter;
    }

    public boolean getVCenter() {
        return vCenter;
    }
    
    public int getWidth() {
        return w;
    }
    
    public int getHeight() {
        return h;
    }
    
    public int getFullHeight() {
        return fullHeight;
    }
}
