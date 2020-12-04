package code;

import code.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyInput extends GLFWKeyCallback {

    Main main;

    public KeyInput(Main main) {
        this.main = main;
    }

    public void invoke(long window, int key, int scancode, int action, int mods) {
        if(action == GLFW.GLFW_PRESS) {

            if(key == GLFW.GLFW_KEY_F11) {
                Engine.setWindow(main.conf, !Engine.isFullscr());
            } if(key == GLFW.GLFW_KEY_F2) {
                Engine.takeScreenshot();
            } else {
                main.keyPressed(key);
            }

        } else if(action == GLFW.GLFW_RELEASE) {
            if(key != GLFW.GLFW_KEY_F11) {
                main.keyReleased(key);
            }
        } else if(action == GLFW.GLFW_REPEAT) {
            if(key == GLFW.GLFW_KEY_BACKSPACE) {
                main.keyPressed(key);
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