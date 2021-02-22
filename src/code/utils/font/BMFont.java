package code.utils.font;

import code.engine3d.Material;
import code.engine3d.Texture;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author Roman Lahin
 */
public class BMFont {
    
    private Texture[] pages;
    private Material mat;
    
    public String name;
    private int fontSize, stretchH;
    public float baseScale = 1;
    
    private Hashtable<Integer, BMChar> chars;
    
    public BMFont() {
        chars = new Hashtable();
        
        mat = new Material(null);
        mat.alphaTest = false;
        mat.linearInterpolation = true;
        mat.mipMapping = false;
        mat.blendMode = Material.BLEND;
    }
    
    public void setInterpolation(boolean interp) {
        mat.linearInterpolation = interp;
    }
    
    /**
     * Should be destroyed after using!
     * @param path
     * @return BMFont or null
     */
    public static BMFont loadFont(String path) {
        try {
            File file = new File("data", path);
            
            if(!file.exists()) {
                System.out.println("No such file "+file.getAbsolutePath());
                return null;
            }
            
            String texp = path;
            texp = texp.substring(0, texp.lastIndexOf('/')+File.separator.length());
            
            FileInputStream is = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(is);
            
            dis.skip(3); //BMF
            dis.skip(1); //Version
            
            BMFont font = new BMFont();
            int tw = 0, th = 0;
            
            while(dis.available() > 0) {
                int blockType = dis.read();
                int blockSize = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
                
                if(blockType == 1) {
                    //Info
                    font.fontSize = dis.read() | (dis.read() << 8);
                    dis.skip(1); //bitField
                    dis.skip(1); //charset
                    font.stretchH = dis.read() | (dis.read() << 8);
                    dis.skip(1); //aa
                    dis.skip(4); //padding
                    dis.skip(2); //spacing
                    dis.skip(1); //outline
                    
                    StringBuffer sb = new StringBuffer();
                    while(true) {
                        char c = (char)dis.read();
                        if(c == '\0') break;
                        sb.append(c);
                    }
                    font.name = sb.toString();
                    
                } else if(blockType == 2) {
                    //Common
                    dis.skip(2); //lineHeight
                    dis.skip(2); //distance from top to base
                    tw = dis.read() | (dis.read() << 8);
                    th = dis.read() | (dis.read() << 8);
                    font.pages = new Texture[dis.read() | (dis.read() << 8)];
                    dis.skip(5); //packed + argb
                    
                } else if(blockType == 3) {
                    //Pages
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0; i < font.pages.length; i++) {
                        while(true) {
                            char c = (char) dis.read();
                            if(c == '\0') break;
                            sb.append(c);
                        }
                        
                        font.pages[i] = Texture.loadTexture(texp + sb.toString());
                        sb.delete(0, sb.length());
                    }
                    
                } else if(blockType == 4) {
                    //Chars
                    int count = blockSize / 20;
                    
                    for(int i = 0; i < count; i++) {
                        int cp = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
                        
                        BMChar c = new BMChar(font, cp,
                                //x, y
                                dis.read() | (dis.read() << 8),
                                dis.read() | (dis.read() << 8),
                                //w, h
                                dis.read() | (dis.read() << 8),
                                dis.read() | (dis.read() << 8),
                                //xoff, yoff
                                (short) (dis.read() | (dis.read() << 8)),
                                (short) (dis.read() | (dis.read() << 8)),
                                //xadv
                                (short) (dis.read() | (dis.read() << 8)),
                                dis.read(),
                                tw, th
                        );
                        
                        font.chars.put(cp, c);
                        dis.skip(1); //chnl
                    }
                } else if(blockType == 5) {
                    //Kerning pairs
                    int count = blockSize / 10;
                    
                    for(int i = 0; i < count; i++) {
                        int c1 = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
                        int c2 = dis.read() | (dis.read() << 8) | (dis.read() << 16) | (dis.read() << 24);
                        int xoff = (short) (dis.read() | (dis.read() << 8));
                        
                        BMChar c = font.chars.get(c1);
                        if(c != null) c.kerning.put(c2, xoff);
                    }
                } else dis.skip(blockSize);
            }
            
            dis.close();
            
            return font;
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public int stringWidth(String text) {
        int prevCP = 0;
        int w = 0;
        
        for(int i=0; i<text.length(); i++) {
            BMChar c = chars.get(Character.codePointAt(text, i));
            if(c == null) continue;
            
            if(prevCP != 0) {
                Integer kerning = c.kerning.get(prevCP);
                if(kerning != null) w += kerning;
            }
            
            w += c.xAdvance;
            prevCP = c.cp;
        }
        
        return (int) (w * baseScale);
    }

    public String getCharByX(String text, int x) {
        if(x < 0) return null;
        int prevCP = 0;
        int w = 0;
        
        for(int i=0; i<text.length(); i++) {
            BMChar c = chars.get(Character.codePointAt(text, i));
            if(c == null) continue;
            
            if(prevCP != 0) {
                Integer kerning = c.kerning.get(prevCP);
                if(kerning != null) w += kerning;
            }
            
            w += c.xAdvance;
            if(w*baseScale > x) return String.valueOf(Character.toChars(c.cp));
            prevCP = c.cp;
        }
        
        return null;
    }
    
    public int cpWidth(int cp) {
        BMChar c = chars.get(cp);
        if(c != null) return (int) (c.xAdvance * baseScale);
        return 0;
    }
    
    public int cpWidth(int cp, int prevCP) {
        BMChar c = chars.get(cp);
        if(c != null) {
            int w = c.xAdvance;
            
            Integer kerning = c.kerning.get(prevCP);
            if(kerning != null) w += kerning;
            
            return (int) (w * baseScale);
        }
        return 0;
    }
    
    public int getHeight() {
        return (int) (fontSize * baseScale);
    }
    
    public void drawString(String text, int x, int y, float scale, int color) {
        drawString(text, x, y, scale, color, 1);
    }
    
    public void drawString(String text, int x, int y, float scale, int color, float a) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        
        float scale2 = baseScale * scale;
        GL11.glLoadIdentity();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale2, scale2, scale2);
        
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glEnableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        
        GL11.glColor4f(((color>>16)&255) / 255f, 
                ((color>>8)&255) / 255f, 
                (color&255) / 255f, a);
        

        int prevCP = 0;
        int prevPage = -1;
        float px = x;
        for(int i=0; i<text.length(); i++) {
            BMChar c = chars.get(Character.codePointAt(text, i));
            if(c == null) continue;
            
            if(prevCP != 0) {
                Integer kerning = c.kerning.get(prevCP);
                if(kerning != null) x += kerning;
            }
            
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, c.coordVBO);
            GL15.glVertexPointer(3, GL15.GL_FLOAT, 0, 0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, c.uvVBO);
            GL15.glTexCoordPointer(2, GL15.GL_FLOAT, 0, 0);
            
            if(prevPage != c.texPage) {
                mat.tex = pages[c.texPage];
                mat.bind();
                prevPage = c.texPage;
            }
            
            GL11.glTranslatef(x-px, 0, 0);
            px = x;
            GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
            
            x += c.xAdvance;
            prevCP = c.cp;
        }
        
        mat.unbind();
        
        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glDisableClientState(GL15.GL_TEXTURE_COORD_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL11.glColor4f(1, 1, 1, 1);
        
        GL11.glPopMatrix();
    }
    
    public void destroy() {
        if(pages != null) for(int i=0; i<pages.length; i++) {
            pages[i].destroy();
        }
        pages = null;
        
        Enumeration<BMChar> els = chars.elements();
        while(els.hasMoreElements()) {
            els.nextElement().destroy();
        }
        chars = null;
    }

}
