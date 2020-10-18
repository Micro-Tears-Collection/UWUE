package code.utils;

import code.Engine;
import code.game.Main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;

/**
 *
 * @author Roman Lahin
 */
public class Scripting {
    
    public static void initFunctions(final Main main) {
        LuaTable luagame = main.luagame;
        
        luagame.set("loadMap", new OneArgFunction() {
            public LuaValue call(LuaValue arg)  {
                main.loadMap(arg.toString());
                return LuaValue.NIL;
            }
        });
        
        luagame.set("playMusic", new TwoArgFunction() {
            public LuaValue call(LuaValue file, LuaValue restart)  {
                String name = file.toString();
                
                if(!name.equals(main.musPlayer.soundName) || restart.toboolean()) {
                    
                    main.musPlayer.stop();
                    if(!name.equals(main.musPlayer.soundName)) {
                        main.musPlayer.free();
                        main.musPlayer.loadFile(name);
                    }
                    main.musPlayer.start();
                    
                }
                return LuaValue.NIL;
            }
        });
        
        luagame.set("stopMusic", new ZeroArgFunction() {
            public LuaValue call()  {
                main.musPlayer.stop();
                return LuaValue.NIL;
            }
        });
        
        luagame.set("setMusicPitch", new OneArgFunction() {
            public LuaValue call(LuaValue arg)  {
                main.musPlayer.setPitch(arg.tofloat());
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
