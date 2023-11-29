package code.engine;

import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Roman Lahin
 */
public class Screen {
    
    public static int MOUSE_LEFT = GLFW.GLFW_MOUSE_BUTTON_LEFT, 
            MOUSE_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            MOUSE_RIGHT = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    
    public void show() {}
    public void destroy() {}
    public void tick() {}
    
    public void keyPressed(int key) {}
    public void keyRepeated(int key) {}
    public void keyReleased(int key) {}
    public void charInput(int cp) {}
    
    public void openTextBox(Object textBox) {}
    public void closeTextBox() {}
    
    public void mouseAction(int button, boolean pressed) {}
    public void mouseScroll(double xoffset, double yoffset) {}
    
    public void sizeChanged(int w, int h, Screen from) {}
    
}
