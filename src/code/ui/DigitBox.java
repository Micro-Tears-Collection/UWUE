package code.ui;

import code.engine.Screen;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class DigitBox extends TextBox {

    public DigitBox(Screen scr, BMFont font) {
        super(scr, font);
    }
    
    public void addChars(char[] chrs) {
        for(int i=0; i<chrs.length; i++) {
            char ch = chrs[i];
            
            if(Character.isDigit(ch)) text += ch;
        }
    }

}
