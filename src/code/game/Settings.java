package code.game;

import code.engine.Screen;

import code.ui.itemList.ItemList;
import code.ui.itemList.TabsItem;
import code.ui.itemList.TextBoxItem;
import code.ui.itemList.TextItem;

import code.utils.Keys;
import code.utils.font.BMFont;

import java.io.File;

/**
 *
 * @author Roman Lahin
 */
public class Settings extends Screen {
    
    private static final int AUDIO = 0, VIDEO = 1, GAMEPLAY = 2;
    
    private final Main main;
    private final Screen previous;
    private int w, h;
    
    private final Configuration mconf;
    private final int maxAA;
    
    private ItemList list, applyConfirm, resetConfirm, message;
    
    private ItemList currentList;
    private int listType;
    private long applyBegin;
    
    private TextBoxItem fullscr, windowres, vres;
    private TextItem messageText, applyConfirmText;
    private TabsItem tabs;
    
    public Settings(final Main main, Screen previous) {
        this.main = main;
        this.previous = previous;
        
        w = main.getWidth(); h = main.getHeight();
        
        mconf = new Configuration(main.conf);
        maxAA = main.e3d.maxAA;
        
        createList();
        setList(AUDIO);
    }
    
    public void destroy() {
        previous.destroy();
    }
    
    private void createList() {
        BMFont font = main.font;
        if(list == null) {
            list = ItemList.createItemList(w/2, h, font, main.selectedS);
        } else {
            list.setSize(w/2, h);
            list.updateList();
        }
        
        if(applyConfirm == null) {
            applyConfirm = ItemList.createItemList(w, h, font, main.selectedS);
        
            applyConfirm.add((new TextItem("Save current settings?", font)).setHCenter(true).setSkip(true));
            applyConfirm.add((new TextItem("Antialiasing will be applied after restart", font))
                    .setHCenter(true).setSkip(true));
            
            applyConfirmText = new TextItem("", font);
            applyConfirmText.setHCenter(true).setSkip(true);
            applyConfirm.add(applyConfirmText);
            
            applyConfirm.add(new TextItem("Yes", font) {
                public void onEnter() {
                    main.clickedS.play();
                    main.conf.copy(mconf);
                    main.conf.save();
                    main.setScreen(previous);
                }
                
            }.setHCenter(true));
            applyConfirm.add(new TextItem("No", font) {
                public void onEnter() {
                    main.clickedS.play();
                    main.conf.apply(main.window, main.e3d, true);
                    setList(VIDEO);
                }
                
            }.setHCenter(true));
        } else {
            applyConfirm.setSize(w, h);
        }
        applyConfirm.updateList();
        
        if(resetConfirm == null) {
            resetConfirm = ItemList.createItemList(w, h, font, main.selectedS);
            
            resetConfirm.add((new TextItem("Remove progress?", font)).setHCenter(true).setSkip(true));
            resetConfirm.add(new TextItem("Yes", font) {
                public void onEnter() {
                    main.clickedS.play();
                    
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
                    
                    setList(GAMEPLAY);
                }
            }.setHCenter(true));
            
            resetConfirm.add((new TextItem("No", font) {
                public void onEnter() {
                    main.clickedS.play();
                    setList(GAMEPLAY);
                }
            }).setHCenter(true));
        } else {
            resetConfirm.setSize(w, h);
        }
        resetConfirm.updateList();
        
        if(message == null) {
            message = ItemList.createItemList(w, h, font, main.selectedS);
            
            messageText = new TextItem("", font).setHCenter(true);
            message.add(messageText);
            message.add(new TextItem("Ok", font) {
                public void onEnter() {
                    setList(listType);
                }
            }.setHCenter(true));
        } else {
            message.setSize(w, h);
        }
        message.updateList();
        
        if(tabs == null) {
            tabs = new TabsItem(new String[]{"AUDIO", "VIDEO", "GAMEPLAY"}, font) {
                public void onEnter() {
                    super.onEnter();

                    if(currentTab != -1) {
                        main.clickedS.play();
                        setList(currentTab);
                    }
                }
            }.setCurrentTab(listType);
        }
    }
    
    private String checkBox(boolean bol) {
        return bol? "[o]" : "[_]";
    }
    
    private String valueEdit(int val) {
        return "<<"+val+">>";
    }
    
    private void setList(int type) {
        BMFont font = main.font;
        
        int prevIndex = list.getIndex();
        int prevYScroll = list.getYScroll();
        boolean wasList = currentList == list && type == listType;
        
        currentList = list;
        list.removeAll();
        
        list.add(tabs);
        
        if(type == AUDIO) {
            list.add(new TextItem("Music volume: " + valueEdit(mconf.musicVolume), 
                    font) {
                        public boolean onRight() {
                            main.clickedS.play();
                            mconf.musicVolume = Math.min(100, mconf.musicVolume+5);
                            mconf.applyAudio();
                            setText("Music volume: " + valueEdit(mconf.musicVolume), list);
                            return true;
                        }

                        public boolean onLeft() {
                            main.clickedS.play();
                            mconf.musicVolume = Math.max(0, mconf.musicVolume-5);
                            mconf.applyAudio();
                            setText("Music volume: " + valueEdit(mconf.musicVolume), list);
                            return true;
                        }
                    });
            
            list.add(new TextItem("Sound volume: " + valueEdit(mconf.soundsVolume), 
                    font) {
                        public boolean onRight() {
                            main.clickedS.play();
                            mconf.soundsVolume = Math.min(100, mconf.soundsVolume+5);
                            mconf.applyAudio();
                            setText("Sound volume: " + valueEdit(mconf.soundsVolume), list);
                            return true;
                        }

                        public boolean onLeft() {
                            main.clickedS.play();
                            mconf.soundsVolume = Math.max(0, mconf.soundsVolume-5);
                            mconf.applyAudio();
                            setText("Sound volume: " + valueEdit(mconf.soundsVolume), list);
                            return true;
                        }
                    });
            
            /*list.add(new TextItem("Footsteps volume: " + valueEdit(mconf.footstepsVolume), 
                    font) {
                        public boolean onRight() {
                            main.clickedS.play();
                            mconf.footstepsVolume = Math.min(100, mconf.footstepsVolume+5);
                            mconf.applyAudio();
                            setText("Footsteps volume: " + valueEdit(mconf.footstepsVolume), list);
                            return true;
                        }

                        public boolean onLeft() {
                            main.clickedS.play();
                            mconf.footstepsVolume = Math.max(0, mconf.footstepsVolume-5);
                            mconf.applyAudio();
                            setText("Footsteps volume: " + valueEdit(mconf.footstepsVolume), list);
                            return true;
                        }
                    });*/
            
        } else if(type == VIDEO) {
            list.add(new TextItem("Launch game in fullscreen: " + checkBox(mconf.startInFullscr),
                    font) {
                        public void onEnter() {
                            main.clickedS.play();
                            mconf.startInFullscr ^= true;
                            setText("Launch game in fullscreen: " + checkBox(mconf.startInFullscr), list);
                        }
                    });
            list.add(new TextItem("(Press F11 to toggle)", font).setHCenter(true).setSkip(true));
            
            list.add(new TextItem("Fullscreen size:", font).setHCenter(true).setSkip(true));
            
            fullscr = new TextBoxItem(main, font, 2);
            fullscr.setOnlyDigit(true);
            fullscr.setText(new String[]{String.valueOf(mconf.fw), String.valueOf(mconf.fh)});
            list.add(fullscr);
            
            list.add(new TextItem("Windowed size:", font).setHCenter(true).setSkip(true));
            
            windowres = new TextBoxItem(main, font, 2);
            windowres.setOnlyDigit(true);
            windowres.setText(new String[]{String.valueOf(mconf.ww), String.valueOf(mconf.wh)});
            list.add(windowres);
            
            list.addVoid(font);
            list.add(new TextItem("Quality settings:", font).setHCenter(true).setSkip(true));
            
            list.add(new TextItem("Antialiasing: "+mconf.aa+"x",
                    font) {
                        public boolean onRight() {
                            main.clickedS.play();
                            mconf.aa <<= 1;
                            if(mconf.aa > maxAA) mconf.aa = 1;
                            setText("Antialiasing: "+mconf.aa+"x", list);
                            return true;
                        }
                        
                        public boolean onLeft() {
                            main.clickedS.play();
                            if(mconf.aa == 1) mconf.aa = maxAA;
                            else mconf.aa >>= 1;
                            setText("Antialiasing: "+mconf.aa+"x", list);
                            return true;
                        }
                        
                        public void onEnter() {onRight();}
                    });
            
            list.add(new TextItem("Vsync: " + checkBox(mconf.vsync),
                    font) {
                        public void onEnter() {
                            main.clickedS.play();
                            mconf.vsync ^= true;
                            setText("Vsync: " + checkBox(mconf.vsync), list);
                        }
                    });
            
            list.add(new TextItem("PSX-Like render: " + checkBox(mconf.psxRender),
                    font) {
                        public void onEnter() {
                            main.clickedS.play();
                            mconf.psxRender ^= true;
                            setList(listType);
                        }
                    });
            
            if(mconf.psxRender) {
                list.add(new TextItem("Virtual resolution:", font).setHCenter(true).setSkip(true));
            
                vres = new TextBoxItem(main, font, 2);
                vres.setOnlyDigit(true);
                vres.setText(new String[]{String.valueOf(mconf.vrw), String.valueOf(mconf.vrh)});
                list.add(vres);

                list.add(new TextItem("Pseudo dithering: " + checkBox(mconf.dithering),
                        font) {
                            public void onEnter() {
                                main.clickedS.play();
                                mconf.dithering ^= true;
                                setText("Pseudo dithering: " + checkBox(mconf.dithering), list);
                            }
                        });
            }
            
        } else if(type == GAMEPLAY) {
            
            list.add(new TextItem("Mouse look speed: " + valueEdit(mconf.mouseLookSpeed), 
                    font) {
                        public boolean onRight() {
                            mconf.mouseLookSpeed += 5;
                            setText("Mouse look speed: " + valueEdit(mconf.mouseLookSpeed), list);
                            return true;
                        }

                        public boolean onLeft() {
                            mconf.mouseLookSpeed = Math.max(0, mconf.mouseLookSpeed - 5);
                            setText("Mouse look speed: " + valueEdit(mconf.mouseLookSpeed), list);
                            return true;
                        }
                    });
            
            list.add(new TextItem("Keyboard look speed: " + valueEdit(mconf.keyboardLookSpeed), 
                    font) {
                        public boolean onRight() {
                            mconf.keyboardLookSpeed += 5;
                            setText("Keyboard look speed: " + valueEdit(mconf.keyboardLookSpeed), list);
                            return true;
                        }

                        public boolean onLeft() {
                            mconf.keyboardLookSpeed = Math.max(0, mconf.keyboardLookSpeed - 5);
                            setText("Keyboard look speed: " + valueEdit(mconf.keyboardLookSpeed), list);
                            return true;
                        }
                    });
            
            /*list.add(new TextItem("Gamepad look speed: " + valueEdit(mconf.gamepadLookSpeed), 
                    font) {
                        public boolean onRight() {
                            mconf.gamepadLookSpeed += 5;
                            setText("Gamepad look speed: " + valueEdit(mconf.gamepadLookSpeed), list);
                            return true;
                        }

                        public boolean onLeft() {
                            mconf.gamepadLookSpeed = Math.max(0, mconf.gamepadLookSpeed - 5);
                            setText("Gamepad look speed: " + valueEdit(mconf.gamepadLookSpeed), list);
                            return true;
                        }
                    });
        
            list.add(new TextItem("Change gamepad layout", font) {
                public void onEnter() {
                    main.clickedS.play();
                }
            });*/
        
            list.add(new TextItem("Remove progress", font) {
                public void onEnter() {
                    main.clickedS.play();
                    currentList = resetConfirm;
                }
            });
        }
        
        list.addVoid(font);
        list.add(new TextItem("Save&apply", font) {
            public void onEnter() {
                    main.clickedS.play();
                    mconf.fw = fullscr==null?mconf.fw:fullscr.getBox(0).toInteger();
                    mconf.fh = fullscr==null?mconf.fh:fullscr.getBox(1).toInteger();
                    
                    mconf.ww = windowres==null?mconf.ww:windowres.getBox(0).toInteger();
                    mconf.wh = windowres==null?mconf.wh:windowres.getBox(1).toInteger();
                    
                    mconf.vrw = vres==null?mconf.vrw:vres.getBox(0).toInteger();
                    mconf.vrh = vres==null?mconf.vrh:vres.getBox(1).toInteger();

                    if(!mconf.isValid()) {
                        currentList = message;
                        messageText.setText("Unsupported videomode :(", message);
                    } else {
                        mconf.apply(main.window, main.e3d, true);
                        
                        if(mconf.isNeedToConfirm(main.conf)) {
                            applyBegin = System.currentTimeMillis();
                            currentList = applyConfirm;
                        } else {
                            main.conf.copy(mconf);
                            main.conf.save();
                            main.setScreen(previous);
                        }
                    }
            }
        });
        list.add(new TextItem("Cancel", font) {
            public void onEnter() {
                    main.clickedS.play();
                    main.conf.applyAudio();
                    main.setScreen(previous);
            }
        });
        
        listType = type;
        if(wasList) {
            list.setIndex(Math.min(list.getItemsCount()-1, prevIndex));
            
            list.setYScroll(prevYScroll);
            list.limitYScroll();
        }
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        this.w = w; this.h = h;
        createList();
        previous.sizeChanged(w, h, this);
    }
    
    public void tick() {
        if(currentList == applyConfirm && System.currentTimeMillis() - applyBegin >= 10000) {
            main.conf.apply(main.window, main.e3d, true);
            setList(VIDEO);
        }
        
        if(previous instanceof Menu) {
            ((Menu)previous).drawBackground();
        } else if(previous instanceof Game) {
            Game game = (Game)previous;
            
            game.world.pausedAnimate(null);
            game.render();
        }
        main.hudRender.drawRect(0, 0, w, h, 0, 0.5f);
        
        if(currentList == applyConfirm) {
            applyConfirmText.setText(
                    (10 - Math.round((System.currentTimeMillis() - applyBegin) / 1000))
                    + " seconds before restoring settings", applyConfirm);
        }

        int listX = currentList == list ? w/4 : 0;
        currentList.mouseUpdate(listX, 0, (int)main.getMouseX(), (int)main.getMouseY());
        currentList.draw(main.hudRender, listX, 0, main.fontColor, main.fontSelColor);
    }
    
    public void mouseAction(int key, boolean pressed) {
        if(key == Screen.MOUSE_LEFT) {
            currentList.mouseAction( 
                    currentList == list ? w/4 : 0, 0, 
                    (int)main.getMouseX(), (int)main.getMouseY(),
                    pressed);
        }
    }
    
    public void keyPressed(int key) {
        if(Keys.isThatBinding(key, Keys.ESC)) {
            if(currentList == list) {
                main.clickedS.play();

                main.conf.applyAudio();
                main.setScreen(previous);
                return;
            }
        }
        
        currentList.keyPressed(key);
    }
    
    public void keyRepeated(int key) {
        currentList.keyRepeated(key);
    }
    
    public void mouseScroll(double xx, double yy) {
        currentList.mouseScroll((int) (yy*main.scrollSpeed()));
    }

}