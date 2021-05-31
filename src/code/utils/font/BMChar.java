package code.utils.font;

import java.util.Hashtable;

/**
 *
 * @author Roman Lahin
 */
public class BMChar {
    
    Hashtable<Integer, Integer> kerning;
    
    int cp;
    int xAdvance, texPage;
    int x, y, w, h;
    float u1, v1, u2, v2;

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
        this.x = xoff;
        this.y = yoff;
        this.w = w;
        this.h = h;
        
        /*coordVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, coordVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, coord, GL33C.GL_STATIC_DRAW);*/
        
        u1 = (float) x / tw; 
        u2 = (float) (x+w) / tw;
        v1 = (float) y / th; 
        v2 = (float) (y+h) / th;
        /*float[] uvm = new float[] {
            u1, v1, u2, v1,
            u2, v2, u1, v2
        };
        
        uvVBO = GL33C.glGenBuffers(); //Creates a VBO ID
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, uvVBO); //Loads the current VBO to store the data
        GL33C.glBufferData(GL33C.GL_ARRAY_BUFFER, uvm, GL33C.GL_STATIC_DRAW);
        
        GL33C.glBindBuffer(GL33C.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.*/
    }
    
    /*void destroy() {
        GL33.glDeleteBuffers(coordVBO);
        GL33.glDeleteBuffers(uvVBO);
    }*/

}
