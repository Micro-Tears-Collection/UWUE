package code.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyInput extends GLFWKeyCallback {

    Screen scr;

    public void invoke(long window, int key, int scancode, int action, int mods) {
        if(scr == null) return;
        
        if(action == GLFW.GLFW_PRESS) {
            scr.keyPressed(key);
        } else if(action == GLFW.GLFW_RELEASE) {
            scr.keyReleased(key);
        } else if(action == GLFW.GLFW_REPEAT) {
            if(key == GLFW.GLFW_KEY_BACKSPACE) {
                scr.keyPressed(key);
            }
        }
    }

    public void close() {
        super.close();
    }

    public void callback(long args) {
        super.callback(args);
    }

    public String getSignature() {
        return super.getSignature();
    }

}