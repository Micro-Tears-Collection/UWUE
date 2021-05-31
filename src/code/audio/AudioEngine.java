package code.audio;

import code.math.Vector3D;
import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

/**
 *
 * @author Roman Lahin
 */
public class AudioEngine {
    
    static long device;
    static long context;
    static ALCCapabilities alcCapabilities;
    static ALCapabilities alCapabilities;
    
    public static int[] soundTypesVolume;
    public static ArrayList<SoundSource> sources;
            
    public static void init() {
        // Initialize OpenAL and clear the error bit.
        device = ALC10.alcOpenDevice((java.lang.CharSequence) null);
        context = ALC10.alcCreateContext(device, (int[]) null);
        ALC10.alcMakeContextCurrent(context);

        alcCapabilities = ALC.createCapabilities(device);
        alCapabilities = AL.createCapabilities(alcCapabilities);
        AL10.alGetError();
        
        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
        //AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
        AL11.alSpeedOfSound(34300f);
        
        if(alcCapabilities.ALC_SOFT_HRTF) Audio3DEffects.enableHRTF(device);
        else System.out.println("HRTF support doesnt found");
        
        //Audio3DEffects.init();
        sources = new ArrayList<>();
    }

    static float[] listenerPos = new float[]{0.0f, 0.0f, 0.0f};
    static float[] listenerSpeed = new float[]{0.0f, 0.0f, 0.0f};
    static float[] listenerOri = new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f};
    
    public static void setListener(Vector3D pos, Vector3D speed, float rotY) {
        listenerPos[0] = pos.x; listenerPos[1] = pos.y; listenerPos[2] = pos.z;
        
        listenerSpeed[0] = speed.x * 1000 / 50; 
        listenerSpeed[1] = speed.y * 1000 / 50;
        listenerSpeed[2] = speed.z * 1000 / 50;
        
        float orientationX = (float) -Math.sin(Math.toRadians(rotY));
        float orientationZ = (float) -Math.cos(Math.toRadians(rotY));

        listenerOri[0] = orientationX;
        listenerOri[2] = orientationZ;
        
        AL10.alListenerfv(AL10.AL_POSITION, listenerPos);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerSpeed);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOri);
    }
    
    public static void suspend() {
        ALC10.alcSuspendContext(context);
    }
    
    public static void process() {
        ALC10.alcProcessContext(context);
    }
    
    public static void close() {
        //Audio3DEffects.destroy();
        sources = null;
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
    
    public static void playMultiple(int[] sources) {
        AL10.alSourcePlayv(sources);
    }
    
    public static void stopMultiple(int[] sources) {
        AL10.alSourceStopv(sources);
    }
    
    public static void rewindMultiple(int[] sources) {
        AL10.alSourceRewindv(sources);
        AL10.alSourcePlayv(sources);
    }

    public static float getSoundTypeVolume(int type) {
        if(soundTypesVolume == null) return 1;
        
        return soundTypesVolume[type]/100f;
    }
}
