package code.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Roman Lahin
 */
public class Engine {
    
    static long window;
    static boolean showCursor;
    
    static int w, h;

    private static KeyInput keyInputCallback;
    private static TextCallback textCallback;
    private static MouseCallback mouseCallback;
    private static ScrollCallback scrollCallback;
    private static ResizeCallback resizeCallback;
    
    public static int[] init() {
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
        
        keyInputCallback = new KeyInput();
        textCallback = new TextCallback();
        mouseCallback = new MouseCallback();
        scrollCallback = new ScrollCallback();
        resizeCallback = new ResizeCallback();
        
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        return new int[]{vidmode.width(), vidmode.height()};
    }
    
    public static void setListener(Screen scr) {
        keyInputCallback.scr = scr;
        textCallback.scr = scr;
        mouseCallback.scr = scr;
        scrollCallback.scr = scr;
        resizeCallback.scr = scr;
    }
    
    public static void createGLWindow(boolean fullscr, int w, int h, boolean vsync, int aa) {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        int rate = vidmode.refreshRate();
        
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, rate);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, aa);
        
        Engine.w = w;
        Engine.h = h;
        window = GLFW.glfwCreateWindow(w, h,
                "UWUE", fullscr?GLFW.glfwGetPrimaryMonitor():NULL, NULL);
        
        if(window == NULL) {
            throw new IllegalStateException("Unable to create GLFW Window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(vsync?1:0); //vsync on
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();
        
        GLFW.glfwSetKeyCallback(window, keyInputCallback);
        GLFW.glfwSetCharCallback(window, textCallback);
        GLFW.glfwSetWindowSizeCallback(window, resizeCallback);
        GLFW.glfwSetMouseButtonCallback(window, mouseCallback);
        GLFW.glfwSetScrollCallback(window, scrollCallback);
    }
    
    public static void flush() {
        GLFW.glfwSwapBuffers(Engine.window);
        GLFW.glfwPollEvents();
    }
    
    public static void setWindow(boolean fullscr, int w, int h, boolean vsync) {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        
        if(fullscr) {
            GLFW.glfwSetWindowMonitor(Engine.window,
                    GLFW.glfwGetPrimaryMonitor(),
                    0, 0, w, h, vidmode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(Engine.window, MemoryUtil.NULL,
                    (vidmode.width() - w) / 2, (vidmode.height() - h) / 2,
                    w, h, vidmode.refreshRate());
        }

        GLFW.glfwSwapInterval(vsync ? 1 : 0); //vsync
    }
    
    public static void setTitle(String title) {
        if(title != null) GLFW.glfwSetWindowTitle(window, title);
    }

    public static void showCursor(boolean show) {
        if(show) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
            GLFW.glfwSetCursorPos(window, w >> 1, h >> 1);
        }

        showCursor = show;
    }
    
    public static boolean isFullscr() {
        return GLFW.glfwGetWindowMonitor(Engine.window) != MemoryUtil.NULL;
    }
    
    public static void destroy() {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public static boolean isResolutionValid(int w, int h) {
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor());
        
        while(modes.hasRemaining()) {
            GLFWVidMode mode = modes.get();
            if(mode.width() == w && mode.height() == h) return true;
        }
        
        return false;
    }
    
    public static int getMaxAA() {
        int samples = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
        
        if(GL11.glGetError() != 0) {
            System.out.println("Can't get GL_MAX_SAMPLES, looks like your GPU doesnt support openGL 3");
            return 4;
        }
        
        return samples;
    }
    
    public static void takeScreenshot() {
        GL11.glReadBuffer(GL11.GL_FRONT);
        ByteBuffer buffer = MemoryUtil.memAlloc(w * h * 4);
        GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        int error = GL11.glGetError();
        if(error != 0) System.out.println("takeScreenshot GL error: "+error);
        
        try {
            File file = new File("screenshots/");
            if(!file.exists()) file.mkdir();
            
            Calendar cal = Calendar.getInstance();
            String data = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH)+1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + "." 
                    + cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);
            file = new File("screenshots/"+data+".png");
            
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    
            for(int x = 0; x < w; x++) {
                for(int y = 0; y < h; y++) {
                    int i = (x + (w * y)) * 4;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, h - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
            MemoryUtil.memFree(buffer);

            ImageIO.write(image, "PNG", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
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