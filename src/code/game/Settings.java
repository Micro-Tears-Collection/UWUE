package code.game;

import code.engine.Engine;
import code.engine.Screen;

import code.ui.DigitBox;
import code.ui.ItemList;
import code.ui.TextBox;

import code.utils.Keys;

import java.io.File;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class Settings extends Screen {
    
    private Main main;
    private Menu menu;
    
    private Configuration mconf;
    
    private int currentList;
    private ItemList list, applyConfirm, resetConfirm, msg;
    private Vector<TextBox> boxes = new Vector();
    
    private long applyBegin;
    private int maxAA;
    
    public Settings(Main main, Menu menu) {
        this.main = main;
        this.menu = menu;
        
        for(int i=0; i<4; i++) {
            TextBox box = new DigitBox(main, main.font);
            boxes.add(box);
        }
        
        mconf = new Configuration(main.conf);

        boxes.get(0).setText(String.valueOf(mconf.fw));
        boxes.get(1).setText(String.valueOf(mconf.fh));
        boxes.get(2).setText(String.valueOf(mconf.ww));
        boxes.get(3).setText(String.valueOf(mconf.wh));
        
        createList();
        setList();
        
        maxAA = Engine.getMaxAA();
    }
    
    public void destroy() {
        menu.destroy();
    }
    
    private void createList() {
        if(list == null) {
            list = ItemList.createItemList(getWidth()/2, getHeight(), main.font, main.selectedS);
        
            list.setHCenter(false);
            list.setSkipMiddle(true);
        } else list.setSize(getWidth()/2, getHeight());
        
        int bw = main.font.stringWidth("9999");
        int xw = main.font.stringWidth("x");
        
        for(int i=0; i<4; i++) {
            TextBox box = boxes.elementAt(i);
            
            int len = i <= 1 ? 
                    main.font.stringWidth("Fullscreen size: ") : 
                    main.font.stringWidth("Windowed size: ");
            
            int xx = getWidth() / 4 + len + (i & 1) * (xw + bw);
            int yy = (3 + i / 2) * main.font.getHeight() + list.getYScroll();
            box.setXYW(xx, yy, bw);
        }
        
        if(applyConfirm == null) {
            applyConfirm = ItemList.createItemList(getWidth(), getHeight(), main.font, main.selectedS);
            applyConfirm.setSkipMiddle(true);
        } else applyConfirm.setSize(getWidth(), getHeight());
        
        applyConfirm.setItems(new String[] {
            "Save current settings?", "Antialiasing will be applied after restart", null, "Yes", "No"},
                new boolean[]{true, true, true, false, false});
        
        if(resetConfirm == null) {
            resetConfirm = ItemList.createItemList(getWidth(), getHeight(), main.font, main.selectedS);
            resetConfirm.setSkipMiddle(true);
        } else resetConfirm.setSize(getWidth(), getHeight());
        
        resetConfirm.setItems(new String[] {"Remove progress?", "Yes", "No"},
                new boolean[]{true, false, false});
        
        if(msg == null) {
            msg = ItemList.createItemList(getWidth(), getHeight(), main.font, main.selectedS);
            msg.setSkipMiddle(true);
        } else msg.setSize(getWidth(), getHeight());
        
        msg.setItems(new String[] {null, "Ok"}, new boolean[]{true, false});
    }
    
    private String checkBox(boolean bol) {
        return bol? "[o]" : "[ ]";
    }
    
    private void setList() {
        String[] items = new String[] {
            "Screen settings:",
            "Launch game in fullscreen: "+checkBox(mconf.startInFullscr),
            "(Press F11 to toggle)",
            "Fullscreen size: ",
            "Windowed size: ",
            "Antialiasing: "+mconf.aa+"x",
            "Vsync: "+checkBox(mconf.vsync),
            "Game settings:",
            "Remove progress",
            "",
            "Apply&save",
            "Cancel"
        };
        
        boolean[] ms = new boolean[items.length];
        ms[0] = true;
        ms[2] = true;
        ms[7] = true;
        ms[9] = true;
        
        list.setItems(items, ms);
    }
    
    private void action() {
        if(currentList == 0) {
            int index = list.getIndex();

            main.closeTextBox();

            for(int i = 0; i < boxes.size(); i++) {
                TextBox box = boxes.elementAt(i);

                box.focused = box.isInBox(getMouseX(), getMouseY() - list.getYScroll());

                if(box.focused) {
                    index = -1;
                    main.openTextBox(box);
                }
            }

            if(index != -1) {
                main.clickedS.play();
                if(index == 1) mconf.startInFullscr ^= true;
                else if(index == 5) {
                    mconf.aa <<= 1;
                    if(mconf.aa > maxAA) mconf.aa = 1;
                } else if(index == 6) { 
                    mconf.vsync ^= true;
                } else if(index == 8) {
                    currentList = 2;
                } else if(index == 10) {
                    mconf.fw = Integer.valueOf(boxes.elementAt(0).text);
                    mconf.fh = Integer.valueOf(boxes.elementAt(1).text);
                    mconf.ww = Integer.valueOf(boxes.elementAt(2).text);
                    mconf.wh = Integer.valueOf(boxes.elementAt(3).text);
                
                    if(!mconf.isValid()) {
                        currentList = 3;
                        msg.setItem("Unsupported videomode :(", 0);
                    } else {
                        mconf.apply();
                        if(mconf.isNeedToConfirm(main.conf)) {
                            applyBegin = System.currentTimeMillis();
                            currentList = 1;
                        } else {
                            saveSettings();
                            exit();
                        }
                    }
                    
                } else if(index == 11) {
                    exit();
                }

                setList();
            }
        } else if(currentList == 1) {
            int index = applyConfirm.getIndex();
            
            if(index == 3) {
                main.clickedS.play();
                saveSettings();
                exit();
            } else if(index == 4) {
                main.clickedS.play();
                main.conf.apply();
                currentList = 0;
            }
            
        } else if(currentList == 2) {
            int index = resetConfirm.getIndex();
            
            if(index != -1) {
                main.clickedS.play();
                currentList = 0;
                
                if(index == 1) {
                    try {
                        File file = new File("saves/");
                        if(file.exists() && file.isDirectory()) {
                            file = new File("saves", "luasave");
                            if(file.exists()) file.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    main.luasave = null;
                    main.clearLua();
                }
            }
        } else if(currentList == 3) {
            
            if(msg.getIndex() == 1) {
                main.clickedS.play();
                currentList = 0;
            }
        }
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        createList();
        setList();
        menu.sizeChanged(w, h, this);
    }
    
    public void tick() {
        if(currentList == 1 && System.currentTimeMillis() - applyBegin >= 10000) {
            main.conf.apply();
            currentList = 0;
        }
        
        menu.drawBackground();
        main.e3d.drawRect(null, 0, 0, getWidth(), getHeight(), 0, 0.5f);

        if(currentList == 0) {
            list.mouseUpdate(getWidth() / 4, 0, getMouseX(), getMouseY());
            list.draw(main.e3d, getWidth() / 4, 0, main.fontColor, main.fontSelColor, false);

            for(int i = 0; i < boxes.size(); i++) {
                TextBox box = boxes.elementAt(i);

                box.y += list.getYScroll();
                box.draw(main.e3d);

                if((i & 1) == 0) main.font.drawString("x", box.x + box.w, box.y, 1, 0xffffff);

                box.y -= list.getYScroll();
            }
        } else {
            ItemList list;
            if(currentList == 1) {
                list = applyConfirm;
                list.setItem(
                        (10-Math.round((System.currentTimeMillis() - applyBegin) / 1000)) 
                        + " seconds before restoring settings", 2);
            } else if(currentList == 2) list = resetConfirm;
            else list = msg;
        
            list.mouseUpdate(0, 0, getMouseX(), getMouseY());
            list.draw(main.e3d, 0, 0, main.fontColor, main.fontSelColor, false);
        }
    }
    
    public void mouseAction(int key, boolean pressed) {
        if(key == Screen.MOUSE_LEFT && !pressed) {
            action();
        }
    }
    
    public void keyReleased(int key) {
        ItemList list;
        if(currentList == 0) list = this.list;
        else if(currentList == 1) list = applyConfirm;
        else if(currentList == 2) list = resetConfirm;
        else list = msg;
        
        if(Keys.isThatBinding(key, Keys.DOWN)) {
            list.down();
            Keys.reset();
        } else if(Keys.isThatBinding(key, Keys.UP)) {
            list.up();
            Keys.reset();
        }
        
        if(Keys.isThatBinding(key, Keys.ESC) && currentList == 0) {
            Keys.reset();
            main.closeTextBox();
            main.clickedS.play();
            exit();
        }
        
        if(Keys.isThatBinding(key, Keys.OK)) {
            Keys.reset();
            main.clickedS.play();
            action();
        }
    }
    
    public void mouseScroll(double xx, double yy) {
        ItemList list;
        if(currentList == 0) list = this.list;
        else if(currentList == 1) list = applyConfirm;
        else if(currentList == 2) list = resetConfirm;
        else list = msg;
        
        list.scroll((int) (yy*main.scrollSpeed()));
    }
    
    private void exit() {
        main.setScreen(menu);
    }
    
    private void saveSettings() {
        main.conf.copy(mconf);
        main.conf.save();
    } 

}
