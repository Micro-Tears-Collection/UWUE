package code.ui;

import code.game.Main;
import code.utils.font.BMFont;

/**
 *
 * @author Roman Lahin
 */
public class DigitBox extends TextBox {

    public DigitBox(Main main, BMFont font) {
        super(main, font);
    }
    
    public void addChars(char[] chrs) {
        for(int i=0; i<chrs.length; i++) {
            char ch = chrs[i];
            
            if(Character.isDigit(ch)) text += ch;
        }
    }

}
