package code.utils.font;

import code.engine3d.HudRender;
import code.engine3d.Texture;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

/**
 *
 * @author Roman Lahin
 */
public class BMFont {
    
    private Texture[] pages;
    
    public String name;
    private int fontSize, stretchH;
    public float baseScale = 1;
    
    private Hashtable<Integer, BMChar> chars;
    
    public BMFont() {
        chars = new Hashtable();
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
    
    public int getOriginalHeight() {
        return fontSize;
    }
    
    public int getHeight() {
        return Math.round(fontSize * baseScale);
    }
    
    public void drawString(HudRender hudRender, String text, float x, float y, float scale, int color) {
        drawString(hudRender, text, x, y, scale, color, 1);
    }
    
    public void drawString(HudRender hudRender, String text, float x, float y, float scale, int color, float a) {
        float scale2 = baseScale * scale;

        int prevCP = 0;
        //int prevPage = -1;
        int dx = 0;
        for(int i=0; i<text.length(); i++) {
            BMChar c = chars.get(Character.codePointAt(text, i));
            if(c == null) continue;
            
            if(prevCP != 0) {
                Integer kerning = c.kerning.get(prevCP);
                if(kerning != null) dx += kerning;
            }
            
            /*if(prevPage != c.texPage) {
                prevPage = c.texPage;
            }*/
            
            hudRender.drawRect(pages[c.texPage], 
                    x+(dx+c.x)*scale2, y+(c.y)*scale2, 
                    c.w*scale2, c.h*scale2,
                    c.u1, c.v1, c.u2, c.v2, color, a);
            
            dx += c.xAdvance;
            prevCP = c.cp;
        }
    }
    
    public void destroy() {
        if(pages != null) for(int i=0; i<pages.length; i++) {
            pages[i].destroy();
        }
        pages = null;
        
        /*Enumeration<BMChar> els = chars.elements();
        while(els.hasMoreElements()) {
            els.nextElement().destroy();
        }*/
        chars = null;
    }

}
