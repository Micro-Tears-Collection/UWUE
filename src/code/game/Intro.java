package code.game;

import code.audio.SoundSource;
import code.engine.Screen;
import code.ui.TextView;
import code.utils.Keys;

/**
 *
 * @author Roman Lahin
 */
public class Intro extends Screen {
    
    private Main main;
    
	private int textIndex = 0;
    private String[] loadedText;
    private TextView text;
	
	private SoundSource paperFlip;
	
	private Fade fade;
    
    Intro(Main main) {
        this.main = main;
		
		paperFlip = new SoundSource("/sounds/paperflip.ogg");
        paperFlip.set3D(false);
        
        loadedText =  new String[] {"I was travelling across the desert while i saw this town in the distance...",
			"I couldn't resist visiting this desolate place as it suddenly became apparent to me that something very interesting is happening there."};
        
		int w = main.getWidth(), h = main.getHeight();
		
        text = new TextView(w < h ? w : (h * 4 / 3), h, main.font);
        text.setHCenter(true);
        text.setVCenter(true);
		
        setText(true);
		fade = new Fade(true, 0x000000, 500);
    }
    
    private void setText(boolean step) {
		int tmpIndex = textIndex - (step?0:1);
		
		if(tmpIndex == loadedText.length) {
			if(!step) return;
			
			fade = new Fade(false, 0x000000, 500) {
				public void onDone() {
					Game game = new Game(main);
					main.setScreen(game, true);
					game.loadMap(main.gamecfg.get("game", "start_map"));
					game.setFade(new Fade(true, 0x000000, 1000));
				}
			};
			
		} else {
			text.setText(loadedText[tmpIndex], '\n');
			if(step) textIndex++;
		}
    }
    
    public void destroy() {
		paperFlip.destroy();
    }
    
    public void sizeChanged(int w, int h, Screen scr) {
        text.setSize(w < h ? w : (h * 4 / 3), h);
        setText(false);
    }
    
    public void tick() {
        step();
		
		main.e3d.prepare2D(0, 0, main.getWidth(), main.getHeight());
        main.e3d.clearColor(0x000000);
		
        text.draw(
			main.hudRender, 
			(main.getWidth() - text.getWidth()) / 2, 
			0, 
			main.fontColor
		);
		
		if(fade != null) {
            fade.step(main.hudRender, 0, 0, main.getWidth(), main.getHeight());
			
            if(fade.checkDone()) {
                fade.onDone();
                fade = null;
            }
        }
    }

    private void step() {
		if(fade != null) return;
		
        if(Keys.isPressed(Keys.DOWN)) text.scroll(-3);
        if(Keys.isPressed(Keys.UP)) text.scroll(3);
    }
    
    public void keyPressed(int key) {
		if(fade != null) return;
		
        if(Keys.isThatBinding(key, Keys.OK)) {
            paperFlip.play();
            setText(true);
        }
    }
    
    public void mouseAction(int key, boolean pressed) {
		if(fade != null) return;
		
        if(key == Screen.MOUSE_LEFT && !pressed) {
            paperFlip.play();
            setText(true);
        }
    }
    
    public void mouseScroll(double xx, double yy) {
		if(fade != null) return;
		
        text.scroll((int) (yy*main.scrollSpeed()));
    }

}
