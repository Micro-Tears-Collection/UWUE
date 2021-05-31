package code.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

/**
 *
 * @author Roman Lahin
 */
public class Engine {
    
    public static void init() {
        System.setOut(new Log(System.out));
        System.setErr(System.out);
        GLFWErrorCallback.createPrint(System.err).set();

        if(!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        System.out.println("ultra wacky\n"
                + "UHHHHHHHHHHHHHHHHHHHHHHH.....\n"
                + "engine\n\n"
                + "(UWUE 0.0)");
    }
    
    public static int[] getMonitorSize() {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        return new int[]{vidmode.width(), vidmode.height()};
    }

    public static boolean isResolutionValid(int w, int h) {
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
        
        while(modes.hasRemaining()) {
            GLFWVidMode mode = modes.get();
            if(mode.width() == w && mode.height() == h) return true;
        }
        
        return false;
    }

}

class Log extends PrintStream {
    
    FileOutputStream fos;

    public Log(PrintStream ps) {
        super(ps);
        
        try {
            File log = new File("log.txt");
            if(!log.exists()) log.createNewFile();
            fos = new FileOutputStream(log);
        } catch(Exception e) {
            e.printStackTrace(ps);
        }
    }
    
    public void write(byte[] data, int off, int len) {
        super.write(data, off, len);
        try{
            fos.write(data, off, len);
        } catch(Exception e) {}
    }
    
    public void write(byte[] data) {
        try{
            super.write(data);
            fos.write(data);
        } catch(Exception e) {}
    }
    
    public void close() {
        super.close();
        try{
            fos.close();
        } catch(Exception e) {}
    }
}
