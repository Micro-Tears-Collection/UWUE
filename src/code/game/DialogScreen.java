package code.game;

import code.Engine;
import code.Screen;
import code.engine3d.E3D;
import code.ui.ItemList;
import code.ui.TextView;
import code.utils.Keys;
import code.utils.StringTools;
import code.utils.font.BMFont;

public class DialogScreen extends Screen {

    private Game game;
    private int w,h;
    
    private BMFont font;
    private String dialogPath;
    private TextView textView;
    
    private ItemList itemList;
    private boolean itemListHasCaption;
    private int[] answersGoIndex;

    boolean reset;
    private int index = -1;
    private String[] dialog;
    
    //private int pX, pY;

    public DialogScreen() {}
    
    public void sizeChanged(int x, int y, Screen from) {
        this.w = x; this.h = y;
        
        if(from != game && game != null) game.sizeChanged(x, y, this);
        
        initView();
        if(index != -1 && dialog != null) {
            itemList = null;
            index--;
            nextText();
        }
    }
    
    public void set(String text, Game game, BMFont font) {
        String newText = null;
        String[] dial;
        
        if(text.charAt(0)=='/' && text.toLowerCase().endsWith(".txt")) {
            newText = StringTools.getStringFromResource(text); //Load text from file
        }
        
        if(newText==null) dial = StringTools.cutOnStrings(text, '@'); 
        else dial = StringTools.cutOnStrings(newText, '\n'); 
        
        set(dial,game,font);
        
        dialogPath = text;
    }
    
    public void set(String[] text, Game game, BMFont font) {
        Keys.reset();
        w = getWidth(); h = getHeight();
        this.font = font;
        dialogPath = null;

        this.game = game;
        itemList = null;
        index = -1;
        
        dialog = text;
        
        for(int i = 0; i < dialog.length; i++) {
            String s = dialog[i];
            if(s.charAt(0) == '\n') s = s.substring(1, s.length());
            if(s.charAt(s.length() - 1) == '\n') s = s.substring(0, s.length() - 1);
            
            dialog[i] = s;
        }
        
        initView();
        nextText();
    }
    
    private void initView() {
        if(font == null) return;
        
        int h2 = (int) (h / 3.5f);
        if(h2 < font.getHeight() * 3) h2 = font.getHeight() * 3;
        
        textView = new TextView(null, w - 20, h2, font);
    }
    
    public void show() {
        game.stop();
        game.main.setScreen(this);
        Engine.hideCursor(false);
    }
    
    public void reset() {
        reset = false;
        nextText();
    }

    private boolean nextText() {
        if(reset) return false;
        
        if(itemList != null) {
            if(itemList.getIndex() == -1) return true;
            
            if(answersGoIndex == null) 
                index += itemList.getItems().length+itemList.getIndex()-(itemListHasCaption?1:0); // Additional move
            else index = answersGoIndex[itemList.getIndex()-(itemListHasCaption?1:0)]-1;
            
            itemList = null; answersGoIndex = null;
        }
        
        
        if(index + 1 < dialog.length) {
            index++;
            
            if(dialog[index].charAt(0)!='$') {
                textView.setString(dialog[index]);
                textView.setY(0);
                return true;
            } else {
                String text = dialog[index];
                
                int spacePlace = text.indexOf(' ');
                
                String script;
                if(spacePlace!=-1) script = text.substring(1, spacePlace).toLowerCase();
                else script = text.substring(1);
                
                String option = text;
                if(spacePlace!=-1) option = text.substring(spacePlace+1);
                
                
                
                if(script.equals("exec")) {
                    //execute script
                    
                    game.main.runScriptFromFile(option);
                    
                    return nextText();
                } else if(script.equals("if")) {
                    //condition check
                    
                    if(!game.main.runScript("return "+option).toboolean()) index++;
                    
                    return nextText();
                } else if(script.equals("cmd")) {
                    //run one script line
                    
                    game.main.runScript(option);
                    
                    return nextText();
                } else if(script.equals("go")) {
                    //go to line
                    
                    return goToLabel(option);
                } else if(script.equals("question")) {
                    //ask question
                    
                    String[] arguments = StringTools.cutOnStrings(option, ' '); // &question& length is 10
                    boolean generative = (arguments.length>=2 && arguments[0].equalsIgnoreCase("gen"));
                    itemListHasCaption = !(arguments.length>=2 && arguments[arguments.length-2].equalsIgnoreCase("nocap"));
                    int capLen = (itemListHasCaption?1:0);
                    
                    int answers = StringTools.parseInt(arguments[arguments.length-1]);
                    String[] items;
                    
                    if(generative) {
                        int newAnswers = 0;
                        String[] allItems = new String[answers];
                        answersGoIndex = new int[answers];
                        
                        for(int i=0;i<answers;i++) {
                            String answer = dialog[index+1+capLen+i];
                            String condition = dialog[index+1+capLen+i+answers];
                            
                            if(game.main.runScript("return "+condition).toboolean()) {
                                allItems[newAnswers] = answer;
                                answersGoIndex[newAnswers] = index+1+capLen+i+answers*2;
                                newAnswers++;
                            }
                        }
                        
                        if(newAnswers == 0) {
                            index += capLen+answers*3;
                            return nextText();
                        }
                        
                        items = new String[ newAnswers + capLen ]; //Questions count + caption
                        System.arraycopy(allItems, 0, items, capLen, newAnswers); //Copy questions to itemList
                        
                    } else {
                        items = new String[ answers + capLen ]; //Questions count + caption
                        System.arraycopy(dialog, index+capLen+1, items, capLen, answers); //Copy questions to itemList
                    }
                    
                    if(itemListHasCaption) items[0] = dialog[index+1]; //Caption
                    
                    itemList = new ItemList(items, textView.getWidth(), textView.getHeight(), font) {
                        public void itemSelected() {
                            if(!itemListHasCaption || itemList.getIndex() > 0) game.main.selectedS.start();
                        }
                    };
                    itemList.setCenter(false);
                    itemList.setIndex(capLen);
                    
                    return true;
                } else if(script.equals("end")) {
                    //exit
                    
                    return false;
                } else {
                    return nextText();
                }
            }
            
        }
        return false;
    }
    
    
    private boolean goToLabel(String option) {
        for (int lineId = 0; lineId < dialog.length; lineId++) {
            String line = dialog[lineId];

            if (option.length() == line.length() - 2 && //Is line length equals label naming length
                    line.charAt(0) == '$' && line.charAt(line.length() - 1) == ':' && //Is this a label
                    line.indexOf(option) == 1) { //Is label name equals option
                index = lineId;
                return nextText();
            }
        }

        System.out.println("Can't find label '"+option+"'");
        return false;
    }

    public void tick() {
        game.render();

        int x = getDialogX();
        int y = getDialogY();
        game.e3d.drawWindow(0, y, w, textView.getHeight(), font);
        
        //Draw dialog
        int textBegin, textEnd;
        if(itemList == null) {
            textBegin = y+textView.getY();
            textEnd = textBegin + textView.getTextHeight();
            textView.paint(game.e3d, x, y, game.main.fontColor);
        } else { //Draw question
            textBegin = y+itemList.getY();
            textEnd = textBegin + itemList.getHeight();
            
            itemList.mouseUpdate(x, y, getMouseX(), getMouseY());
            if(itemList.getIndex() == 0 && itemListHasCaption) itemList.setIndex(-1);
            
            itemList.draw(game.e3d, x, y, game.main.fontColor, game.main.fontSelColor, false);
        }
        
        if((textView.getTextHeight() > textView.getHeight() && itemList == null)
                || (itemList != null && itemList.getHeight() > textView.getHeight())) {

            //Down arrow
            if(textEnd > y + textView.getHeight()) game.e3d.drawArrow(
                        w - 10 - 3, y + textView.getHeight() - 10 - 3,
                        20, 20, 90, game.main.fontColor, 1);

            //Up arrow
            if(textBegin < y) game.e3d.drawArrow(
                        w - 10 - 3, y + 10 + 3,
                        20, 20, -90, game.main.fontColor, 1);
            /*game.e3d.drawRect(null,
                    x + textView.getWidth()+5, y - textView.getHeight() * textView.getY() / textView.getTextHeight(),
                    50, textView.getHeight() * textView.getHeight() / textView.getTextHeight(),
                    0xffffff, 0.5f);*/
        }

        step();
        game.e3d.flush();
    }

    private void step() {
        if(itemList == null) {
            if(Keys.isPressed(Keys.DOWN)) textView.scroll(-3);
            if(Keys.isPressed(Keys.UP)) textView.scroll(3);
        } else {
            if(Keys.isPressed(Keys.DOWN)) {
                itemList.scrollDown();
                if(itemList.getIndex()==0 && itemListHasCaption) itemList.scrollDown();
                Keys.reset();
            }
            if(Keys.isPressed(Keys.UP)) {
                itemList.scrollUp();
                if(itemList.getIndex()==0 && itemListHasCaption) itemList.scrollUp();
                Keys.reset();
            }
        }
        
        ok:
        if(Keys.isPressed(Keys.OK)) {
            Keys.reset();
            if(itemList != null && itemList.getIndex() == -1) break ok;
            
            game.main.clickedS.start();
            if(!nextText()) {
                if(reset) {
                    reset();
                    return;
                }
                
                itemList = null;
                dialog = null;

                /*game.paused = false;
                game.inDialog = false;
                game.updateItems(true);*/
                Engine.hideCursor(true);
                game.main.setScreen(game);
                game.start();
                return;
            }
        }
    }
    
    private final int getDialogX() {
        return (w - textView.getWidth()) / 2;
    }
    
    private final int getDialogY() {
        return h - textView.getHeight() - E3D.getWindowYBorder();
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index - 1;
        nextText();
    }
    
    public void mouseScroll(double xx, double yy) {
        int scroll = (int) (yy/2f*font.getHeight());
        
        if(itemList == null) textView.scroll(scroll);
        else itemList.scrollY(scroll);
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(pressed || button != Screen.MOUSE_LEFT) return;
        
        if(itemList == null) 
            Keys.keyPressed(Keys.getBindingKeyCode(Keys.OK, 0));
        else if(itemList.isInBox(getDialogX(), getDialogY(), getMouseX(), getMouseY())) 
            Keys.keyPressed(Keys.getBindingKeyCode(Keys.OK, 0));
    }

    public static String loadTextFromFile(String text) {
        if(text==null) return null;
        
        if(text.charAt(0)!='/' || !text.toLowerCase().endsWith(".txt")) return text;
        
        text = StringTools.getStringFromResource(text);
        return text.replace('\n', '@');
    }
}
