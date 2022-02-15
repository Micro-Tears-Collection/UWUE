package code.game.world;

import code.audio.AudioEngine;
import code.audio.SoundSource;

import code.engine3d.game.lighting.Light;
import code.engine3d.game.lighting.LightGroup;
import code.engine3d.game.WorldMaterial;
import code.engine3d.instancing.Sprite;
import code.engine3d.instancing.MeshInstance;
import code.engine3d.Material;
import code.game.Configuration;

import code.utils.assetManager.AssetManager;

import code.game.Game;
import code.game.scripting.Scripting;
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
import java.util.ArrayList;

/**
 *
 * @author Roman Lahin
 */
public class WorldLoader {

    public static void loadWorld(Game game, String map, 
            Vector3D newPlayerPos, float nextRotX, float nextRotY) {
        if(game.world != null) game.world.destroy();
        AudioEngine.suspend();
        
        LightGroup.clear(true);
        
        String[] lines = AssetManager.loadLines("/maps/"+map+"/map.ini");
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
            
            game.player.rotX = lvl.getFloat("player", "rot_x", 0);
            game.player.rotY = lvl.getFloat("player", "rot_y", 0);
        }
        
        if(newPlayerPos != null) game.player.pos.set(newPlayerPos);
        if(nextRotY != Game.DONT_ROTATE) {
            game.player.rotX = 0;
            game.player.rotY = nextRotY;
        }
        if(nextRotX != Game.DONT_ROTATE) game.player.rotX = nextRotX;
		
		Vector3D camPos = new Vector3D(game.player.pos);
		camPos.y += game.player.eyeHeight;
        World.updateListener(camPos, null, game.player.rotY);
        
        MeshInstance[] skybox = null;
        int skyColor = 0;
        if(lvl.groupExists("sky")) {
            
            String tmp = lvl.get("sky", "model");
            if(tmp!=null) {
                skybox = game.e3d.getMeshInstances(tmp, null);
            }
            
            tmp = lvl.get("sky", "color");
            if(tmp!=null) skyColor = StringTools.getRGB(tmp,',');
            
        }
        
        MeshInstance[] worldMeshes = null;
        if(lvl.groupExists("world")) {
            worldMeshes = game.e3d.getMeshInstances(lvl.get("world", "model"), null);
        }
        
        World world = new World(game.e3d, worldMeshes, skyColor, skybox, game.main.conf.debug);
        
		world.drawDistance = lvl.getFloat("world", "draw_distance", world.drawDistance);
        world.fallDeath = lvl.getInt("world", "fall_death", world.fallDeath?1:0) == 1;
        
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
        
        ArrayList<Integer> sourcesToPlay = new ArrayList<>();
        Object[] objGroups = IniFile.createGroups(lines);
        loadObjects((String[])objGroups[0], (IniFile[])objGroups[1], game, world, sourcesToPlay);
        if(LightGroup.allLights.isEmpty()) {
            LightGroup.defaultGroup = null;
            LightGroup.lightgroups.clear();
        }
        
        game.world = world;
        world.addEntity(game.player);
        
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
        
        AssetManager.destroyThings(AssetManager.CONTENT);
        AudioEngine.process();
        
        if(!sourcesToPlay.isEmpty()) {
            int[] sources = new int[sourcesToPlay.size()];
            
            for(int i=0; i<sources.length; i++) {
                sources[i] = sourcesToPlay.get(i);
            }
        
            AudioEngine.playMultiple(sources);
        }
    }
    
    static void loadObjects(
			String[] sections, 
			IniFile[] objsInis, Game game, World world,
			ArrayList<Integer> sourcesToPlay
	) {
        ArrayList lightgroupdata = new ArrayList<>();
        boolean defaultWas = false;
        
        for(int i=0; i<sections.length; i++) {
            String section = sections[i];
            IniFile objIni = objsInis[i];
            
            if(section.startsWith("obj ")) {
                String[] data = StringTools.cutOnStrings(section, ' ');
                
                String type = data[1];
                String name = null;
                if(data.length >= 3) {
                    StringBuffer sb = new StringBuffer();
                    for(int x=2; x<data.length; x++) {
                        sb.append(data[x]);
                        if(x != data.length - 1) sb.append(' ');
                    }
                    
                    name = sb.toString();
                }
                
                String tmp = objIni.get("pos");
                String[] poses = tmp==null?null:StringTools.cutOnStrings(tmp, ';');
                
                for(int x=0; x<(poses==null?1:poses.length); x++) {
                    float[] pos = null;
                    String thisName = name;
                    if(poses != null) {
                        pos = StringTools.cutOnFloats(poses[x], ',');
                        if(poses.length > 1 && thisName != null) thisName += "_" + (x+1);
                    }
					
                    defaultWas |= type.equals("lightgroup") && "default".equals(name);
                    loadObject(game, world, data[1], thisName, objIni, 
							pos, lightgroupdata, sourcesToPlay);
                }
            }
        }
        
        if(!defaultWas) {
            lightgroupdata.add(LightGroup.defaultGroup);
            lightgroupdata.add(new String[]{"all"});
            lightgroupdata.add(new String[0]);
        }

        for(int i=0; i<lightgroupdata.size(); i+=3) {
            LightGroup group = (LightGroup) lightgroupdata.get(i);

            String[] groupLights = (String[]) lightgroupdata.get(i+1);
            boolean all = groupLights.length==1?groupLights[0].equals("all"):false;
            
            if(all) group.lights = (ArrayList<Light>)LightGroup.allLights.clone();
            else for(String lightName : groupLights) {
                for(Light light : LightGroup.allLights) {
                    if(lightName.equals(light.name)) {
                        group.lights.add(light);
                        break;
                    }
                }
            }
            
            groupLights = (String[]) lightgroupdata.get(i+2);
            
            for(String lightName : groupLights) {
                for(int xx=0; xx<group.lights.size(); xx++) {
                    Light light = group.lights.get(xx);
                    if(lightName.equals(light.name)) {
                        group.lights.remove(xx);
                        break;
                    }
                }
            }
        }
    }

    private static void loadObject(Game game, World world, String objType, String objName, IniFile ini, 
            float[] pos, ArrayList lightgroupdata, ArrayList<Integer> sourcesToPlay) {
        //yeah...
        //todo maybe move code to objects?
        
        Entity obj = null;
        if(objType.equals("spr")) {
            obj = loadSprite(objName, pos, game, world, ini, false);
        } else if(objType.equals("billboard")) {
            obj = loadSprite(objName, pos, game, world, ini, true);
        } else if(objType.equals("mesh")) {
            obj = loadMesh(objName, pos, game, world, ini);
        } else if(objType.equals("sound")) {
            obj = loadSoundSourceEntity(objName, pos, game, world, ini, sourcesToPlay);
        } else if(objType.equals("entity")) {
            obj = new Entity();
            loadDefEntity(obj, pos, objName, game, world, ini);
        } else if(objType.equals("teleport")) {
            obj = loadTP(objName, pos, game, world, ini);
        } else if(objType.equals("box")) {
            obj = loadBox(objName, pos, game, world, ini);
        } else if(objType.equals("light")) {
            
            float[] color = StringTools.cutOnFloats(ini.getDef("color", "255,255,255"), ',');

			Vector3D position = null;
            Vector3D dir = new Vector3D();
            dir.setDirection(ini.getFloat("rot_x", 0), ini.getFloat("rot_y", 0));

            String tmp = ini.getDef("type", "point");
            boolean isSpot = tmp.equals("spot");
			boolean isPoint = true;
            
            if(tmp.equals("point") || isSpot) {
                position = new Vector3D(pos[0], pos[1], pos[2]);

                if(!isSpot) dir = null;
            } else if(tmp.equals("dir")) {
                position = dir;
				dir = null;
				isPoint = false;
            }

            Light light = new Light(objName, position, isPoint, dir, color);

            if(isSpot) light.cutoff = ini.getFloat("cutoff", light.cutoff);

            LightGroup.allLights.add(light);
        } else if(objType.equals("lightgroup")) {
            LightGroup group;
            boolean defaultGroup = objName.equals("default");
            if(defaultGroup) {
                group = LightGroup.defaultGroup;
            } else {
                group = new LightGroup(objName);
                LightGroup.lightgroups.add(group);
            }

            group.setAmbient(StringTools.cutOnFloats(ini.getDef("ambient", "0,0,0"), ','));

            lightgroupdata.add(group);
            lightgroupdata.add(StringTools.cutOnStrings(ini.getDef("include", defaultGroup ? "all" : ""), ','));
            lightgroupdata.add(StringTools.cutOnStrings(ini.getDef("exclude", ""), ','));
        }
        
        if(obj != null) world.addEntity(obj);
    }

    private static SoundSourceEntity loadSoundSourceEntity(String name, float[] pos,
            Game game, World world, IniFile ini, ArrayList<Integer> sourcesToPlay) {
        SoundSource source = new SoundSource(ini.get("sound"));
        
        source.setVolume(ini.getFloat("volume", 1));
        source.setPitch(ini.getFloat("pitch", 1));
        source.setLoop(ini.getInt("loop", 1) == 1);
        
        source.set3D(ini.getInt("3d_effects", 1) == 1);
		
		boolean linear = ini.getInt("linear_attenuation", SoundSource.LINEAR_DIST?1:0) == 1;
		
        source.setDistance(
			ini.getFloat("min_distance", linear ? SoundSource.MIN_LINEAR_DIST : SoundSource.MIN_DIST),
			ini.getFloat("max_distance", SoundSource.MAX_DIST),
			linear
		);
		
		String tmp = ini.get("sound_type");
		if(tmp != null) {
			if(tmp.equals("sound")) source.setSoundType(Configuration.SOUND);
			else if(tmp.equals("music")) source.setSoundType(Configuration.MUSIC);
			else if(tmp.equals("footstep")) source.setSoundType(Configuration.FOOTSTEP);
		}
        
        SoundSourceEntity sound = new SoundSourceEntity(source, ini.getInt("random_offset", 0) == 1);
        
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

    private static MeshObject loadMesh(
			String name, float[] pos, 
			Game game, World world, IniFile ini) {
        MeshObject mesh = new MeshObject(game.e3d.getMeshInstance(ini.get("model"), null));
        
        mesh.meshCollision = ini.getInt("ph_mesh_collision", mesh.meshCollision?1:0) == 1;
        mesh.visible = ini.getInt("visible", mesh.visible?1:0) == 1;
        
        loadPhysEntity(mesh, pos, name, game, world, ini);
		
        return mesh;
    }

    private static SpriteObject loadSprite(
			String name, float[] pos, 
			Game game, World world, IniFile ini,
			boolean billboard) {
        Material loadedMat = game.e3d.getMaterial(ini.get("tex"), null);
        if(!(loadedMat instanceof WorldMaterial)) {
            System.out.println("wrong material???");
            return null;
        }
        
        WorldMaterial mat = (WorldMaterial) loadedMat;
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
        
        Sprite spr = new Sprite(mat, billboard, w, h, align);
        spr.load(new IniFile(StringTools.cutOnStrings(ini.getDef("options", ""), ';'), false));
        
        SpriteObject sprObj = new SpriteObject(spr);
        
        sprObj.visible = ini.getInt("visible", sprObj.visible?1:0) == 1;
        
        loadDefEntity(sprObj, pos, name, game, world, ini);
        
        return sprObj;
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
        if(tmp != null) obj.activateWhen = Scripting.loadScript(game.main, "return "+tmp);
        
        tmp = ini.get("on_activate");
        if(tmp != null) obj.onActivate = Scripting.loadScript(game.main, tmp);
        
        tmp = ini.get("script_on_activate");
        if(tmp != null) obj.onActivate = Scripting.loadScriptFromFile(game.main, tmp);
        
        tmp = ini.get("on_fail");
        if(tmp != null) obj.onFail = Scripting.loadScript(game.main, tmp);
        
        tmp = ini.get("script_on_fail");
        if(tmp != null) obj.onFail = Scripting.loadScriptFromFile(game.main, tmp);
    }

}
