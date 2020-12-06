package code.game;

import code.Engine;
import code.Screen;
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
    
    Main main;
    Menu menu;
    
    Configuration mconf;
    
    int currentList;
    ItemList list, applyConfirm, resetConfirm, msg;
    Vector<TextBox> boxes = new Vector();
    
    long applyBegin;
    int maxAA;
    
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
    
    void createList() {
        list = new ItemList(getWidth()/2, getHeight(), main.font) {
                    public void itemSelected() {
                        main.selectedS.play();
                    }
                };
        
        list.setCenter(false);
        list.setSkipMiddle(true);
        
        int bw = main.font.stringWidth("9999");
        int xw = main.font.stringWidth("x");
        
        for(int i=0; i<4; i++) {
            TextBox box = boxes.elementAt(i);
            
            int len = i <= 1 ? 
                    main.font.stringWidth("Fullscreen size: ") : 
                    main.font.stringWidth("Windowed size: ");
            
            int xx = getWidth() / 4 + len + (i & 1) * (xw + bw);
            int yy = (3 + i / 2) * main.font.getHeight() + list.getY();
            box.setXYW(xx, yy, bw);
        }
        
        applyConfirm = new ItemList(new String[] {
            "Save current settings?", "Antialiasing will be applied after restart", null, "Yes", "No"},
                getWidth(), getHeight(), main.font, new boolean[]{true, true, true, false, false}) {
                    public void itemSelected() {
                        main.selectedS.play();
                    }
                };
        
        applyConfirm.setSkipMiddle(true);
        
        resetConfirm = new ItemList(new String[] {"Remove progress?", "Yes", "No"},
                getWidth(), getHeight(), main.font, new boolean[]{true, false, false}) {
                    public void itemSelected() {
                        main.selectedS.play();
                    }
                };
        
        resetConfirm.setSkipMiddle(true);
        
        msg = new ItemList(new String[] {null, "Ok"},
                getWidth(), getHeight(), main.font, new boolean[]{true, false}) {
                    public void itemSelected() {
                        main.selectedS.play();
                    }
                };
        
        msg.setSkipMiddle(true);
    }
    
    String checkBox(boolean bol) {
        return bol? "[o]" : "[ ]";
    }
    
    void setList() {
        list.setItems(new String[] {
            "Screen settings:",
            "Launch game in fullscreen: "+checkBox(mconf.startInFullscr),
            "(Press F11 to toggle)",
            "Fullscreen size: ",
            "Windowed size: ",
            "Antialiasing: "+mconf.aa+"x",
            "Game settings:",
            "Remove progress",
            "",
            "Apply&save",
            "Cancel"
        });
        
        boolean[] ms = new boolean[list.getItems().length];
        ms[0] = true;
        ms[2] = true;
        ms[6] = true;
        ms[8] = true;
        list.setMS(ms);
    }
    
    public void action() {
        if(currentList == 0) {
            int index = list.getIndex();

            main.closeTextBox();

            for(int i = 0; i < boxes.size(); i++) {
                TextBox box = boxes.elementAt(i);

                box.focused = box.isInBox(getMouseX(), getMouseY() - list.getY());

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
                } else if(index == 7) {
                    currentList = 2;
                } else if(index == 9) {
                    mconf.fw = Integer.valueOf(boxes.elementAt(0).text);
                    mconf.fh = Integer.valueOf(boxes.elementAt(1).text);
                    mconf.ww = Integer.valueOf(boxes.elementAt(2).text);
                    mconf.wh = Integer.valueOf(boxes.elementAt(3).text);
                
                    if(!mconf.isValid()) {
                        currentList = 3;
                        msg.getItems()[0] = "Unsupported videomode :(";
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
                    
                } else if(index == 10) {
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
                        Engine.printError(e);
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

                box.y += list.getY();
                box.draw(main.e3d);

                if((i & 1) == 0) main.font.drawString("x", box.x + box.w, box.y, 1, 0xffffff);

                box.y -= list.getY();
            }
        } else {
            ItemList list;
            if(currentList == 1) {
                list = applyConfirm;
                list.getItems()[2] = (10-Math.round((System.currentTimeMillis() - applyBegin) / 1000)) + " seconds before restoring settings";
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
            list.scrollDown();
            Keys.reset();
        } else if(Keys.isThatBinding(key, Keys.UP)) {
            list.scrollUp();
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
