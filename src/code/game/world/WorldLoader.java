package code.game.world;

import code.utils.Asset;
import code.engine3d.Mesh;
import code.utils.MeshLoader;
import code.game.Game;
import code.utils.IniFile;
import code.utils.StringTools;

/**
 *
 * @author Roman Lahin
 */
public class WorldLoader {

    public static void loadWorld(Game game, String folder) {
        Asset.freeThings();
        
        IniFile lvl = Asset.loadIni(folder+"level.ini", true);
        
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
        
        loadObjects(game, lvl, world);
        
        game.player.rotX = game.player.rotY = 0;
        
        game.world = world;
        world.objects.add(game.player);
        
        if(lvl.groupExists("MUSIC")) {
            String tmp = lvl.get("MUSIC","PITCH");
            if(tmp != null) game.main.musPlayer.setPitch(StringTools.parseFloat(tmp));
            
            boolean rewinded = false;
            tmp = lvl.get("MUSIC","PATH");
            if(tmp != null && 
                    (lvl.getInt("MUSIC", "DONT_CHANGE_MUSIC", 0) == 0 || !game.main.musPlayer.isPlaying())) {
                game.main.musPlayer.stop();
                game.main.musPlayer.loadFile(tmp);
                game.main.musPlayer.start();
                rewinded = true;
            }
            
            if(!rewinded && lvl.getInt("MUSIC", "REWIND", 0) == 1) {
                game.main.musPlayer.rewind();
            }
        }
        if(game.main.musPlayer.buffer != null) game.main.musPlayer.buffer.using = true;
        
        Asset.destroyThings();
    }
    
    public static void loadObjects(Game game, IniFile lvl, World world) {
        //todo
        
        /*String[] objs = lvl.keys();
        Hashtable[] tables = lvl.hashtables();
        
        Group sprites = new Group();
        
        Fog blackFog = null;
        Fog grayFog = null;
        Fog whiteFog = null;
        if(game.fog!=null) {
            blackFog = (Fog)game.fog.duplicate();
            blackFog.setColor(0);
            grayFog = (Fog)game.fog.duplicate();
            grayFog.setColor(0x808080);
            whiteFog = (Fog)game.fog.duplicate();
            whiteFog.setColor(0xffffff);
        }
        Material m = new Material();
        m.setColor(Material.AMBIENT, 0xff000000);
        m.setColor(Material.DIFFUSE, 0xff000000);
        m.setColor(Material.EMISSIVE, 0xffffffff);
        
        for(int i=0;i<objs.length;i++) {
            String objectType = objs[i];
            if(!objectType.startsWith("OBJECT_")) continue;
            
            Hashtable table = tables[i];
            Vector3D pos = new Vector3D((String)table.get("POS"),',');
            
            if(objectType.startsWith("OBJECT_SPRITE")) {
                int blendMode = CompositingMode.REPLACE;
                CompositingMode cm = new CompositingMode();
                Appearance ap = new Appearance();
                ap.setFog(game.fog);
                ap.setCompositingMode(cm);
                
                String tmp = (String)table.get("BLEND_MODE");
                if(tmp!=null) {
                    if(tmp.equals("ALPHA")) blendMode = CompositingMode.ALPHA;
                    else if(tmp.equals("ALPHA_ADD")) {
                        blendMode = CompositingMode.ALPHA_ADD;
                        ap.setFog(blackFog);
                    }
                    else if(tmp.equals("MODULATE")) {
                        blendMode = CompositingMode.MODULATE;
                        ap.setFog(whiteFog);
                    }
                    else if(tmp.equals("MODULATE_X2")) {
                        blendMode = CompositingMode.MODULATE_X2;
                        ap.setFog(grayFog);
                    }
                }
                    
                cm.setBlending(blendMode);
                
                Image2D img = Asset.loadImage2D((String)table.get("TEX"),blendMode==CompositingMode.ALPHA);
                
                Sprite3D spr = new Sprite3D(true,img,ap);
                
                tmp = (String)table.get("SIZE");
                if(tmp!=null) {
                    float[] sizes = StringTools.cutOnFloats(tmp, ',');
                    float scalex = 1; float scaley = 1;
                    
                    if(sizes.length==2) {
                        scalex = sizes[0]; scaley = sizes[1];
                    } else {
                        scalex = scaley = sizes[0];
                    }
                    
                    spr.setScale(scalex, scaley, 1);
                }
                
                spr.setTranslation(pos.x, pos.y, pos.z);
                        
                sprites.addChild(spr);
            }
            
        }
        if(sprites.getChildCount()>0) world.addChild(sprites);*/
    }

}
