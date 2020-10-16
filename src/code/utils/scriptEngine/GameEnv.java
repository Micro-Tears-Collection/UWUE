package code.utils.scriptEngine;

import code.game.Game;

/**
 *
 * @author Roman Lahin
 */
public class GameEnv extends EnvironmentBase {
    
    Game game;
    
    public GameEnv(Game game) {
        super();
        this.game = game;
    }

    int[] getsetEnvValue(String name, int value, boolean set) {
        return null;
    }

    String getsetEnvString(String name, String value, boolean set) {
        return null;
    }

    boolean functionEnvVoid(String name, String[] args) {
        
        /*if(name.equals("playMusic")) {
            game.playMusic(readString(args[0]), 0, args.length==1?false:readBoolean(args[1]) );
            return true;
        } if(name.equals("stopMusic")) {
            Main.musicPlayer.destroy();
            return true;
        } else if(name.equals("loadScene")) {
            game.loadScene(readString(args[0]));
            return true;
        } else if(name.equals("showDialog")) {
            game.showDialog(readString(args[0]));
            return true;
        } else if(name.equals("setBackground")) {
            game.setBackground(readString(args[0]));
            return true;
        } else if(name.equals("setBackgroundColor")) {
            game.background.backgroundColor = (readValue(args[0])<<16)|(readValue(args[1])<<8)|(readValue(args[2]));
            return true;
        }*/
        
        return false;
    }

    int[] functionEnvValue(String name, String[] args) {
        return null;
    }

    String functionEnvString(String name, String[] args) {
        return null;
    }

}
