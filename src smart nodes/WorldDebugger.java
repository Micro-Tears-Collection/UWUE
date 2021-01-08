package code.game;

import code.game.world.Node;
import code.ui.ItemList;
import code.utils.Keys;
import java.util.Vector;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Roman Lahin
 */
public class WorldDebugger {
    
    Game game;
    
    ItemList menu;
    public Vector<Node> path;
    
    public WorldDebugger(Game game) {
        this.game = game;
        path = new Vector();
    }
    
    public void reload() {
        path.clear();
        createMenu();
    }
    
    public void sizeChanged() {
        createMenu();
    }

    private void createMenu() {
        if(game.world == null) return;
        
        Vector<String> list = new Vector();
        Node current = path.isEmpty()?null:path.lastElement();
        
        list.add(current==null?"<root>":("<"+path.size()+": "+current.toString()+">"));
        
        if(current != null) {
            for(int i=0; i<current.childs.size(); i++) {
                list.add(current.childs.elementAt(i).toString());
            }
        } else {
            for(int i=0; i<game.world.renderNodes.size(); i++) {
                list.add(game.world.renderNodes.elementAt(i).toString());
            }
        }
        
        menu = new ItemList(list.toArray(new String[list.size()]), 
                game.getWidth()/2, game.getHeight(), game.main.font) {
                    public void itemSelected() {
                        game.main.selectedS.play();
                    }
                };
        menu.setIndex(0);
    }
    
    public void draw2D() {
        if(game.world == null) return;
        
        game.e3d.drawRect(null, game.getWidth()/2, 0, game.getWidth()/2, game.getHeight(), 0, 0.5f);
        menu.draw(game.e3d, game.getWidth() / 2, 0, game.main.fontColor, game.main.fontSelColor, false);
    }
    
    public void draw3D() {
        if(game.world == null) return;
        
        Node last = path.isEmpty()?null:path.lastElement();
        Node current = last!=null&&menu.getIndex()>0?last.childs.elementAt(menu.getIndex()-1):null;
        if(current == null && menu.getIndex() > 0) 
            current = game.world.renderNodes.elementAt(menu.getIndex()-1);
        
        if(last != null) {
            game.e3d.drawCube(last.min, last.max, current==null?0xffff00:0x0000ff, 1);
            drawNode(false, last, 0, current);
        }
        
        if(current != null) {
            game.e3d.drawCube(current.min, current.max, 0xffff00, 1);
            drawNode(false, current, 0x800000, null);
        }
    }
    
    private void drawNode(boolean drawThis, Node node, int color, Node except) {
        if(drawThis) game.e3d.drawCube(node.min, node.max, color, 1);
        
        for(Node child : node.childs) {
            if(child != except) drawNode(true, child, color, except);
        }
    }
    
    public boolean keyReleased(int key) {
        if(game.world == null) return false;
        
        if(key == GLFW.GLFW_KEY_KP_2) {
            menu.scrollDown();
            Keys.reset();
            return true;
        } else if(key == GLFW.GLFW_KEY_KP_8) {
            menu.scrollUp();
            Keys.reset();
            return true;
        }

        if(key == GLFW.GLFW_KEY_KP_5) {
            Keys.reset();
            open();
            return true;
        }
        
        return false;
    }
    
    private void open() {
        int index = menu.getIndex();
        if(index == -1) return;
        
        if(index == 0) {
            if(!path.isEmpty()) path.removeElementAt(path.size()-1);
        } else if(index > 0) {
            if(!path.isEmpty()) path.add(path.lastElement().childs.elementAt(index-1));
            else path.add(game.world.renderNodes.elementAt(index-1));
        }
        
        createMenu();
    }

}
