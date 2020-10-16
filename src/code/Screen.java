package code;

import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Roman Lahin
 */
public class Screen {
    
    public static int MOUSE_LEFT = GLFW.GLFW_MOUSE_BUTTON_LEFT, 
            MOUSE_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            MOUSE_RIGHT = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    
    public boolean isRunning() {return true;}
    public void destroy() {}
    public void tick() {}
    
    public void keyPressed(int key) {}
    public void keyReleased(int key) {}
    public void mouseAction(int button, boolean pressed) {}
    public void mouseScroll(double xoffset, double yoffset) {}
    
    public void sizeChanged(int w, int h, Screen from) {}
    
    public final int getWidth() {
        return Engine.w;
    }
    
    public final int getHeight() {
        return Engine.h;
    }
    
    public final int getMouseX() {
        double[] xx = new double[1];
        GLFW.glfwGetCursorPos(Engine.window, xx, null);
        return (int)xx[0];
    }
    
    public final int getMouseY() {
        double[] yy = new double[1];
        GLFW.glfwGetCursorPos(Engine.window, null, yy);
        return (int)yy[0];
    }
    
    public final boolean isFocused() {
        return GLFW.glfwGetWindowAttrib(Engine.window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
    }
    
    public final void setCursorPos(int x, int y) {
        GLFW.glfwSetCursorPos(Engine.window, x, y);
    }
    
    public final boolean userTryingToCloseApp() {
        return GLFW.glfwWindowShouldClose(Engine.window);
    }
    
}
