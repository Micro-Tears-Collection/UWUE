package code.engine;

import java.nio.IntBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author Roman Lahin
 */
public class Window {
    
	private Callback debugProc;
    long window;
    boolean cursorVisible;
    
    int w, h;

    private Screen scr;
    
    private Window(long window) {
        this.window = window;
        
        final Window win = this;
        
        GLFW.glfwSetKeyCallback(window, (long window2, int key, int scancode, int action, int mods) -> {
            Screen scr = getListener();
            if(scr == null) return;

            if(action == GLFW.GLFW_PRESS) {
                scr.keyPressed(key);
            } else if(action == GLFW.GLFW_RELEASE) {
                scr.keyReleased(key);
            } else if(action == GLFW.GLFW_REPEAT) {
                scr.keyRepeated(key);
            }
        });
        
        GLFW.glfwSetCharCallback(window, (long window2, int codepoint) -> {
            Screen scr = getListener();
            
            if(scr != null) scr.charInput(codepoint);
        });

        GLFW.glfwSetWindowSizeCallback(window, (long window2, int w, int h) -> {
            Screen scr = getListener();
            if(w == 0 || h == 0) return;
            win.w = w;
            win.h = h;

            if(scr != null) scr.sizeChanged(w, h, null);
        });
        
        GLFW.glfwSetMouseButtonCallback(window, (long window2, int button, int action, int mods) -> {
            Screen scr = getListener();
            if(scr == null) return;

            if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE)
                scr.mouseAction(button, action == GLFW.GLFW_PRESS);
        });
        
        GLFW.glfwSetScrollCallback(window, (long window2, double xoffset, double yoffset) -> {
            Screen scr = getListener();
            if(scr != null) scr.mouseScroll(xoffset, yoffset);
        });
    }
    
    public static Window createGLWindow(boolean fullscr, int w, int h, boolean vsync, int aa, boolean debug) {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        int rate = vidmode.refreshRate();
        
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		if(debug) GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, rate);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, aa);
        
        long window = GLFW.glfwCreateWindow(w, h,
                "UWUE", fullscr?GLFW.glfwGetPrimaryMonitor():NULL, NULL);
        
        if(window == NULL) {
            throw new IllegalStateException("Unable to create GLFW Window");
        }
        
        Window win = new Window(window);
        win.w = w; win.h = h;
        
        long prevContext = GLFW.glfwGetCurrentContext();
        win.bind();
		
        GLFW.glfwSwapInterval(vsync?1:0); //vsync on
		if(GLFW.glfwRawMouseMotionSupported()) 
			GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();
		
		if(debug) {
			Configuration.DEBUG.set(true);
			Configuration.DEBUG_FUNCTIONS.set(true);
			Configuration.DEBUG_LOADER.set(true);
			Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
			Configuration.DEBUG_STACK.set(true);

			GL33C.glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
			win.debugProc = GLUtil.setupDebugMessageCallback(System.out);

			ARBDebugOutput.glDebugMessageControlARB(
				GL33C.GL_DONT_CARE, 
				GL33C.GL_DONT_CARE, 
				GL43C.GL_DEBUG_SEVERITY_NOTIFICATION, 
				(IntBuffer) null, 
				false
			);
		}
		
        GLFW.glfwMakeContextCurrent(prevContext);
        
        return win;
    }
    
    public void setListener(Screen scr) {
        this.scr = scr;
    }
    
    private Screen getListener() {
        return scr;
    }
    
    public void destroy() {
		if(debugProc != null) {
			debugProc.free();
			debugProc = null;
		}
		
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }
    
    public void setWindow(boolean fullscr, int w, int h, boolean vsync) {
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        this.w = w; this.h =  h;
        
        if(fullscr) {
            GLFW.glfwSetWindowMonitor(window,
                    GLFW.glfwGetPrimaryMonitor(),
                    0, 0, w, h, vidmode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, MemoryUtil.NULL,
                    (vidmode.width() - w) / 2, (vidmode.height() - h) / 2,
                    w, h, vidmode.refreshRate());
        }

        long prevContext = GLFW.glfwGetCurrentContext();
        bind();
        GLFW.glfwSwapInterval(vsync?1:0); //vsync on
        GLFW.glfwMakeContextCurrent(prevContext);
    }
    
    public void flush() {
        GLFW.glfwSwapBuffers(window);
    }
	
	public void pollEvents() {
		GLFW.glfwPollEvents();
	}
    
    public void bind() {
        GLFW.glfwMakeContextCurrent(window);
    }
    
    public void unbind() {
        GLFW.glfwMakeContextCurrent(NULL);
    }
    
    public void setTitle(String title) {
        if(title != null) GLFW.glfwSetWindowTitle(window, title);
    }

    public void showCursor(boolean show) {
        if(show) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            GLFW.glfwSetCursorPos(window, w >> 1, h >> 1);
        }

        cursorVisible = show;
    }
    
    public final boolean isCursorVisible() {
        return cursorVisible;
    }
    
    public boolean isFullscr() {
        return GLFW.glfwGetWindowMonitor(window) != MemoryUtil.NULL;
    }
    
    public final float getMouseX() {
        double[] xx = new double[1];
        GLFW.glfwGetCursorPos(window, xx, null);
        return (float)xx[0];
    }
    
    public final float getMouseY() {
        double[] yy = new double[1];
        GLFW.glfwGetCursorPos(window, null, yy);
        return (float)yy[0];
    }
    
    public final void setCursorPos(int x, int y) {
        GLFW.glfwSetCursorPos(window, x, y);
    }
    
    public int getWidth() {
        return w;
    }
    
    public int getHeight() {
        return h;
    }
    
    public final boolean isFocused() {
        return GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
    }
    
    public final boolean isShouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }
    
}