package code.utils;

import code.Engine;
import code.Screen;
import code.game.DialogScreen;
import code.game.Fade;
import code.game.Game;
import code.game.Main;
import code.game.world.entities.Entity;
import code.game.world.entities.PhysEntity;
import code.game.world.entities.SoundSourceEntity;
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
    
    public static void initFunctions(final Main main) {
        LuaTable lua = main.lua;
        
        lua.set("loadMap", new TwoArgFunction() {
            public LuaValue call(final LuaValue map, final LuaValue data)  {
                final Game game = main.getGame();
                LuaValue fade = (!data.isnil() && data.istable()) ? data.get("fade") : LuaValue.NIL;
                
                if(game == null || fade.isnil()) {
                    main.loadMap(map, data);
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
                            main.loadMap(map, data);
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
                    game.loadMap(game.currentMap, new Vector3D(game.player.pos), game.player.rotX, game.player.rotY);
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
        
        addEntitiesScripts(main, lua);
        
    }
    
    private static void addEntitiesScripts(final Main main, LuaTable lua) {
        
        lua.set("playSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                Entity found = game != null?game.world.findObject(obj.toString()):null;
                SoundSourceEntity snd = 
                        (found != null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;
                
                if(snd != null) snd.source.play();
                return LuaValue.NIL;
            }
        });
        
        lua.set("stopSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                Entity found = game != null?game.world.findObject(obj.toString()):null;
                SoundSourceEntity snd = 
                        (found != null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;
                
                if(snd != null) snd.source.stop();
                return LuaValue.NIL;
            }
        });
        
        lua.set("rewindSource", new OneArgFunction() {
            public LuaValue call(LuaValue obj)  {
                Game game = main.getGame();
                Entity found = game != null?game.world.findObject(obj.toString()):null;
                SoundSourceEntity snd = 
                        (found != null && found instanceof SoundSourceEntity)?(SoundSourceEntity)found:null;
                
                if(snd != null) snd.source.rewind();
                return LuaValue.NIL;
            }
        });
        
        lua.set("objVar", new ThreeArgFunction() {
            public LuaValue call(LuaValue obj, LuaValue var, LuaValue val)  {
                Game game = main.getGame();
                Entity entity = game != null?game.world.findObject(obj.toString()):null;
                PhysEntity physEntity = 
                        (entity != null && entity instanceof PhysEntity)?(PhysEntity)entity:null;
                
                if(entity == null) return LuaValue.NIL;
                
                String varName = var.toString();
                
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
                    if(!val.isnil() && val.isnumber()) entity.pos.x = var.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.x);
                    
                } else if(varName.equals("y")) {
                    if(!val.isnil() && val.isnumber()) entity.pos.y = var.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.y);
                    
                } else if(varName.equals("z")) {
                    if(!val.isnil() && val.isnumber()) entity.pos.z = var.tofloat();
                    
                    return LuaValue.valueOf(entity.pos.z);
                    
                } else if(varName.equals("activable")) {
                    if(!val.isnil() && val.isboolean()) entity.activable = var.toboolean();
                    
                    return LuaValue.valueOf(entity.activable);
                    
                } else if(varName.equals("activateRadius")) {
                    if(!val.isnil() && val.isnumber()) entity.activateRadius = var.tofloat();
                    
                    return LuaValue.valueOf(entity.activateRadius);
                    
                } else if(varName.equals("clickable")) {
                    if(!val.isnil() && val.isboolean()) entity.clickable = var.toboolean();
                    
                    return LuaValue.valueOf(entity.clickable);
                    
                } else if(varName.equals("pointable")) {
                    if(!val.isnil() && val.isboolean()) entity.pointable = var.toboolean();
                    
                    return LuaValue.valueOf(entity.pointable);
                    
                } else if(varName.equals("animateWhenPaused")) {
                    if(!val.isnil() && val.isboolean()) entity.animateWhenPaused = var.toboolean();
                    
                    return LuaValue.valueOf(entity.animateWhenPaused);
                    
                } 
                //Physics entities
                else if(varName.equals("hp") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.hp = var.tofloat();
                    
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
                    if(!val.isnil() && val.isnumber()) physEntity.rotY = var.tofloat();
                    
                    return LuaValue.valueOf(physEntity.rotY);
                    
                } else if(varName.equals("radius") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.radius = var.tofloat();
                    
                    return LuaValue.valueOf(physEntity.radius);
                    
                } else if(varName.equals("height") && physEntity != null) {
                    if(!val.isnil() && val.isnumber()) physEntity.height = var.tofloat();
                    
                    return LuaValue.valueOf(physEntity.height);
                    
                } else if(varName.equals("physics") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.physics = var.toboolean();
                    
                    return LuaValue.valueOf(physEntity.physics);
                    
                } else if(varName.equals("pushable") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.pushable = var.toboolean();
                    
                    return LuaValue.valueOf(physEntity.pushable);
                    
                } else if(varName.equals("canPush") && physEntity != null) {
                    if(!val.isnil() && val.isboolean()) physEntity.canPush = var.toboolean();
                    
                    return LuaValue.valueOf(physEntity.canPush);
                    
                } 
                
                return LuaValue.NIL;
            }
        });
        
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
            Engine.printError(e);
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
            Engine.printError(e);
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
