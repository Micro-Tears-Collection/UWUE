package code;

import code.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

public class KeyInput extends GLFWKeyCallback {

    Main main;

    public KeyInput(Main main) {
        this.main = main;
    }

    public void invoke(long window, int key, int scancode, int action, int mods) {
        if(action == GLFW.GLFW_PRESS) {

            if(key == GLFW.GLFW_KEY_F11) {
                boolean fullscr = GLFW.glfwGetWindowMonitor(Engine.window) == MemoryUtil.NULL;
                GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

                if(fullscr) {
                    GLFW.glfwSetWindowMonitor(Engine.window,
                            GLFW.glfwGetPrimaryMonitor(),
                            0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
                } else {
                    GLFW.glfwSetWindowMonitor(Engine.window, MemoryUtil.NULL,
                            (vidmode.width() - 800) / 2, (vidmode.height() - 600) / 2,
                            800, 600, vidmode.refreshRate());
                }
            } if(key == GLFW.GLFW_KEY_F2) {
                Engine.takeScreenshot();
            } else {
                main.keyPressed(key);
            }

        } else if(action == GLFW.GLFW_RELEASE) {
            if(key != GLFW.GLFW_KEY_F11) {
                main.keyReleased(key);
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