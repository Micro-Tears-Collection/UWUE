package code;

import code.game.Main;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class ResizeCallback extends GLFWWindowSizeCallback {

    Main main;

    public ResizeCallback(Main main) {
        this.main = main;
    }

    public void invoke(long window, int w, int h) {
        if(w == 0 || h == 0) return;
        Engine.w = w;
        Engine.h = h;
        main.sizeChanged(w, h, null);
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