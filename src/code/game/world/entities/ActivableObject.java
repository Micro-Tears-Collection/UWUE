package code.game.world.entities;

import org.luaj.vm2.LuaValue;

/**
 *
 * @author Roman Lahin
 */
public class ActivableObject {
    
    LuaValue condition;
    LuaValue onActivate;
    
    public boolean activate(boolean click) {
        if(onActivate == null) return false;
        
        if(condition == null || condition.call().toboolean());
        return false;
    }
}
