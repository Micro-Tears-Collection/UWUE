package code.game.world;

import code.audio.SoundSource;
import code.utils.Asset;
import code.engine3d.Mesh;
import code.utils.MeshLoader;
import code.game.Game;
import code.utils.IniFile;
import code.utils.StringTools;
import java.util.Hashtable;

/**
 *
 * @author Roman Lahin
 */
public class WorldLoader {

    public static void loadWorld(Game game, String folder) {
        Asset.destroyThings(Asset.DISPOSABLE);
        Asset.free();
        
        String path = folder;
        if(!folder.toLowerCase().endsWith(".ini")) path += "map.ini";
        
        String[] lines = Asset.loadLines(path);
        IniFile lvl = new IniFile(new Hashtable());
        lvl.set(lines, true);
        
        game.player.pos.set(0,0,0);
        if(lvl.groupExists("PLAYER")) {
            float[] pPos = StringTools.cutOnFloats(lvl.get("PLAYER", "POS"), ',');
            game.player.pos.add(pPos[0], pPos[1], pPos[2]);
        }
        
        Mesh[] skybox = null;
        int skyColor = 0;
        if(lvl.groupExists("SKY")) {
            
            String tmp = lvl.get("SKY","MODEL");
            if(tmp!=null) skybox = MeshLoader.loadObj(tmp);
            
            tmp = lvl.get("SKY","COLOR");
            if(tmp!=null) skyColor = StringTools.getRGB(tmp,',');
            
        }
        
        Mesh[] worldMeshes = null;
        if(lvl.groupExists("WORLD")) {
            worldMeshes = MeshLoader.loadObj(lvl.get("WORLD", "MODEL"), true);
        }
        
        World world = new World(worldMeshes, skyColor, skybox);
        
        if(lvl.groupExists("FOG")) {
            
            String tmp = lvl.get("FOG", "COLOR");
            if(tmp != null) {
                int c = StringTools.getRGB(tmp,',');
                world.fogColor = new float[] {((c>>16)&255) / 255f, 
                    ((c>>8)&255) / 255f, 
                    (c&255) / 255f, 1};
            }
            
            tmp = lvl.get("FOG", "DENSITY");
            if(tmp != null) {
                world.fogDensity = StringTools.parseFloat(tmp);
                world.fogMode = World.EXP;
            }
            
            String near = lvl.get("FOG", "NEAR");
            String far = lvl.get("FOG", "FAR");
            if(near != null || far != null) {
                float nearV = 0; float farV = 2000;
                if(near!=null) nearV = StringTools.parseFloat(near);
                if(far!=null) farV = StringTools.parseFloat(far);
                
                world.fogStart = nearV;
                world.fogEnd = farV;
                world.fogMode = World.LINEAR;
            }
        }
        
        Object[] objGroups = IniFile.createGroups(lines);
        loadObjects((String[])objGroups[0], (IniFile[])objGroups[1], game, world);
        
        game.player.rotX = game.player.rotY = 0;
        
        game.world = world;
        world.objects.add(game.player);
        
        if(lvl.groupExists("MUSIC")) {
            SoundSource player = game.main.musPlayer;
            
            String tmp = lvl.get("MUSIC", "PITCH");
            if(tmp != null) player.setPitch(StringTools.parseFloat(tmp));
            
            boolean playing = player.isPlaying();
            boolean dontChange = lvl.getInt("MUSIC", "DONT_CHANGE", 0) == 1;
            
            tmp = lvl.get("MUSIC", "PATH");
            if(tmp != null && !(playing && (dontChange || tmp.equals(player.soundName)))) {
                player.stop();
                if(player.buffer != null) player.free();
                player.loadFile(tmp);
                player.start();
            }
            if(lvl.getInt("MUSIC", "STOP", 0) == 1) {
                player.stop();
                player.free();
            }
            
            if(lvl.getInt("MUSIC", "REWIND", 0) == 1) player.rewind();
        }
        if(game.main.musPlayer.buffer != null) game.main.musPlayer.buffer.using = true;
        
        Asset.destroyThings(Asset.REUSABLE);
    }
    
    public static void loadObjects(String[] names, IniFile[] objs, Game game, World world) {
        
        for(int i=0; i<names.length; i++) {
            String name = names[i];
            if(!name.startsWith("OBJECT ")) continue;
            
            String objType = name.substring(7);
            IniFile obj = objs[i];
            
            loadObject(game, world, objType, obj);
        }
        
    }

    private static void loadObject(Game game, World world, String objType, IniFile obj) {
        //yeah...
        
        
    }

}
