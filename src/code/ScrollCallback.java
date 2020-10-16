package code;

import code.game.Main;
import org.lwjgl.glfw.GLFWScrollCallback;

public class ScrollCallback extends GLFWScrollCallback {

    Main main;

    public ScrollCallback(Main main) {
        this.main = main;
    }

    public void invoke(long window, double xoffset, double yoffset) {
        main.mouseScroll(xoffset, yoffset);
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