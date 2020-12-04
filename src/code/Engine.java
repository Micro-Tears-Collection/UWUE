package code;

import code.game.Main;
import code.audio.AudioEngine;
import code.game.Configuration;
import code.game.world.entities.Player;
import code.utils.Keys;
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
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Roman Lahin
 */
public class Engine {
    
    public static Main main;
    public static long window;
    public static boolean hideCursor;
    
    public static int w, h;

    public static KeyInput keyInputCallback;
    public static TextCallback textCallback;
    public static MouseCallback mouseCallback;
    public static ScrollCallback scrollCallback;
    public static ResizeCallback resizeCallback;
    
    public static void main(String[] args) {
        System.setOut(new Log(System.out));
        System.setErr(System.out);
        GLFWErrorCallback.createPrint(System.err).set();

        if(!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        
        System.out.println("ultra wacky\n"
                + "UHHHHHHHHHHHHHHHHHHHHHHH.....\n"
                + "engine\n\n"
                + "(UWUE 0.0)");
        
        Keys.UP = Keys.addKeyToBinding(Keys.UP, GLFW.GLFW_KEY_UP);
        Keys.DOWN = Keys.addKeyToBinding(Keys.DOWN, GLFW.GLFW_KEY_DOWN);
        Keys.LEFT = Keys.addKeyToBinding(Keys.LEFT, GLFW.GLFW_KEY_LEFT);
        Keys.RIGHT = Keys.addKeyToBinding(Keys.RIGHT, GLFW.GLFW_KEY_RIGHT);
        Keys.OK = Keys.addKeyToBinding(Keys.OK, GLFW.GLFW_KEY_ENTER);
        Keys.ESC = Keys.addKeyToBinding(Keys.ESC, GLFW.GLFW_KEY_ESCAPE);
        
        Player.initKeys(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_SPACE);
        Main.TILDE = Keys.addKeyToBinding(Main.TILDE, GLFW.GLFW_KEY_GRAVE_ACCENT);
        Main.ERASE = Keys.addKeyToBinding(Main.ERASE, GLFW.GLFW_KEY_BACKSPACE);
        
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        main = new Main(vidmode.width(), vidmode.height());
        
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, main.conf.aa);
        
        w = main.conf.startInFullscr? main.conf.fw:main.conf.ww;
        h = main.conf.startInFullscr? main.conf.fh:main.conf.wh;
        
        keyInputCallback = new KeyInput(main);
        textCallback = new TextCallback(main);
        mouseCallback = new MouseCallback(main);
        scrollCallback = new ScrollCallback(main);
        resizeCallback = new ResizeCallback(main);
        
        initGL(main.conf.startInFullscr);
        AudioEngine.init();
        
        main.init();
    }
    
    static void initGL(boolean fullscr) {
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        
        window = GLFW.glfwCreateWindow(w, h,
                "UWUE", fullscr?GLFW.glfwGetPrimaryMonitor():NULL, NULL);
        
        if(window == NULL) {
            throw new IllegalStateException("Unable to create GLFW Window");
        }

        GLFW.glfwMakeContextCurrent(window);
        /*
        I have weird issues with vsync in windowed mode. 
        For some reason fps drops to 30 frames, so i disabled vsync
        */
        GLFW.glfwSwapInterval(0); //vsync
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();
        
        GLFW.glfwSetKeyCallback(window, keyInputCallback);
        GLFW.glfwSetCharCallback(window, textCallback);
        GLFW.glfwSetWindowSizeCallback(window, resizeCallback);
        GLFW.glfwSetMouseButtonCallback(window, mouseCallback);
        GLFW.glfwSetScrollCallback(window, scrollCallback);
    }
    
    public static boolean setWindow(Configuration conf, boolean fullscr) {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, conf.aa);

        int ww, hh;
        
        if(fullscr) {
            ww = conf.fw; hh = conf.fh;
            GLFW.glfwSetWindowMonitor(Engine.window,
                    GLFW.glfwGetPrimaryMonitor(),
                    0, 0, conf.fw, conf.fh, vidmode.refreshRate());
        } else {
            ww = conf.ww; hh = conf.wh;
            GLFW.glfwSetWindowMonitor(Engine.window, MemoryUtil.NULL,
                    (vidmode.width() - conf.ww) / 2, (vidmode.height() - conf.wh) / 2,
                    conf.ww, conf.wh, vidmode.refreshRate());
        }
        
        return w == ww && h == hh;
    }
    
    public static void setTitle(String title) {
        if(title != null) GLFW.glfwSetWindowTitle(window, title);
    }

    public static void hideCursor(boolean hide) {
        if(!hide) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
            GLFW.glfwSetCursorPos(window, w >> 1, h >> 1);
        }

        hideCursor = hide;
    }
    
    public static boolean isFullscr() {
        return GLFW.glfwGetWindowMonitor(Engine.window) != MemoryUtil.NULL;
    }
    
    public static void destroy() {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();

        AudioEngine.close();
    }
    
    public static void takeScreenshot() {
        GL11.glReadBuffer(GL11.GL_FRONT);
        int bpp = 4; // rgba
        ByteBuffer buffer = MemoryUtil.memAlloc(w * h * bpp);
        GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        
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
                    int i = (x + (w * y)) * bpp;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, h - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
            MemoryUtil.memFree(buffer);

            ImageIO.write(image, "PNG", file);
        } catch (Exception e) {
            printError(e);
        }
    }
    
    public static void printError(Throwable t) {
        t.printStackTrace(System.out);
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