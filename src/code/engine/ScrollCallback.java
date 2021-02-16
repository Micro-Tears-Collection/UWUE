package code.engine;

import org.lwjgl.glfw.GLFWScrollCallback;

public class ScrollCallback extends GLFWScrollCallback {

    Screen scr;

    public void invoke(long window, double xoffset, double yoffset) {
        if(scr != null) scr.mouseScroll(xoffset, yoffset);
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