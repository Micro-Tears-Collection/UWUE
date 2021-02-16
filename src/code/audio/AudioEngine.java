package code.audio;

import code.math.Vector3D;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.SOFTHRTF;

/**
 *
 * @author Roman Lahin
 */
public class AudioEngine {
    
    static long device;
    static long context;
    static ALCCapabilities alcCapabilities;
    static ALCapabilities alCapabilities;
    
    //static int auxEffectSlot, reverbEffect;
    
    //Env Size Diffus Room RoomHF RoomLF DecTm DcHF DcLF Refl RefDel Ref Pan Revb RevDel Rev Pan EchTm	EchDp ModTm ModDp AirAbs HFRef LFRef RRlOff FLAGS

    //CARPETTEDHALLWAY
    //static float[] REVERB_PRESET =
            //{ 0.4287f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 0.3000f, 0.1000f, 1.0000f, 0.1215f, 0.0020f,  0.0000f, 0.0000f, 0.0000f , 0.1531f, 0.0300f,  0.0000f, 0.0000f, 0.0000f , 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
     //{ 1.0000f, 0.7800f, 0.3162f, 0.7079f, 0.8913f, 1.7900f, 1.1200f, 0.9100f, 0.2818f, 0.0460f, 0.0000f, 0.0000f, 0.0000f, 0.1995f, 0.0280f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
    //CITY_STREETS

//{ 1.0000f, 0.6900f, 0.3162f, 0.7943f, 0.8913f, 3.2800f, 1.1700f, 0.9100f, 0.4467f, 0.0440f, 0.0000f, 0.0000f, 0.0000f, 0.2818f, 0.0240f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9966f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
//CITY_ABANDONED
//{ 0.3645f, 0.5600f, 0.3162f, 0.7943f, 0.7079f, 1.7900f, 0.3800f, 0.2100f, 0.5012f, 0.0020f, 0.0000f, 0.0000f, 0.0000f, 1.2589f, 0.0060f, 0.0000f, 0.0000f, 0.0000f, 0.2020f, 0.0500f, 0.2500f, 0.0000f, 0.9886f, 13046.0000f, 163.3000f, 0.0000f, 0x1 };
//DUSTYROOM
//{ 0.3071f, 0.8000f, 0.3162f, 0.3162f, 1.0000f, 2.8100f, 0.1400f, 1.0000f, 1.6387f, 0.0140f, 0.0000f, 0.0000f, 0.0000f, 3.2471f, 0.0210f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
//sewerpipe 
            
    public static void init() {
        // Initialize OpenAL and clear the error bit.
        device = ALC10.alcOpenDevice((java.lang.CharSequence) null);
        context = ALC10.alcCreateContext(device, (int[]) null);
        ALC10.alcMakeContextCurrent(context);

        alcCapabilities = ALC.createCapabilities(device);
        alCapabilities = AL.createCapabilities(alcCapabilities);
        AL10.alGetError();
        
        if(alcCapabilities.ALC_SOFT_HRTF) enableHRTF();
        else System.out.println("HRTF support doesnt found");
        
        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
        //AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
        AL11.alSpeedOfSound(34300f);
        
        /*auxEffectSlot = createAuxEffectSlot();
        if(auxEffectSlot != 0) reverbEffect = createEffect(EXTEfx.AL_EFFECT_REVERB);
        if(auxEffectSlot != 0 && reverbEffect != 0) {
            setupReverbOLD(reverbEffect, REVERB_PRESET);
            EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbEffect);
        }*/
    }
    
    private static void enableHRTF() {
        int num_hrtf = ALC10.alcGetInteger(device, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
        if(num_hrtf == 0) {
            System.out.println("No HRTFs found");
        } else {
            if(!SOFTHRTF.alcResetDeviceSOFT(device, new int[]{SOFTHRTF.ALC_HRTF_SOFT, ALC10.ALC_TRUE, 0})) {
                System.out.format("Failed to reset device: %s\n", 
                        ALC10.alcGetString(device, ALC10.alcGetError(device)));
            }

            int hrtf_state = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_SOFT);
            if(hrtf_state == 0) {
                System.out.format("HRTF not enabled!\n");
            }
        }
    }
    

    private static int createAuxEffectSlot() {
        // Clear AL Error state
        AL10.alGetError();

        // Generate an Auxiliary Effect Slot
        int slot = EXTEfx.alGenAuxiliaryEffectSlots();
        
        int error = AL10.alGetError();
        if(error != AL10.AL_NO_ERROR) {
            System.out.println("AuxEffectSlot initialization error: "+error);
            return 0;
        }

        return slot;
    }
    
    private static int createEffect(int eEffectType) {
        // Clear AL Error State
        AL10.alGetError();

        // Generate an Effect
        int effect = EXTEfx.alGenEffects();
        if(AL10.alGetError() == AL10.AL_NO_ERROR) {
            // Set the Effect Type
            EXTEfx.alEffecti(effect, EXTEfx.AL_EFFECT_TYPE, eEffectType);
            
            int error = AL10.alGetError();
            if(error == AL10.AL_NO_ERROR) return effect;
            else {
                EXTEfx.alDeleteEffects(effect);
                System.out.println("Effect initialization error: "+error);
            }
        }

        return 0;
    }

    private static void setupReverb(int effect, float[] preset) {
        AL10.alGetError();
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DENSITY, 0.79f);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DIFFUSION, 0.51f);
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAIN, 1);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAINHF, 1);
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_TIME, 1.82f);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_HFRATIO, 1.2f);
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_REFLECTIONS_GAIN, 0.124f);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_REFLECTIONS_DELAY, 0.08f);
                
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, 0.473f);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_LATE_REVERB_DELAY, 0.05f);
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, 0.898f);
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_ROOM_ROLLOFF_FACTOR, 0.16f);
        EXTEfx.alEffecti(effect, EXTEfx.AL_REVERB_DECAY_HFLIMIT, 0);
        
        int error = AL10.alGetError();
        if(error != AL10.AL_NO_ERROR) System.out.println("Reverb initialization error: "+error);
    }

    private static void setupReverbOLD(int effect, float[] preset) {
        AL10.alGetError();
        
        int i = 0;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DENSITY, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DIFFUSION, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAINHF, preset[i]); i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_GAINLF, preset[i]);*/ i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_TIME, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_HFRATIO, preset[i]); i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_LFRATIO, preset[i]);*/ i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_REFLECTIONS_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_REFLECTIONS_DELAY, preset[i]); i++;
        /*EXTEfx.alEffectfv(effect, EXTEfx.AL_EAXREVERB_REFLECTIONS_PAN, 
                new float[]{preset[i], preset[i+1], preset[i+2]});*/ i+=3;
                
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_LATE_REVERB_DELAY, preset[i]); i++;
        /*EXTEfx.alEffectfv(effect, EXTEfx.AL_EAXREVERB_LATE_REVERB_PAN, 
                new float[]{preset[i], preset[i+1], preset[i+2]});*/ i+=3;
                
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_ECHO_TIME, preset[i]);*/ i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_ECHO_DEPTH, preset[i]);*/ i++;
        
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_MODULATION_TIME, preset[i]);*/ i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_MODULATION_DEPTH, preset[i]);*/ i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, preset[i]); i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_HFREFERENCE, preset[i]);*/ i++;
        /*EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_LFREFERENCE, preset[i]);*/ i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_ROOM_ROLLOFF_FACTOR, preset[i]); i++;
        EXTEfx.alEffecti(effect, EXTEfx.AL_REVERB_DECAY_HFLIMIT, (int) preset[i]); i++;
        
        int error = AL10.alGetError();
        if(error != AL10.AL_NO_ERROR) System.out.println("Reverb initialization error: "+error);
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
        /*EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, EXTEfx.AL_EFFECT_NULL);
        EXTEfx.alDeleteAuxiliaryEffectSlots(auxEffectSlot);
        EXTEfx.alDeleteEffects(reverbEffect);*/
        
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
}
