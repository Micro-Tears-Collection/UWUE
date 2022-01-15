package code.game;

import code.engine.Screen;

import code.game.scripting.Scripting;

import code.ui.itemList.ItemList;
import code.ui.TextView;
import code.ui.itemList.TextItem;

import code.utils.assetManager.AssetManager;
import code.utils.Keys;
import code.utils.StringTools;
import code.utils.font.BMFont;

public class DialogScreen extends Screen {

    public Game game;
    private int w, h;
    
    private BMFont font;
    private String dialogPath;
    private TextView textView;
    
    private ItemList itemList;
    private boolean itemListHasCaption;
    private int[] answersGoIndex;

    private boolean reset;
    private int index = -1;
    private String[] dialog;

    public DialogScreen() {}
    
    public void destroy() {
        game.destroy();
    }
    
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
    
    public void load(String path, Game game, BMFont font) {
        set(AssetManager.loadLines(path), game, font);
        dialogPath = path;
    }
    
    public void set(String text, Game game, BMFont font) {
        set(StringTools.cutOnStrings(text, '@'), game, font);
    }
    
    public void set(String[] text, Game game, BMFont font) {
        w = game.main.getWidth(); h = game.main.getHeight();
        this.font = font;
        dialogPath = null;
        reset = true;

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
    }
    
    private void initView() {
        if(font == null) return;
        
        int h2 = (int) (game.getViewportH() / 3.5f);
        if(h2 < font.getHeight() * 3) h2 = font.getHeight() * 3;
        int w2 = game.getViewportW() - 20;
        
        if(textView == null) textView = new TextView(w2, h2, font);
        else textView.setSize(w2, h2);
    }
    
    public void open() {
        game.main.setScreen(this);
        initView();
        game.main.window.showCursor(true);
    }
    
    public void show() {
        while(reset) {
            reset = false;
            nextText();
        }
    }

    private boolean nextText() {
        if(reset) return false;
        
        if(itemList != null) {
            if(itemList.getIndex() == -1) return true;
            
            if(answersGoIndex == null) 
                index += itemList.getItemsCount()+itemList.getIndex()-(itemListHasCaption?1:0); // Additional move
            else index = answersGoIndex[itemList.getIndex()-(itemListHasCaption?1:0)]-1;
            
            itemList = null; answersGoIndex = null;
        }
        
        
        if(index + 1 < dialog.length) {
            index++;
            
            if(dialog[index].charAt(0)!='$') {
                textView.setText(dialog[index]);
                textView.setYScroll(0);
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
                    
                    Scripting.runScriptFromFile(game.main, option);
                    
                    return nextText();
                } else if(script.equals("if")) {
                    //condition check
                    
                    if(!Scripting.runScript(game.main, "return "+option).toboolean()) index++;
                    
                    return nextText();
                } else if(script.equals("cmd")) {
                    //run one script line
                    
                    Scripting.runScript(game.main, option);
                    
                    return nextText();
                } else if(script.equals("go")) {
                    //go to line
                    
                    return goToLabel(option);
                } else if(script.equals("question")) {
                    //ask question
                    
                    itemList = ItemList.createItemList(
                            textView.getWidth(), textView.getHeight(), 
                            font, game.main.selectedS);
                    
                    String[] arguments = StringTools.cutOnStrings(option, ' '); // &question& length is 10
                    boolean generative = (arguments.length>=2 && arguments[0].equalsIgnoreCase("gen"));
                    itemListHasCaption = !(arguments.length>=2 && arguments[arguments.length-2].equalsIgnoreCase("nocap"));
                    int capLen = (itemListHasCaption?1:0);
                    
                    //Caption
                    if(itemListHasCaption) 
                        itemList.add((new TextItem(dialog[index+1], game.main.font)).setSkip(true));
                    
                    int answers = StringTools.parseInt(arguments[arguments.length-1]);
                    if(generative) {
                        answersGoIndex = new int[answers];
                    }
                    
                    int answersCount = 0;
                    
                    for(int i=0; i<answers; i++) {
                        String answer = dialog[index+1+capLen+i];
                        String condition = dialog[index+1+capLen+i+answers];
                        boolean add = !generative;
                        
                        if(generative && Scripting.runScript(game.main, "return "+condition).toboolean()) {
                            add = true;
                            answersGoIndex[answersCount] = index+1+capLen+i+answers*2;
                        }
                        
                        if(add) {
                            itemList.add(new TextItem(answer, game.main.font){
                                public void onEnter() {
                                    onPress();
                                }
                            });
                            answersCount++;
                        }
                    }
                    
                    if(answersCount == 0) {
                        index += capLen + answers * 3;
                        return nextText();
                    }
                    
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
        if(game.nextMap != null) game.loadMapImpl();
        game.world.pausedAnimate(null);
        game.render();

        int x = getDialogX();
        int y = getDialogY();
        game.main.hudRender.drawWindow(game.getViewportX(), y, game.getViewportW(), textView.getHeight());
        
        //Draw dialog
        int textBegin, textEnd;
        if(itemList == null) {
            textBegin = y+textView.getYScroll();
            textEnd = textBegin + textView.getTextHeight();
            textView.draw(game.main.hudRender, x, y, game.main.fontColor);
        } else { //Draw question
            textBegin = y+itemList.getYScroll();
            textEnd = textBegin + itemList.getFullHeight();
            
            itemList.mouseUpdate(x, y, (int)game.main.getMouseX(), (int)game.main.getMouseY());
            
            itemList.draw(game.main.hudRender, x, y, game.main.fontColor, game.main.fontSelColor);
        }
        
        if((textView.getTextHeight() > textView.getHeight() && itemList == null)
                || (itemList != null && itemList.getFullHeight() > textView.getHeight())) {

            //Down arrow
            if(textEnd > y + textView.getHeight()) game.main.hudRender.drawArrow(
                        game.getViewportX() + game.getViewportW() - 10 - 3, y + textView.getHeight() - 10 - 3,
                        20, 20, 90, game.main.fontColor, 1);

            //Up arrow
            if(textBegin < y) game.main.hudRender.drawArrow(
                        game.getViewportX() + game.getViewportW() - 10 - 3, y + 10 + 3,
                        20, 20, -90, game.main.fontColor, 1);
            /*game.e3d.drawRect(null,
                    x + textView.getWidth()+5, y - textView.getHeight() * textView.getY() / textView.getTextHeight(),
                    50, textView.getHeight() * textView.getHeight() / textView.getTextHeight(),
                    0xffffff, 0.5f);*/
        }

        step();
    }

    private void step() {
        if(itemList == null) {
            if(Keys.isPressed(Keys.DOWN)) textView.scroll(-3);
            if(Keys.isPressed(Keys.UP)) textView.scroll(3);
        }
    }
    
    public void onPress() {
        game.main.clickedS.play();
        
        if(!nextText()) {
            if(reset) {
                show();
                return;
            }

            itemList = null;
            dialog = null;

            game.main.window.showCursor(false);
            game.main.setScreen(game);
            return;
        }
    }
    
    public void keyPressed(int key) {
        if(itemList != null) itemList.keyPressed(key);
        else {
            if(Keys.isThatBinding(key, Keys.OK)) onPress();
        }
    }
    
    public void keyRepeated(int key) {
        if(itemList != null) itemList.keyRepeated(key);
    }
    
    public void mouseAction(int button, boolean pressed) {
        if(itemList == null) {
            if(!pressed || button != Screen.MOUSE_LEFT) onPress();
        } else {
            itemList.mouseAction(
                    getDialogX(), getDialogY(), 
                    (int)game.main.getMouseX(), (int)game.main.getMouseY(), 
                    pressed);
        }
    }
    
    public void mouseScroll(double xx, double yy) {
        int scroll = (int) (yy*game.main.scrollSpeed());
        
        if(itemList == null) textView.scroll(scroll);
        else itemList.mouseScroll(scroll);
    }
    
    private final int getDialogX() {
        return game.getViewportX() + (game.getViewportW() - textView.getWidth()) / 2;
    }
    
    private final int getDialogY() {
        return game.getViewportY() + game.getViewportH() - textView.getHeight() - game.main.hudRender.getWindowYBorder();
    }
}
