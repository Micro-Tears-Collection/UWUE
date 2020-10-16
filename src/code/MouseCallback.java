package code;

import code.game.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

class MouseCallback extends GLFWMouseButtonCallback {

    Main main;

    public MouseCallback(Main main) {
        this.main = main;
    }

    public void invoke(long window, int button, int action, int mods) {
        if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE)
            main.mouseAction(button, action == GLFW.GLFW_PRESS);
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