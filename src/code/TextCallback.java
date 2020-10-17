package code;

import code.game.Main;
import org.lwjgl.glfw.GLFWCharCallback;

public class TextCallback extends GLFWCharCallback {

    Main main;

    public TextCallback(Main main) {
        this.main = main;
    }

    public void invoke(long window, int codepoint) {
        main.charInput(codepoint);
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