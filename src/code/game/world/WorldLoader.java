package code.game.world;

import code.audio.SoundSource;
import code.utils.Asset;
import code.engine3d.Mesh;
import code.engine3d.Sprite;
import code.utils.MeshLoader;
import code.game.Game;
import code.game.world.entities.Entity;
import code.game.world.entities.MeshObject;
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
        
        game.player.pos.set(0, 0, 0);
        game.player.rotX = 0;
        game.player.rotY = 0;
        if(lvl.groupExists("player")) {
            String tmp = lvl.get("player", "pos");
            if(tmp != null) {
                float[] pPos = StringTools.cutOnFloats(tmp, ',');
                game.player.pos.set(pPos[0], pPos[1], pPos[2]);
            }
            
            game.player.rotY = lvl.getFloat("player", "rot_y", 0);
        }
        
        Mesh[] skybox = null;
        int skyColor = 0;
        if(lvl.groupExists("sky")) {
            
            String tmp = lvl.get("sky", "model");
            if(tmp!=null) skybox = MeshLoader.loadObj(tmp);
            
            tmp = lvl.get("sky", "color");
            if(tmp!=null) skyColor = StringTools.getRGB(tmp,',');
            
        }
        
        Mesh[] worldMeshes = null;
        if(lvl.groupExists("world")) {
            worldMeshes = MeshLoader.loadObj(lvl.get("world", "model"), true);
        }
        
        World world = new World(worldMeshes, skyColor, skybox);
        
        if(lvl.groupExists("fog")) {
            
            String tmp = lvl.get("fog", "color");
            if(tmp != null) {
                int c = StringTools.getRGB(tmp,',');
                world.fogColor = new float[] {((c>>16)&255) / 255f, 
                    ((c>>8)&255) / 255f, 
                    (c&255) / 255f, 1};
            }
            
            tmp = lvl.get("fog", "density");
            if(tmp != null) {
                world.fogDensity = StringTools.parseFloat(tmp);
                world.fogMode = World.EXP;
            }
            
            String near = lvl.get("fog", "near");
            String far = lvl.get("fog", "far");
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
        
        game.world = world;
        world.objects.add(game.player);
        
        if(lvl.groupExists("music")) {
            SoundSource player = game.main.musPlayer;
            
            String tmp = lvl.get("music", "pitch");
            if(tmp != null) player.setPitch(StringTools.parseFloat(tmp));
            
            boolean playing = player.isPlaying();
            boolean dontChange = lvl.getInt("music", "dont_change", 0) == 1;
            
            tmp = lvl.get("music", "path");
            if(tmp != null && !(playing && (dontChange || tmp.equals(player.soundName)))) {
                player.stop();
                if(player.buffer != null) player.free();
                player.loadFile(tmp);
                player.start();
            }
            if(lvl.getInt("music", "stop", 0) == 1) {
                player.stop();
                player.free();
            }
            
            if(lvl.getInt("music", "rewind", 0) == 1) player.rewind();
        }
        if(game.main.musPlayer.buffer != null) game.main.musPlayer.buffer.using = true;
        
        Asset.destroyThings(Asset.REUSABLE);
    }
    
    public static void loadObjects(String[] names, IniFile[] objs, Game game, World world) {
        
        for(int i=0; i<names.length; i++) {
            String name = names[i];
            if(!name.startsWith("obj ")) continue;
            
            String objType = name.substring(4);
            IniFile obj = objs[i];
            
            loadObject(game, world, objType, obj);
        }
        
    }

    private static void loadObject(Game game, World world, String objType, IniFile ini) {
        //yeah...
        
        Entity obj = null;
        if(objType.equals("spr")) {
            obj = loadSprite(game, world, ini, false);
        } else if(objType.equals("billboard")) {
            obj = loadSprite(game, world, ini, true);
        } else if(objType.equals("mesh")) {
            obj = loadMesh(game, world, ini);
        }
        
        if(obj != null) world.objects.add(obj);
        
    }

    private static MeshObject loadMesh(Game game, World world, IniFile ini) {
        MeshObject mesh = new MeshObject(MeshLoader.loadObj(ini.get("model"), true));
        
        loadPhysEntity(mesh, game, world, ini);
        
        return mesh;
    }

    private static SpriteObject loadSprite(Game game, World world, IniFile ini, boolean billboard) {
        SpriteObject spr = new SpriteObject();
        
        float size = ini.getFloat("size", 100);
        float height = ini.getFloat("height", size);
        
        String tmp = ini.getDef("align", billboard?"bottom":"center");
        int align = Sprite.BOTTOM;
        if(tmp.equals("center")) align = Sprite.CENTER;
        else if(tmp.equals("top")) align = Sprite.TOP;
        
        spr.spr = new Sprite(Asset.getMaterial(ini.get("tex")), billboard, size, height, align);
        spr.spr.load(new IniFile(StringTools.cutOnStrings(ini.getDef("options", ""), ';'), false));
        
        loadDefEntity(spr, game, world, ini);
        
        return spr;
    }
    
    private static void loadPhysEntity(PhysEntity obj, Game game, World world, IniFile ini) {
        obj.radius = ini.getFloat("phys_radius", obj.radius);
        obj.height = ini.getFloat("phys_height", obj.height);
        
        obj.rotY = ini.getFloat("rot_y", obj.height);
        obj.hp = ini.getInt("hp", obj.hp);
        
        loadDefEntity(obj, game, world, ini);
    }
    
    private static void loadDefEntity(Entity obj, Game game, World world, IniFile ini) {
        float[] pos = StringTools.cutOnFloats(ini.get("pos"), ',');
        obj.pos.set(pos[0], pos[1], pos[2]);
        
        obj.name = ini.getDef("name", obj.name);
        
        //Scripting stuff
        
        obj.activable = ini.getInt("activable", obj.activable?1:0) == 1;
        obj.activateDistance = ini.getFloat("activate_radius", obj.activateDistance);
        obj.clickable = ini.getFloat("clickable", obj.clickable?1:0) == 1;
        obj.pointable = ini.getFloat("pointable", obj.pointable?1:0) == 1;
        
        String tmp = ini.get("activate_if");
        if(tmp != null) obj.activateWhen = game.main.loadScript("return "+tmp);
        
        tmp = ini.get("on_activate");
        if(tmp != null) obj.onActivate = game.main.loadScript(tmp);
        
        tmp = ini.get("script_on_activate");
        if(tmp != null) obj.onActivate = game.main.loadScriptFromFile(tmp);
        
        tmp = ini.get("on_fail");
        if(tmp != null) obj.onFail = game.main.loadScript(tmp);
        
        tmp = ini.get("script_on_fail");
        if(tmp != null) obj.onFail = game.main.loadScriptFromFile(tmp);
    }

}
