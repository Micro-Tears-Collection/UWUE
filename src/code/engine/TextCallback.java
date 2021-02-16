package code.engine;

import org.lwjgl.glfw.GLFWCharCallback;

public class TextCallback extends GLFWCharCallback {

    Screen scr;

    public void invoke(long window, int codepoint) {
        if(scr != null) scr.charInput(codepoint);
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