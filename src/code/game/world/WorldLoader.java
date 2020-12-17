package code.game.world;

import code.audio.AudioEngine;
import code.audio.SoundSource;
import code.engine3d.Light;
import code.engine3d.LightGroup;
import code.engine3d.Material;
import code.utils.Asset;
import code.engine3d.Mesh;
import code.engine3d.Sprite;
import code.utils.MeshLoader;
import code.game.Game;
import code.game.world.entities.BoxEntity;
import code.game.world.entities.Entity;
import code.game.world.entities.MeshObject;
import code.game.world.entities.PhysEntity;
import code.game.world.entities.SoundSourceEntity;
import code.game.world.entities.SpriteObject;
import code.game.world.entities.Teleport;
import code.math.Vector3D;
import code.utils.IniFile;
import code.utils.StringTools;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Roman Lahin
 */
public class WorldLoader {

    public static void loadWorld(Game game, String folder, 
            Vector3D newPlayerPos, float nextRotX, float nextRotY) {
        Asset.destroyThings(Asset.DISPOSABLE);
        Asset.free();
        AudioEngine.suspend();
        
        LightGroup.clear(true);
        
        String path = folder;
        if(!folder.toLowerCase().endsWith(".ini")) {
            if(folder.endsWith("/")) path += "map.ini";
            else path = "/maps/"+path+"/map.ini";
        }
        
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
        
        if(newPlayerPos != null) game.player.pos.set(newPlayerPos);
        if(nextRotY != Float.MAX_VALUE) {
            game.player.rotX = 0;
            game.player.rotY = nextRotY;
        }
        if(nextRotX != Float.MAX_VALUE) game.player.rotX = nextRotX;
        World.updateListener(game.player);
        
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
            String prefix = null, postfix = null;
            if(lvl.getInt("world", "trenchbroom", 0) == 1) {
                prefix = "/textures/";
                postfix = ".png";
            }
            worldMeshes = MeshLoader.loadObj(lvl.get("world", "model"), true, prefix, postfix);
        }
        
        World world = new World(worldMeshes, skyColor, skybox);
        
        world.fallDeath = lvl.getInt("world", "fall_death", world.fallDeath?1:0) == 1;
        
        if(lvl.groupExists("fog")) {
            
            String tmp = lvl.get("fog", "color");
            if(tmp != null) {
                int c = StringTools.getRGB(tmp,',');
                world.fogColor = new float[] {(float)((c>>16)&255) / 255f, 
                    (float)((c>>8)&255) / 255f, 
                    (float)(c&255) / 255f, 1};
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
        
        Vector<Integer> sourcesToPlay = new Vector();
        Object[] objGroups = IniFile.createGroups(lines);
        loadObjects((String[])objGroups[0], (IniFile[])objGroups[1], game, world, sourcesToPlay);
        if(LightGroup.allLights.isEmpty()) {
            LightGroup.defaultGroup = null;
            LightGroup.lightgroups.removeAllElements();
        }
        
        game.world = world;
        world.objects.add(game.player);
        
        if(lvl.groupExists("music")) {
            SoundSource player = game.main.musPlayer;
            
            boolean pitchWasSet = false;
            String tmp = lvl.get("music", "pitch");
            if(tmp != null) {
                player.setPitch(StringTools.parseFloat(tmp));
                pitchWasSet = true;
            }
            
            boolean playing = player.isPlaying();
            boolean dontChange = lvl.getInt("music", "dont_change", 0) == 1;
            
            tmp = lvl.get("music", "path");
            if(tmp != null && !(playing && (dontChange || tmp.equals(player.soundName)))) {
                player.stop();
                if(player.buffer != null) player.free();
                if(!pitchWasSet) player.setPitch(1);
                player.loadFile(tmp);
                sourcesToPlay.add(player.getID());
            }
            if(lvl.getInt("music", "stop", 0) == 1) {
                player.stop();
                player.free();
            }
            
            if(lvl.getInt("music", "rewind", 0) == 1) player.rewind();
        }
        if(game.main.musPlayer.buffer != null) game.main.musPlayer.buffer.using = true;
        
        Asset.destroyThings(Asset.REUSABLE);
        AudioEngine.process();
        
        if(!sourcesToPlay.isEmpty()) {
            int[] sources = new int[sourcesToPlay.size()];
            
            for(int i=0; i<sources.length; i++) {
                sources[i] = sourcesToPlay.elementAt(i);
            }
        
            AudioEngine.playMultiple(sources);
        }
    }
    
    public static void loadObjects(String[] names, IniFile[] objs, Game game, World world, Vector<Integer> sourcesToPlay) {
        Vector lightgroupdata = new Vector();
        boolean defaultWas = false;
        
        for(int i=0; i<names.length; i++) {
            String section = names[i];
            IniFile obj = objs[i];
            
            if(section.startsWith("obj ")) {
                String[] data = StringTools.cutOnStrings(section, ' ');
                
                String name = null;
                if(data.length >= 3) {
                    StringBuffer sb = new StringBuffer();
                    for(int x=2; x<data.length; x++) {
                        sb.append(data[x]);
                        if(x != data.length - 1) sb.append(' ');
                    }
                    
                    name = sb.toString();
                }
                
                String tmp = obj.get("pos");
                String[] poses = tmp==null?null:StringTools.cutOnStrings(tmp, ';');
                
                for(int x=0; x<(poses==null?1:poses.length); x++) {
                    float[] pos = null;
                    String thisName = name;
                    if(poses != null) {
                        pos = StringTools.cutOnFloats(poses[x], ',');
                        if(poses.length > 1 && thisName != null) thisName += + '_' + (x+1);
                    }
                    
                    defaultWas |= data[1].equals("lightgroup") && "default".equals(name);
                    loadObject(game, world, data[1], thisName, obj, pos, lightgroupdata, sourcesToPlay);
                }
            }
        }
        
        if(!defaultWas) {
            lightgroupdata.add(LightGroup.defaultGroup);
            lightgroupdata.add(new String[]{"all"});
            lightgroupdata.add(new String[0]);
        }

        for(int i=0; i<lightgroupdata.size(); i+=3) {
            LightGroup group = (LightGroup) lightgroupdata.elementAt(i);

            String[] groupLights = (String[]) lightgroupdata.elementAt(i+1);
            boolean all = groupLights.length==1?groupLights[0].equals("all"):false;
            
            if(all) group.lights = (Vector<Light>)LightGroup.allLights.clone();
            else for(String lightName : groupLights) {
                for(Light light : LightGroup.allLights) {
                    if(lightName.equals(light.name)) {
                        group.lights.add(light);
                        break;
                    }
                }
            }
            
            groupLights = (String[]) lightgroupdata.elementAt(i+2);
            
            for(String lightName : groupLights) {
                for(int xx=0; xx<group.lights.size(); xx++) {
                    Light light = group.lights.elementAt(xx);
                    if(lightName.equals(light.name)) {
                        group.lights.removeElementAt(xx);
                        break;
                    }
                }
            }
        }
    }

    private static void loadObject(Game game, World world, String objType, String name, IniFile ini, 
            float[] pos, Vector lightgroupdata, Vector<Integer> sourcesToPlay) {
        //yeah...
        
        Entity obj = null;
        if(objType.equals("spr")) {
            obj = loadSprite(name, pos, game, world, ini, false);
        } else if(objType.equals("billboard")) {
            obj = loadSprite(name, pos, game, world, ini, true);
        } else if(objType.equals("mesh")) {
            obj = loadMesh(name, pos, game, world, ini);
        } else if(objType.equals("sound")) {
            obj = loadSoundSourceEntity(name, pos, game, world, ini, sourcesToPlay);
        } else if(objType.equals("entity")) {
            obj = new Entity();
            loadDefEntity(obj, pos, name, game, world, ini);
        } else if(objType.equals("teleport")) {
            obj = loadTP(name, pos, game, world, ini);
        } else if(objType.equals("box")) {
            obj = loadBox(name, pos, game, world, ini);
        } else if(objType.equals("light")) {
            
            float[] color = StringTools.cutOnFloats(ini.getDef("color", "255,255,255"), ',');

            Vector3D dir = new Vector3D();
            dir.setDirection(ini.getFloat("rot_x", 0), ini.getFloat("rot_y", 0));
            
            float[] spot = new float[]{dir.x, dir.y, dir.z, 1};

            String tmp = ini.getDef("type", "point");
            boolean isSpot = tmp.equals("spot");
            
            if(tmp.equals("point") || isSpot) {
                pos = new float[]{pos[0], pos[1], pos[2], 1};

                if(!isSpot) spot = null;
            } else if(tmp.equals("dir")) {
                pos = spot;
                pos[0] = -spot[0];
                pos[1] = -spot[1];
                pos[2] = -spot[2];
                pos[3] = 0;
                spot = null;
            }

            Light light = new Light(name, pos, color, spot);

            if(isSpot) light.cutoff = ini.getFloat("cutoff", light.cutoff);

            LightGroup.allLights.add(light);
        } else if(objType.equals("lightgroup")) {
            LightGroup group;
            boolean defaultGroup = name.equals("default");
            if(defaultGroup) {
                group = LightGroup.defaultGroup;
            } else {
                group = new LightGroup(name);
                LightGroup.lightgroups.add(group);
            }

            group.setAmbient(StringTools.cutOnFloats(ini.getDef("ambient", "0,0,0"), ','));

            lightgroupdata.add(group);
            lightgroupdata.add(StringTools.cutOnStrings(ini.getDef("include", defaultGroup ? "all" : ""), ','));
            lightgroupdata.add(StringTools.cutOnStrings(ini.getDef("exclude", ""), ','));
        }
        
        if(obj != null) world.objects.add(obj);
        
    }

    private static SoundSourceEntity loadSoundSourceEntity(String name, float[] pos,
            Game game, World world, IniFile ini, Vector<Integer> sourcesToPlay) {
        SoundSource source = Asset.getSoundSource(ini.get("sound"));
        
        source.setVolume(ini.getFloat("volume", 1));
        source.setPitch(ini.getFloat("pitch", 1));
        source.setLoop(ini.getInt("loop", 1) == 1);
        
        source.set3D(ini.getInt("3d_effects", 1) == 1);
        source.setDistance(ini.getFloat("reference_distance", SoundSource.defRefDist), 
                ini.getFloat("max_distance", SoundSource.defMaxDist));
        
        SoundSourceEntity sound = new SoundSourceEntity(source);
        
        loadDefEntity(sound, pos, name, game, world, ini);
        
        source.setPosition(sound.pos);
        if(ini.getInt("playing_from_start", 1) == 1) {
            if(sourcesToPlay == null) source.play();
            else sourcesToPlay.add(source.getID());
        }
        
        return sound;
    }

    private static Teleport loadTP(String name, float[] pos, Game game, World world, IniFile ini) {
        boolean useOffset = false;
        Vector3D tpto = new Vector3D();
        
        String tmp;
        if((tmp = ini.get("set")) != null) {
            float[] pos2 = StringTools.cutOnFloats(tmp, ',');
            tpto.set(pos2[0], pos2[1], pos2[2]);
        } else if((tmp = ini.get("to")) != null) {
            float[] pos2 = StringTools.cutOnFloats(tmp, ',');
            tpto.set(pos2[0], pos2[1], pos2[2]);
            useOffset = true;
        }
        
        Teleport tp = new Teleport(tpto, useOffset);
        
        loadDefEntity(tp, pos, name, game, world, ini);
        
        return tp;
    }

    private static BoxEntity loadBox(String name, float[] pos, Game game, World world, IniFile ini) {
        float[] size = StringTools.cutOnFloats(ini.get("size"), ',');
        
        BoxEntity cube = new BoxEntity(size[0], size[1], size[2]);
        
        if(ini.getInt("on_inside", 0) == 1) {
            cube.clickable = false;
            cube.pointable = false;
            cube.activateRadius = 0;
        }
        
        loadDefEntity(cube, pos, name, game, world, ini);
        
        return cube;
    }

    private static MeshObject loadMesh(String name, float[] pos, Game game, World world, IniFile ini) {
        MeshObject mesh = new MeshObject(MeshLoader.loadObj(ini.get("model"), true, null, null));
        
        mesh.meshCollision = ini.getInt("ph_mesh_collision", mesh.meshCollision?1:0) == 1;
        mesh.visible = ini.getInt("visible", mesh.visible?1:0) == 1;
        
        loadPhysEntity(mesh, pos, name, game, world, ini);
        
        return mesh;
    }

    private static SpriteObject loadSprite(String name, float[] pos, Game game, World world, IniFile ini, boolean billboard) {
        SpriteObject spr = new SpriteObject();
        
        Material mat = Asset.getMaterial(ini.get("tex"));
        float w = 100, h = 100;
        
        float ww = ini.getFloat("width", Float.MAX_VALUE);
        float hh = ini.getFloat("height", Float.MAX_VALUE);
        
        if(ww != Float.MAX_VALUE && hh == Float.MAX_VALUE) {
            w = ww;
            h = ww * mat.tex.h / mat.tex.w;
        } else if(ww == Float.MAX_VALUE && hh != Float.MAX_VALUE) {
            w = hh * mat.tex.w / mat.tex.h;
            h = hh;
        } else if(ww != Float.MAX_VALUE && hh != Float.MAX_VALUE) {
            w = ww; h = hh;
        }
        
        String tmp = ini.getDef("align", billboard?"bottom":"center");
        int align = Sprite.BOTTOM;
        if(tmp.equals("center")) align = Sprite.CENTER;
        else if(tmp.equals("top")) align = Sprite.TOP;
        
        spr.spr = new Sprite(mat, billboard, w, h, align);
        spr.spr.load(new IniFile(StringTools.cutOnStrings(ini.getDef("options", ""), ';'), false));
        
        spr.visible = ini.getInt("visible", spr.visible?1:0) == 1;
        
        loadDefEntity(spr, pos, name, game, world, ini);
        
        return spr;
    }
    
    private static void loadPhysEntity(PhysEntity obj, float[] pos, String name, Game game, World world, IniFile ini) {
        obj.radius = ini.getFloat("ph_radius", obj.radius);
        obj.height = ini.getFloat("ph_height", obj.height);
        
        obj.physics = ini.getInt("ph_physics", obj.physics?1:0) == 1;
        obj.pushable = ini.getInt("ph_pushable", obj.pushable?1:0) == 1;
        obj.canPush = ini.getInt("ph_can_push", obj.canPush?1:0) == 1;
        
        obj.rotY = ini.getFloat("rot_y", 0);
        obj.hp = ini.getFloat("hp", obj.hp);
        
        loadDefEntity(obj, pos, name, game, world, ini);
    }
    
    private static void loadDefEntity(Entity obj, float[] pos, String name, Game game, World world, IniFile ini) {
        if(pos != null) obj.pos.set(pos[0], pos[1], pos[2]);
        
        obj.name = name!=null?name:obj.name;
        
        //Scripting stuff
        
        obj.activable = ini.getInt("activable", obj.activable?1:0) == 1;
        obj.activateRadius = ini.getFloat("activate_radius", obj.activateRadius);
        obj.clickable = ini.getFloat("clickable", obj.clickable?1:0) == 1;
        obj.pointable = ini.getFloat("pointable", obj.pointable?1:0) == 1;
        
        obj.animateWhenPaused = ini.getFloat("animate_when_paused", obj.animateWhenPaused?1:0) == 1;
        
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
