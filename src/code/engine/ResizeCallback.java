package code.engine;

import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class ResizeCallback extends GLFWWindowSizeCallback {

    Screen scr;

    public void invoke(long window, int w, int h) {
        if(w == 0 || h == 0) return;
        Engine.w = w;
        Engine.h = h;
        
        if(scr != null) scr.sizeChanged(w, h, null);
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