package code.ui.itemList;

import code.engine3d.E3D;
import code.engine3d.HudRender;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class TabsItem extends ListItem {
    
    protected BMFont font;
    protected String[] tabs;
    
    protected int[] tabW, tabX;
    protected int width, totalWidth;
    
    protected int xScroll;
    protected int currentTab = -1, selectedTab = -1;
    
    public TabsItem(String[] tabs, BMFont font) {
        this.tabs = tabs;
        this.font = font;
        
        tabW = new int[tabs.length];
        tabX = new int[tabs.length];
    }
    
    public TabsItem setItems(String tabs[], ItemList list) {
        this.tabs = tabs;
        if(tabW.length != tabs.length) tabW = new int[tabs.length];
        if(tabX.length != tabs.length) tabX = new int[tabs.length];
        
        list.updateList();
        return this;
    }
    
    public void updateHeight(int w) {
        width = w;
        totalWidth = 0;
        height = font.getHeight();
        
        for(int i=0; i<tabs.length; i++) {
            int itemWidth = font.stringWidth(tabs[i]);
            
            tabX[i] = totalWidth;
            tabW[i] = itemWidth;
            totalWidth += itemWidth;
        }
        
        if(totalWidth < w) {
            int distancing = (w - totalWidth) / tabs.length;
            
            for(int i=0; i<tabs.length; i++) {
                tabX[i] += distancing*i;
                tabW[i] = (i==tabs.length-1) ? (w-tabX[i]) : (tabW[i]+distancing);
            }
            
            totalWidth = w;
        }
        
        limitScroll();
    }
    
    public void draw(HudRender hudRender, int windowX, int windowY, int windowW, int windowH, 
            int yScroll, boolean selected, int color, int selColor) {
        
        hudRender.drawRect(windowX, windowY+yScroll+y, windowW, height, 0, 0.5f);
        
        if(selectedTab != -1 && selected) {
            int x = windowX + tabX[selectedTab] + xScroll;
            int w = tabW[selectedTab];
            int y = windowY + this.y + yScroll; 
            
            hudRender.drawRect(x, y, w, 1, selColor, 0.5f);
            hudRender.drawRect(x, y+height-1, w, 1, selColor, 0.5f);
            
            hudRender.drawRect(x, y+1, 1, height-2, selColor, 0.5f);
            hudRender.drawRect(x+w-1, y+1, 1, height-2, selColor, 0.5f);
        }
        
        for(int i=0; i<tabs.length; i++) {
            int tabX = this.tabX[i];
            if(tabX + xScroll > windowH) break;
            
            int tabW = this.tabW[i];
            if(tabX + tabW + xScroll <= 0) continue;
            
            String tab = tabs[i];
            
            int offsetX = (tabW - font.stringWidth(tab)) / 2;
            boolean tabSelected = (selected&&selectedTab==i);
            boolean tabIsCurrent = currentTab == i;
            
            font.drawString(hudRender, tab, 
                    windowX + tabX + xScroll + offsetX, 
                    windowY + y + yScroll, 
                    1, tabSelected?selColor:color, (tabIsCurrent||tabSelected)?1:0.5f);
        }
    }
    
    protected void limitScroll() {
        if(totalWidth <= width) {
            xScroll = 0;
        } else {
            //Левый край в начале окна
            if(xScroll > 0) xScroll = 0;
            
            //Правый край в конце окна
            if(xScroll + totalWidth < width) xScroll = width - totalWidth;
        }
    }
    
    public TabsItem selectTab(int index) {
        selectedTab = index;
        
        xScroll = width - tabX[index] - tabW[index]/2;
        limitScroll();
        return this;
    }
    
    public TabsItem setCurrentTab(int index) {
        currentTab = index;
        return selectTab(index);
    }
    
    public void mouseUpdate(int x, int y, int mx, int my) {
        selectedTab = -1;
        
        for(int i=0; i<tabs.length; i++) {
            int tabX = this.tabX[i];
            int tabW = this.tabW[i];
            
            if(mx >= x+tabX+xScroll && mx < x+tabX+tabW+xScroll) {
                selectedTab = i;
                break;
            }
        }
    }
    
    public boolean onMouseScroll(int y) {
        xScroll += y;
        limitScroll();
        
        return totalWidth > width;
    }
    
    public boolean onLeft() {
        if(selectedTab == -1) selectedTab = tabs.length / 2;
        else if(selectedTab == 0) selectedTab = tabs.length-1;
        else selectedTab--;
        
        selectTab(selectedTab);
        return true;
    }
    
    public boolean onRight() {
        if(selectedTab == -1) selectedTab = tabs.length / 2;
        else if(selectedTab == tabs.length-1) selectedTab = 0;
        else selectedTab++;
        
        selectTab(selectedTab);
        return true;
    }
    
    public void onEnter() {
        if(selectedTab != -1) currentTab = selectedTab;
    }

}
