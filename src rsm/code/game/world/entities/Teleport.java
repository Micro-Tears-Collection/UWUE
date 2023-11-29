package code.game.world.entities;

import code.game.Main;
import code.game.scripting.Scripting;
import code.math.Vector3D;

/**
 *
 * @author Roman Lahin
 */
public class Teleport extends Entity {
    
    Vector3D newPos;
    boolean useOffset;
    
    public Teleport(Vector3D newPos, boolean useOffset) {
        this.newPos = newPos;
        this.useOffset = useOffset;
        
        activable = true;
        clickable = false;
        pointable = false;
    }
    
    public void destroy() {
        super.destroy();
        newPos = null;
    }
    
    protected boolean activateImpl(Main main) {
        if(useOffset) {
            main.getGame().player.pos.add(newPos.x-pos.x, newPos.y-pos.y, newPos.z-pos.z);
        } else main.getGame().player.pos.set(newPos);
        
        if(onActivate != null) Scripting.runScript(onActivate);
        
        return true;
    }

}
