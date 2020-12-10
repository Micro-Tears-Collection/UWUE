package code.game.world;

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

    public static void loadWorld(Game game, String folder) {
        Asset.destroyThings(Asset.DISPOSABLE);
        Asset.free();
        
        LightGroup.lightgroups.removeAllElements();
        LightGroup.allLights.removeAllElements();
        LightGroup.defaultGroup = new LightGroup("default");
        LightGroup.lightgroups.add(LightGroup.defaultGroup);
        
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
        
        Vector<SoundSourceEntity> soundsToPlay = new Vector();
        Object[] objGroups = IniFile.createGroups(lines);
        loadObjects((String[])objGroups[0], (IniFile[])objGroups[1], game, world, soundsToPlay);
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
                player.play();
            }
            if(lvl.getInt("music", "stop", 0) == 1) {
                player.stop();
                player.free();
            }
            
            if(lvl.getInt("music", "rewind", 0) == 1) player.rewind();
        }
        if(game.main.musPlayer.buffer != null) game.main.musPlayer.buffer.using = true;
        
        for(SoundSourceEntity sound : soundsToPlay) {
            sound.source.play();
        }
        
        Asset.destroyThings(Asset.REUSABLE);
    }
    
    public static void loadObjects(String[] names, IniFile[] objs, Game game, World world, Vector soundsToPlay) {
        Vector lightgroupdata = new Vector();
        boolean defaultWas = false;
        
        for(int i=0; i<names.length; i++) {
            String section = names[i];
            IniFile obj = objs[i];
            
            if(section.startsWith("obj light ") || section.equals("obj light")) {
                float[] pos = null;
                float[] color = StringTools.cutOnFloats(obj.getDef("color", "255,255,255"), ',');
                
                Vector3D dir = new Vector3D();
                dir.setDirection(obj.getFloat("rot_x", 0), obj.getFloat("rot_y", 0));
                float[] spot = new float[]{dir.x, dir.y, dir.z, 1};
                    
                String tmp = obj.getDef("type", "point");
                boolean isSpot = tmp.equals("spot");
                if(tmp.equals("point") || isSpot) {
                    pos = StringTools.cutOnFloats(obj.get("pos"), ',');
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
                
                Light light = new Light(section.length()>10?section.substring(10):"", pos, color, spot);
                
                if(isSpot) {
                    light.cutoff = obj.getFloat("cutoff", light.cutoff);
                }

                LightGroup.allLights.add(light);
            } else if(section.startsWith("obj lightgroup ")) {
                String lname = section.substring(15);
                
                LightGroup group;
                boolean defaultGroup = lname.equals("default");
                defaultWas |= defaultGroup;
                if(defaultGroup) {
                    group = LightGroup.defaultGroup;
                } else {
                    group = new LightGroup(lname);
                    LightGroup.lightgroups.add(group);
                }
                
                group.setAmbient(StringTools.cutOnFloats(obj.getDef("ambient", "0,0,0"), ','));
                
                lightgroupdata.add(group);
                lightgroupdata.add(StringTools.cutOnStrings(obj.getDef("include", defaultGroup?"all":""), ','));
                lightgroupdata.add(StringTools.cutOnStrings(obj.getDef("exclude", ""), ','));
            } else if(section.startsWith("obj ")) {
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

                loadObject(game, world, data[1], name, obj, soundsToPlay);
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
                    if(light.name.equals(lightName)) {
                        group.lights.add(light);
                        break;
                    }
                }
            }
            
            groupLights = (String[]) lightgroupdata.elementAt(i+2);
            
            for(String lightName : groupLights) {
                for(int xx=0; xx<group.lights.size(); xx++) {
                    Light light = group.lights.elementAt(xx);
                    if(light.name.equals(lightName)) {
                        group.lights.removeElementAt(xx);
                        break;
                    }
                }
            }
        }
    }

    private static void loadObject(Game game, World world, String objType, String name, IniFile ini, Vector soundsToPlay) {
        //yeah...
        
        Entity obj = null;
        if(objType.equals("spr")) {
            obj = loadSprite(name, game, world, ini, false);
        } else if(objType.equals("billboard")) {
            obj = loadSprite(name, game, world, ini, true);
        } else if(objType.equals("mesh")) {
            obj = loadMesh(name, game, world, ini);
        } else if(objType.equals("sound")) {
            obj = loadSoundSourceEntity(name, game, world, ini, soundsToPlay);
        } else if(objType.equals("entity")) {
            obj = new Entity();
            loadDefEntity(obj, name, game, world, ini);
        } else if(objType.equals("teleport")) {
            obj = loadTP(name, game, world, ini);
        } else if(objType.equals("box")) {
            obj = loadBox(name, game, world, ini);
        }
        
        if(obj != null) world.objects.add(obj);
        
    }

    private static SoundSourceEntity loadSoundSourceEntity(String name, 
            Game game, World world, IniFile ini, Vector soundsToPlay) {
        SoundSource source = Asset.getSoundSource(ini.get("sound"));
        
        source.setVolume(ini.getFloat("volume", 1));
        source.setPitch(ini.getFloat("pitch", 1));
        source.setLoop(ini.getInt("loop", 1) == 1);
        
        source.set3D(ini.getInt("3d_effects", 1) == 1);
        source.setDistance(ini.getFloat("reference_distance", SoundSource.defRefDist), 
                ini.getFloat("max_distance", SoundSource.defMaxDist));
        
        SoundSourceEntity sound = new SoundSourceEntity(source);
        
        loadDefEntity(sound, name, game, world, ini);
        
        source.setPosition(sound.pos);
        if(ini.getInt("playing_from_start", 1) == 1) {
            if(soundsToPlay != null) soundsToPlay.add(sound);
            else source.play();
        }
        
        return sound;
    }

    private static Teleport loadTP(String name, Game game, World world, IniFile ini) {
        boolean useOffset = false;
        Vector3D pos = new Vector3D();
        
        String tmp;
        if((tmp = ini.get("set")) != null) {
            float[] pos2 = StringTools.cutOnFloats(tmp, ',');
            pos.set(pos2[0], pos2[1], pos2[2]);
        } else if((tmp = ini.get("to")) != null) {
            float[] pos2 = StringTools.cutOnFloats(tmp, ',');
            pos.set(pos2[0], pos2[1], pos2[2]);
            useOffset = true;
        }
        
        Teleport tp = new Teleport(pos, useOffset);
        
        loadDefEntity(tp, name, game, world, ini);
        
        return tp;
    }

    private static BoxEntity loadBox(String name, Game game, World world, IniFile ini) {
        float[] size = StringTools.cutOnFloats(ini.get("size"), ',');
        
        BoxEntity cube = new BoxEntity(size[0], size[1], size[2]);
        
        if(ini.getInt("on_inside", 0) == 1) {
            cube.clickable = false;
            cube.pointable = false;
            cube.activateRadius = 0;
        }
        
        loadDefEntity(cube, name, game, world, ini);
        
        return cube;
    }

    private static MeshObject loadMesh(String name, Game game, World world, IniFile ini) {
        MeshObject mesh = new MeshObject(MeshLoader.loadObj(ini.get("model"), true, null, null));
        
        mesh.meshCollision = ini.getInt("ph_mesh_collision", mesh.meshCollision?1:0) == 1;
        mesh.visible = ini.getInt("visible", mesh.visible?1:0) == 1;
        
        loadPhysEntity(mesh, name, game, world, ini);
        
        return mesh;
    }

    private static SpriteObject loadSprite(String name, Game game, World world, IniFile ini, boolean billboard) {
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
        
        loadDefEntity(spr, name, game, world, ini);
        
        return spr;
    }
    
    private static void loadPhysEntity(PhysEntity obj, String name, Game game, World world, IniFile ini) {
        obj.radius = ini.getFloat("ph_radius", obj.radius);
        obj.height = ini.getFloat("ph_height", obj.height);
        
        obj.physics = ini.getInt("ph_physics", obj.physics?1:0) == 1;
        obj.pushable = ini.getInt("ph_pushable", obj.pushable?1:0) == 1;
        obj.canPush = ini.getInt("ph_can_push", obj.canPush?1:0) == 1;
        
        obj.rotY = ini.getFloat("rot_y", 0);
        obj.hp = ini.getFloat("hp", obj.hp);
        
        loadDefEntity(obj, name, game, world, ini);
    }
    
    private static void loadDefEntity(Entity obj, String name, Game game, World world, IniFile ini) {
        float[] pos = StringTools.cutOnFloats(ini.get("pos"), ',');
        obj.pos.set(pos[0], pos[1], pos[2]);
        
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
