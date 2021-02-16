package code.game.scripting;

import code.engine.Screen;

import code.audio.AudioEngine;

import code.engine3d.Lighting.Light;
import code.engine3d.Lighting.LightGroup;
import code.game.DialogScreen;
import code.game.Fade;
import code.game.Game;
import code.game.Main;
import code.game.Pause;

import code.game.world.entities.Entity;
import code.game.world.entities.MeshObject;
import code.game.world.entities.PhysEntity;
import code.game.world.entities.Player;
import code.game.world.entities.SoundSourceEntity;
import code.game.world.entities.SpriteObject;

import code.math.Vector3D;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 *
 * @author Roman Lahin
 */
public class Scripting {
    
    public static LuaValue loadScript(Main main, String script) {
        try {
            return main.lua.load(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static LuaValue loadScriptFromFile(Main main, String path) {
        File file = new File("data", path);
        if(!file.exists()) {
            System.out.println("No such file "+file.getAbsolutePath()+"!");
            return null;
        }
        
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            byte[] chars = new byte[dis.available()];
            dis.readFully(chars);
            dis.close();
            dis = null;
            
            String script = new String(chars);
            LuaValue chunk = main.lua.load(script);
            return chunk;
            
        } catch(Exception e) {
            if(dis != null) {
                try{
                    dis.close();
                } catch(Exception ee) {}
            }
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static void runScriptFromFile(Main main, String path) {
        LuaValue chunk = loadScriptFromFile(main, path);
        if(chunk != null) chunk.call();
    }

    public static LuaValue runScript(Main main, String script) {
        try {
            LuaValue chunk = main.lua.load(script);
            return chunk.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return LuaValue.NIL;
    }

    public static LuaValue runScript(LuaValue chunk) {
        try {
            return chunk.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return LuaValue.NIL;
    }

    public static void loadMap(Main main, LuaValue mapArg, LuaValue data) {
        String map = mapArg.toString();
        Game game = main.getGame();

        Vector3D newPlayerPos = null;
        float rotX = Float.MAX_VALUE;
        float rotY = Float.MAX_VALUE;
        
        if(!data.isnil() && data.istable()) {
            LuaValue pos = data.get("pos");
            
            if(!pos.isnil() && pos.istable()) {
                newPlayerPos = new Vector3D(
                        pos.get(1).tofloat(),
                        pos.get(2).tofloat(),
                        pos.get(3).tofloat());
            }
            
            if(!data.get("rotX").isnil()) rotY = data.get("rotX").tofloat();
            if(!data.get("rotY").isnil()) rotY = data.get("rotY").tofloat();
        }

        if(game == null) {
            game = new Game(main);
            main.setScreen(game, true);
        }
        game.loadMap(map, newPlayerPos, rotX, rotY);
    }
    
    public static void initFunctions(final Main main) {
        LuaTable lua = main.lua;
        
        lua.set("loadMap", new TwoArgFunction() {
            public LuaValue call(final LuaValue map, final LuaValue data)  {
                final Game game = main.getGame();
                LuaValue fade = (!data.isnil() && data.istable()) ? data.get("fade") : LuaValue.NIL;
                
                if(game == null || fade.isnil()) {
                    loadMap(main, map, data);
                    return LuaValue.NIL;
                } else {
                    final int fadeColor;
                    final int fadeTime;
                    if(!fade.get("color").isnil()) fadeColor = fade.get("color").toint();
                    else fadeColor = 0xffffff;

                    if(!fade.get("time").isnil()) fadeTime = fade.get("time").toint();
                    else fadeTime = 1000;

                    game.paused = true;
                    game.setFade(new Fade(false, fadeColor, fadeTime) {
                        public void onDone() {
                            loadMap(main, map, data);
                            game.paused = false;
                            game.setFade(new Fade(true, fadeColor, fadeTime));
                        }
                    });
                
                    return LuaValue.NIL;
                }
            }
        });
        
        lua.set("reload", new ZeroArgFunction() {
            public LuaValue call()  {
                Game game = main.getGame();
                
                if(game != null) {
                    game.loadMap(game.currentMap, 
                            new Vector3D(game.player.pos), 
                            game.player.rotX, 
                            game.player.rotY);
                }
                return LuaValue.NIL;
            }
        });
        
        lua.set("showDialog", new OneArgFunction() {
            public LuaValue call(LuaValue arg)  {
                String dialog = arg.toString();
                Screen screen = main.getScreen();
                
                if(screen instanceof Game) ((Game)screen).openDialog(dialog, false);
                else if(screen instanceof DialogScreen) ((DialogScreen)screen).game.openDialog(dialog, false);
                return LuaValue.NIL;
            }
        });
        
        lua.set("loadDialog", new OneArgFunction() {
            public LuaValue call(LuaValue arg)  {
                String dialog = arg.toString();
                Screen screen = main.getScreen();
                
                if(screen instanceof Game) ((Game)screen).openDialog(dialog, true);
                else if(screen instanceof DialogScreen) ((DialogScreen)screen).game.openDialog(dialog, true);
                return LuaValue.NIL;
            }
        });
        
        lua.set("playMusic", new TwoArgFunction() {
            public LuaValue call(LuaValue file, LuaValue restart)  {
                String name = file.toString();
                
                if(!name.equals(main.musPlayer.soundName)) {
                    main.musPlayer.stop();
                    main.musPlayer.free();
                    main.musPlayer.loadFile(name);
                    main.musPlayer.play();
                } else if(!main.musPlayer.isPlaying()) main.musPlayer.play();
                else if(restart.toboolean()) main.musPlayer.rewind();
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("rewindMusic", new ZeroArgFunction() {
            public LuaValue call() {
                main.musPlayer.rewind();
                return LuaValue.NIL;
            }
        });
        
        
        lua.set("stopMusic", new ZeroArgFunction() {
            public LuaValue call()  {
                main.musPlayer.stop();
                return LuaValue.NIL;
            }
        });
        
        lua.set("isMusicPlaying", new ZeroArgFunction() {
            public LuaValue call()  {
                return LuaBoolean.valueOf(main.musPlayer.isPlaying());
            }
        });
        
        lua.set("setMusicPitch", new OneArgFunction() {
            public LuaValue call(LuaValue arg)  {
                main.musPlayer.setPitch(arg.tofloat());
                return LuaValue.NIL;
            }
        });
        lua.set("audioSuspend", new ZeroArgFunction() {
            public LuaValue call()  {
                AudioEngine.suspend();
                return LuaValue.NIL;
            }
        });
        
        lua.set("audioProcess", new ZeroArgFunction() {
            public LuaValue call()  {
                AudioEngine.process();
                return LuaValue.NIL;
            }
        });
        
        lua.set("fade", new ThreeArgFunction() {
            public LuaValue call(LuaValue fadeIn, final LuaValue func, LuaValue data)  {
                Game game = main.getGame();
                
                if(game != null) {
                    final int fadeColor;
                    final int fadeTime;
                    if(!data.isnil() && !data.get("color").isnil()) fadeColor = data.get("color").toint();
                    else fadeColor = 0xffffff;

                    if(!data.isnil() && !data.get("time").isnil()) fadeTime = data.get("time").toint();
                    else fadeTime = 1000;

                    game.setFade(new Fade(fadeIn.toboolean(), fadeColor, fadeTime) {
                        public void onDone() {
                            if(!func.isnil()) runScript(func);
                        }
                    });
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("pause", new TwoArgFunction() {
            public LuaValue call(LuaValue time, final LuaValue func)  {
                Game game = main.getGame();
                
                if(game != null) {
                    game.addPause(new Pause(time.toint()) {
                        public void onDone() {
                            if(!func.isnil()) runScript(func);
                        }
                    });
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("fov", new OneArgFunction() {
            public LuaValue call(LuaValue fov)  {
                if(fov.isnumber()) main.conf.fov = fov.tofloat();
                
                return LuaValue.valueOf(main.conf.fov);
            }
        });
        
        lua.set("atan2", new TwoArgFunction() {
            public LuaValue call(LuaValue p1, LuaValue p2)  {
                if(p1.istable() && p2.istable()) {
                    return LuaValue.valueOf(Math.toDegrees(
                            Math.atan2(
                                    p1.get(1).todouble()-p2.get(1).todouble(), 
                                    p1.get(3).todouble()-p2.get(3).todouble())
                    ));
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("lookAt", new TwoArgFunction() {
            public LuaValue call(LuaValue p1, LuaValue p2)  {
                String o1 = '\"'+p1.toString()+'\"', o2 = '\"'+p2.toString()+'\"';
                
                runScript(main,
                        "objVar("+o1+",\"rotY\",atan2("
                    +"objVar("+o1+",\"pos\"),"
                    +"objVar("+o2+",\"pos\")))");
                
                return LuaValue.NIL;
            }
        });
        
        addEntitiesScripts(main, lua);
        
    }
    
    private static void addEntitiesScripts(final Main main, LuaTable lua) {
        
        lua.set("playSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                
                if(obj.isstring()) {
                    Entity found = game!=null?game.world.findObject(obj.toString()):null;
                    SoundSourceEntity snd
                            = (found!=null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;

                    if(snd != null) snd.source.play();
                } else if(obj.istable()) {
                    int[] list = buildSourcesArray(game, obj);
                    if(list.length > 0) AudioEngine.playMultiple(list);
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("stopSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                
                if(obj.isstring()) {
                    Entity found = game!=null?game.world.findObject(obj.toString()):null;
                    SoundSourceEntity snd
                            = (found!=null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;

                    if(snd != null) snd.source.stop();
                } else if(obj.istable()) {
                    int[] list = buildSourcesArray(game, obj);
                    if(list.length > 0) AudioEngine.stopMultiple(list);
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("rewindSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                
                if(obj.isstring()) {
                    Entity found = game!=null?game.world.findObject(obj.toString()):null;
                    SoundSourceEntity snd
                            = (found!=null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;

                    if(snd != null) snd.source.rewind();
                } else if(obj.istable()) {
                    int[] list = buildSourcesArray(game, obj);
                    if(list.length > 0) AudioEngine.rewindMultiple(list);
                }
                
                return LuaValue.NIL;
            }
        });
        
        lua.set("objVar", new ThreeArgFunction() {
            public LuaValue call(LuaValue obj, LuaValue var, LuaValue val)  {
                Game game = main.getGame();
                Entity entity = game != null?game.world.findObject(obj.toString()):null;
                PhysEntity physEntity = 
                        (entity != null && entity instanceof PhysEntity)?(PhysEntity)entity:null;
                MeshObject meshObj = 
                        (entity != null && entity instanceof MeshObject)?(MeshObject)entity:null;
                SpriteObject sprObj = 
                        (entity != null && entity instanceof SpriteObject)?(SpriteObject)entity:null;
                SoundSourceEntity sndObj = 
                        (entity != null && entity instanceof SoundSourceEntity)?(SoundSourceEntity)entity:null;
                Player player = 
                        (entity != null && entity instanceof Player)?(Player)entity:null;
                
                String varName = var.toString();
                
                if(entity == null) {
                    Light light = LightGroup.findLight(obj.toString());
                    
                    if(light == null) return LuaValue.NIL;
                    
                    if(varName.equals("color")) {
                        if(!val.isnil()) {
                            if(val.istable()) 
                                light.setColor(new float[] {
                                val.get(1).tofloat(),
                                val.get(2).tofloat(),
                                val.get(3).tofloat()
                            });
                            else if(val.isnumber()) 
                                light.setColor(new float[] {val.tofloat()});
                        }

                        return LuaTable.listOf(new LuaValue[]{
                            LuaValue.valueOf(light.color.get(0)*255),
                            LuaValue.valueOf(light.color.get(1)*255),
                            LuaValue.valueOf(light.color.get(2)*255)
                        });
                    }
                    
                    return LuaValue.NIL;
                }
                
                //All entities
                if(varName.equals("pos")) {
                    if(!val.isnil() && val.istable()) {
                        entity.pos.x = val.get(1).tofloat();
                        entity.pos.y = val.get(2).tofloat();
                        entity.pos.z = val.get(3).tofloat();
                    }
                    
                    return LuaTable.listOf(new LuaValue[]{
                        LuaValue.valueOf(entity.pos.x), 
                        LuaValue.valueOf(entity.pos.y), 
                        LuaValue.valueOf(entity.pos.z)
                    });
                    
                } else if(varName.equals("x")) {
                    if(!val.isnil() && val.isnumber()) entity.pos.x = val.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.x);
                    
                } else if(varName.equals("y")) {
                    if(!val.isnil() && val.isnumber()) entity.pos.y = val.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.y);
                    
                } else if(varName.equals("z")) {
                    if(!val.isnil() && val.isnumber()) entity.pos.z = val.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.z);
                    
                } else if(varName.equals("activable")) {
                    if(!val.isnil() && val.isboolean()) entity.activable = val.toboolean();
                    
                    return LuaValue.valueOf(entity.activable);
                    
                } else if(varName.equals("activateRadius")) {
                    if(!val.isnil() && val.isnumber()) entity.activateRadius = val.tofloat();
                    
                    return LuaValue.valueOf(entity.activateRadius);
                    
                } else if(varName.equals("clickable")) {
                    if(!val.isnil() && val.isboolean()) entity.clickable = val.toboolean();
                    
                    return LuaValue.valueOf(entity.clickable);
                    
                } else if(varName.equals("pointable")) {
                    if(!val.isnil() && val.isboolean()) entity.pointable = val.toboolean();
                    
                    return LuaValue.valueOf(entity.pointable);
                    
                } else if(varName.equals("animateWhenPaused")) {
                    if(!val.isnil() && val.isboolean()) entity.animateWhenPaused = val.toboolean();
                    
                    return LuaValue.valueOf(entity.animateWhenPaused);
                    
                } 
                //Physics entities
                else if(varName.equals("hp") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.hp = val.tofloat();
                    
                    return LuaValue.valueOf(physEntity.hp);
                    
                } else if(varName.equals("speed") && physEntity != null) {
                    if(!val.isnil() && val.istable()) {
                        physEntity.speed.x = val.get(1).tofloat();
                        physEntity.speed.y = val.get(2).tofloat();
                        physEntity.speed.z = val.get(3).tofloat();
                    }
                    
                    return LuaTable.listOf(new LuaValue[]{
                        LuaValue.valueOf(physEntity.speed.x), 
                        LuaValue.valueOf(physEntity.speed.y), 
                        LuaValue.valueOf(physEntity.speed.z)
                    });
                    
                } else if(varName.equals("rotY") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.rotY = val.tofloat();
                    
                    return LuaValue.valueOf(physEntity.rotY);
                   
                } 
                //Player vars
                else if(varName.equals("rotX") && player != null) {
                    if(!val.isnil() && val.isnumber()) player.rotX = val.tofloat();
                    
                    return LuaValue.valueOf(player.rotX);
                } 
                //Physics entities again
                else if(varName.equals("radius") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.radius = val.tofloat();
                    
                    return LuaValue.valueOf(physEntity.radius);
                    
                } else if(varName.equals("height") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.height = val.tofloat();
                    
                    return LuaValue.valueOf(physEntity.height);
                    
                } else if(varName.equals("physics") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.physics = val.toboolean();
                    
                    return LuaValue.valueOf(physEntity.physics);
                    
                } else if(varName.equals("pushable") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.pushable = val.toboolean();
                    
                    return LuaValue.valueOf(physEntity.pushable);
                    
                } else if(varName.equals("canPush") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.canPush = val.toboolean();
                    
                    return LuaValue.valueOf(physEntity.canPush);
                    
                } 
                //Meshes and Sprites
                else if(varName.equals("visible") && (meshObj != null || sprObj != null)) {
                    if(meshObj != null) {
                        if(!val.isnil() && val.isboolean()) meshObj.visible = val.toboolean();
                        return LuaValue.valueOf(meshObj.visible);
                    } else {
                        if(!val.isnil() && val.isboolean()) sprObj.visible = val.toboolean();
                        return LuaValue.valueOf(sprObj.visible);
                    }
                    
                }
                //Sound sources
                else if(varName.equals("volume") && sndObj != null) {
                    if(!val.isnil() && val.isnumber()) sndObj.source.setVolume(val.tofloat());
                    
                    return LuaValue.valueOf(sndObj.source.getVolume());
                } else if(varName.equals("pitch") && sndObj != null) {
                    if(!val.isnil() && val.isnumber()) sndObj.source.setPitch(val.tofloat());
                    
                    return LuaValue.valueOf(sndObj.source.getPitch());
                } else if(varName.equals("loop") && sndObj != null) {
                    if(!val.isnil() && val.isboolean()) sndObj.source.setLoop(val.toboolean());
                    
                    return LuaValue.valueOf(sndObj.source.getLoop());
                }
                
                return LuaValue.NIL;
            }
        });
        
    }
    
    public static int[] buildSourcesArray(Game game, LuaValue list) {
        Vector<Integer> sourcesV = new Vector();
        
        for(int i=0; i<list.length(); i++) {
            Entity found = game!=null?game.world.findObject(list.get(i+1).toString()):null;
            SoundSourceEntity snd
                    = (found != null && found instanceof SoundSourceEntity) ? (SoundSourceEntity) found : null;
            
            if(snd != null) sourcesV.add(snd.source.getID());
        }
        
        int[] sources = new int[sourcesV.size()];

        for(int i=0; i<sources.length; i++) {
            sources[i] = sourcesV.elementAt(i);
        }
        
        return sources;
    }
    
    public static void save(LuaValue save) {
        File file = new File("saves/");
        if(!file.exists()) file.mkdir();
        
        FileOutputStream fos = null;
        try {
            file = new File("saves", "luasave");
            if(!file.exists()) file.createNewFile();
            
            DataOutputStream dos = new DataOutputStream((fos = new FileOutputStream(file)));
            dos.writeInt(0); //version
            save(save, dos);
            dos.close();
            
        } catch (Exception e) {
            if(fos != null) try {
                fos.close();
            } catch(Exception ee) {}
            e.printStackTrace();
        }
    }
    
    static final int TAB = 0, BOL = 1, INT = 2, NUM = 3, STR = 4, NIL = 5;
    
    private static void save(LuaValue val, DataOutputStream dos) throws IOException {
        if(val.istable()) {
            dos.writeInt(TAB);
            
            Vector<Varargs> vals = new Vector();
            
            LuaValue k = LuaValue.NIL;
            while(true) {
                Varargs next = val.next(k);
                k = next.arg1();
                if(k.isnil()) break;
                vals.add(next);
            }
            
            dos.writeInt(vals.size());
            for(int i=0; i<vals.size(); i++) {
                Varargs el = vals.elementAt(i);
                save(el.arg1(), dos);
                save(el.arg(2), dos);
            }
            
        } else if(val.isboolean()) {
            dos.writeInt(BOL);
            dos.writeBoolean(val.toboolean());
        } else if(val.isinttype()) {
            dos.writeInt(INT);
            dos.writeInt(val.toint());
        } else if(val.isnumber()) {
            dos.writeInt(NUM);
            dos.writeDouble(val.todouble());
        } else if(val.isstring()) {
            dos.writeInt(STR);
            dos.writeUTF(val.toString());
        } else if(val.isnil()) {
            dos.writeInt(NIL);
        }
    }
    
    public static LuaTable load(Main main) {
        File file = new File("saves", "luasave");
        
        LuaTable save = new LuaTable();
        if(!file.exists()) return save;
        
        FileInputStream fis = null;
        try {
            
            DataInputStream dis = new DataInputStream((fis = new FileInputStream(file)));
            
            dis.skip(4); //version
            LuaValue tmp = load(main.lua, dis);
            if(tmp instanceof LuaTable) save = (LuaTable) tmp;
            else System.out.println("save is not a table! what did you made lol");
                
            dis.close();
            
        } catch (Exception e) {
            if(fis != null) try {
                fis.close();
            } catch(Exception ee) {}
            e.printStackTrace();
        }
        
        return save;
    }
    
    private static LuaValue load(Globals global, DataInputStream dis) throws IOException {
        int type = dis.readInt();
        
        if(type == TAB) {
            LuaTable table = new LuaTable();
            int count = dis.readInt();
            
            for(int i=0; i<count; i++) {
                table.set(load(global, dis), load(global, dis));
            }
            
            return table;
        } else if(type == BOL) {
            return LuaValue.valueOf(dis.readBoolean());
        } else if(type == INT) {
            return LuaInteger.valueOf(dis.readInt());
        } else if(type == NUM) {
            return LuaValue.valueOf(dis.readDouble());
        } else if(type == STR) {
            return LuaValue.valueOf(dis.readUTF());
        } else if(type == NIL) {
            return LuaValue.NIL;
        }
        
        return null;
    }

}
