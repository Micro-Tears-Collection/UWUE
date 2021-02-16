package code.utils.font;

import java.util.Hashtable;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class BMChar {
    
    Hashtable<Integer, Integer> kerning;
    
    int cp;
    int xAdvance, texPage;
    
    int coordVBO, uvVBO;

    BMChar(BMFont font, int cp, 
            int x, int y, 
            int w, int h, 
            int xoff, int yoff, int xadv, 
            int page, int tw, int th) {
        this.cp = cp;
        xAdvance = xadv;
        texPage = page;
        kerning = new Hashtable();
        
        float[] coord = new float[] {
            xoff, yoff, 0, xoff+w, yoff, 0,
            xoff+w, yoff+h, 0, xoff, yoff+h, 0
        };
        
        coordVBO = GL15.glGenBuffers(); //Creates a VBO ID
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, coordVBO); //Loads the current VBO to store the data
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, coord, GL15.GL_STATIC_DRAW);
        
        float u1 = (float) x / tw, u2 = (float) (x+w) / tw;
        float v1 = (float) y / th, v2 = (float) (y+h) / th;
        float[] uvm = new float[] {
            u1, v1, u2, v1,
            u2, v2, u1, v2
        };
        
        uvVBO = GL15.glGenBuffers(); //Creates a VBO ID
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVBO); //Loads the current VBO to store the data
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvm, GL15.GL_STATIC_DRAW);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
    }
    
    void destroy() {
        GL15.glDeleteBuffers(coordVBO);
        GL15.glDeleteBuffers(uvVBO);
    }

}
