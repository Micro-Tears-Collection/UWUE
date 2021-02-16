package code.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

class MouseCallback extends GLFWMouseButtonCallback {

    Screen scr;

    public void invoke(long window, int button, int action, int mods) {
        if(scr == null) return;
        
        if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE)
            scr.mouseAction(button, action == GLFW.GLFW_PRESS);
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