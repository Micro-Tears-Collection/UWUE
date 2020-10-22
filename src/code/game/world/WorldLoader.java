package code.game.world;

import code.audio.SoundSource;
import code.utils.Asset;
import code.engine3d.Mesh;
import code.engine3d.Sprite;
import code.utils.MeshLoader;
import code.game.Game;
import code.game.world.entities.Entity;
import code.game.world.entities.PhysEntity;
import code.game.world.entities.SpriteObject;
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

    private static void loadObject(Game game, World world, String objType, IniFile ini) {
        //yeah...
        
        Entity obj = null;
        if(objType.equals("SPR")) {
            obj = loadSprite(game, world, ini, false);
        } else if(objType.equals("BILLBOARD")) {
            obj = loadSprite(game, world, ini, true);
        }
        
        if(obj != null) world.objects.add(obj);
        
    }

    private static SpriteObject loadSprite(Game game, World world, IniFile ini, boolean billboard) {
        SpriteObject spr = new SpriteObject();
        
        float size = ini.getFloat("SIZE", 100);
        float height = ini.getFloat("HEIGHT", size);
        
        spr.spr = new Sprite(Asset.getMaterial(ini.get("TEX")), size, height);
        spr.spr.billboard = billboard;
        spr.spr.load(new IniFile(StringTools.cutOnStrings(ini.getDef("OPTIONS", ""), ';'), false));
        
        String align = ini.getDef("ALIGN", billboard?"BOTTOM":"CENTER");
        if(align.equals("CENTER")) spr.spr.offsety = -height/2;
        else if(align.equals("TOP")) spr.spr.offsety = -height;
        
        loadDefEntity(spr, game, world, ini);
        
        return spr;
    }
    
    private static void loadPhysEntity(PhysEntity obj, Game game, World world, IniFile ini) {
        obj.radius = ini.getFloat("PHYS_RADIUS", obj.radius);
        obj.height = ini.getFloat("PHYS_HEIGHT", obj.height);
        
        obj.rotY = ini.getFloat("ROT_Y", obj.height);
        obj.hp = ini.getInt("HP", obj.hp);
        
        loadDefEntity(obj, game, world, ini);
    }
    
    private static void loadDefEntity(Entity obj, Game game, World world, IniFile ini) {
        float[] pos = StringTools.cutOnFloats(ini.get("POS"), ',');
        obj.pos.set(pos[0], pos[1], pos[2]);
        
        obj.name = ini.getDef("NAME", obj.name);
        
        //Scripting stuff
        
        obj.activable = ini.getInt("ACTIVABLE", obj.activable?1:0) == 1;
        obj.activateDistance = ini.getFloat("ACTIVATE_RADIUS", obj.activateDistance);
        obj.clickable = ini.getFloat("CLICKABLE", obj.clickable?1:0) == 1;
        obj.pointable = ini.getFloat("POINTABLE", obj.pointable?1:0) == 1;
        
        String tmp = ini.get("ACTIVATE_WHEN");
        if(tmp != null) obj.activateWhen = game.main.loadScript("return "+tmp);
        
        tmp = ini.get("ON_ACTIVATE");
        if(tmp != null) obj.onActivate = game.main.loadScript(tmp);
        
        tmp = ini.get("SCRIPT_ON_ACTIVATE");
        if(tmp != null) obj.onActivate = game.main.loadScriptFromFile(tmp);
        
        tmp = ini.get("ON_FAIL");
        if(tmp != null) obj.onFail = game.main.loadScript(tmp);
        
        tmp = ini.get("SCRIPT_ON_FAIL");
        if(tmp != null) obj.onFail = game.main.loadScriptFromFile(tmp);
    }

}
