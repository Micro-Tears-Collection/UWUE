package code.audio;

import code.math.Vector3D;
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
    
    public static void init() {
        // Initialize OpenAL and clear the error bit.
        device = ALC10.alcOpenDevice((java.lang.CharSequence) null);
        context = ALC10.alcCreateContext(device, (int[]) null);
        ALC10.alcMakeContextCurrent(context);

        alcCapabilities = ALC.createCapabilities(device);
        alCapabilities = AL.createCapabilities(alcCapabilities);
        //AL10.alGetError();
        
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE);
        AL11.alSpeedOfSound(34300f);
    }

    static float[] listenerPos = new float[]{0.0f, 0.0f, 0.0f};
    static float[] listenerSpeed = new float[]{0.0f, 0.0f, 0.0f};
    static float[] listenerOri = new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f};
    
    public static void setListener(Vector3D pos, Vector3D speed, float rotX, float rotY) {
        listenerPos[0] = pos.x; listenerPos[1] = pos.y; listenerPos[2] = pos.z;
        
        listenerSpeed[0] = speed.x * 1000 / 50; 
        listenerSpeed[1] = speed.y * 1000 / 50; 
        listenerSpeed[2] = speed.z * 1000 / 50;
        
        final float xa = 1f;
        float ya = xa * (float) Math.cos(Math.toRadians(rotX));
        float yaYDSin = ya * (float) -Math.sin(Math.toRadians(rotY));
        float yaYDCos = ya * (float) -Math.cos(Math.toRadians(rotY));

        listenerOri[0] = yaYDSin;
        listenerOri[1] = xa * (float) Math.sin(Math.toRadians(rotX));
        listenerOri[2] = yaYDCos;
        
        AL10.alListenerfv(AL10.AL_POSITION, listenerPos);
        AL10.alListenerfv(AL10.AL_VELOCITY, listenerSpeed);
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOri);
    }
    
    public static void close() {
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}
