package code.audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.openal.SOFTHRTF;

/**
 *
 * @author Roman Lahin
 */
public class Audio3DEffects {
    
    static int auxEffectSlot, reverbEffect;
    
    //Env Size Diffus Room RoomHF RoomLF DecTm DcHF DcLF Refl RefDel Ref Pan Revb RevDel Rev Pan EchTm	EchDp ModTm ModDp AirAbs HFRef LFRef RRlOff FLAGS

    static float[] REVERB_PRESET =
            //{ 0.4287f, 1.0000f, 0.3162f, 0.0100f, 1.0000f, 0.3000f, 0.1000f, 1.0000f, 0.1215f, 0.0020f,  0.0000f, 0.0000f, 0.0000f , 0.1531f, 0.0300f,  0.0000f, 0.0000f, 0.0000f , 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
    //CARPETTEDHALLWAY
            { 1.0000f, 0.7800f, 0.1862f, 0.7079f, 0.8913f, 1.7900f, 1.1200f, 0.9100f, 0.2818f, 0.0460f, 0.0000f, 0.0000f, 0.0000f, 0.1995f, 0.0280f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
			//{ 1.0000f, 0.7800f, 0.3162f, 0.7079f, 0.8913f, 1.7900f, 1.1200f, 0.9100f, 0.2818f, 0.0460f, 0.0000f, 0.0000f, 0.0000f, 0.1995f, 0.0280f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
    //CITY_STREETS

            //{ 1.0000f, 0.6900f, 0.3162f, 0.7943f, 0.8913f, 3.2800f, 1.1700f, 0.9100f, 0.4467f, 0.0440f, 0.0000f, 0.0000f, 0.0000f, 0.2818f, 0.0240f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9966f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
    //CITY_ABANDONED
           // { 0.3645f, 0.5600f, 0.3162f, 0.7943f, 0.7079f, 1.7900f, 0.3800f, 0.2100f, 0.5012f, 0.0020f, 0.0000f, 0.0000f, 0.0000f, 1.2589f, 0.0060f, 0.0000f, 0.0000f, 0.0000f, 0.2020f, 0.0500f, 0.2500f, 0.0000f, 0.9886f, 13046.0000f, 163.3000f, 0.0000f, 0x1 };
    //DUSTYROOM
            //{ 0.3071f, 0.8000f, 0.3162f, 0.3162f, 1.0000f, 2.8100f, 0.1400f, 1.0000f, 1.6387f, 0.0140f, 0.0000f, 0.0000f, 0.0000f, 3.2471f, 0.0210f, 0.0000f, 0.0000f, 0.0000f, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 };
    //sewerpipe 
    
    static void init() {
        auxEffectSlot = createAuxEffectSlot();
        if(auxEffectSlot != 0) reverbEffect = createEffect(EXTEfx.AL_EFFECT_EAXREVERB);
        if(auxEffectSlot != 0 && reverbEffect != 0) {
            setupReverb(reverbEffect, REVERB_PRESET);
            EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbEffect);
        }
    }
    
    static void destroy() {
        EXTEfx.alAuxiliaryEffectSloti(auxEffectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, EXTEfx.AL_EFFECT_NULL);
        EXTEfx.alDeleteAuxiliaryEffectSlots(auxEffectSlot);
        EXTEfx.alDeleteEffects(reverbEffect);
    }
    
    static void enableHRTF(long device, boolean enable) {
        int num_hrtf = ALC10.alcGetInteger(device, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
		
        if(num_hrtf == 0) {
            System.out.println("No HRTFs found");
        } else {
            if(!SOFTHRTF.alcResetDeviceSOFT(device, new int[]{SOFTHRTF.ALC_HRTF_SOFT, enable?ALC10.ALC_TRUE:ALC10.ALC_FALSE, 0})) {
                System.out.format("Failed to reset openal device: %s\n", 
                        ALC10.alcGetString(device, ALC10.alcGetError(device)));
            }

            int hrtf_state = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_SOFT);
            if(hrtf_state == 0 && enable) {
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
        
        int i = 0;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DENSITY, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DIFFUSION, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_GAINHF, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_GAINLF, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DECAY_TIME, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_DECAY_LFRATIO, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_REFLECTIONS_DELAY, preset[i]); i++;
        EXTEfx.alEffectfv(effect, EXTEfx.AL_EAXREVERB_REFLECTIONS_PAN, 
                new float[]{preset[i], preset[i+1], preset[i+2]}); i+=3;
                
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, preset[i]); i++;
        EXTEfx.alEffectfv(effect, EXTEfx.AL_EAXREVERB_LATE_REVERB_PAN, 
                new float[]{preset[i], preset[i+1], preset[i+2]}); i+=3;
                
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_ECHO_TIME, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_ECHO_DEPTH, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_MODULATION_TIME, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_MODULATION_DEPTH, preset[i]); i++;
        
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_HFREFERENCE, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_LFREFERENCE, preset[i]); i++;
        EXTEfx.alEffectf(effect, EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, preset[i]); i++;
        EXTEfx.alEffecti(effect, EXTEfx.AL_EAXREVERB_DECAY_HFLIMIT, (int) preset[i]); i++;
        
        int error = AL10.alGetError();
        if(error != AL10.AL_NO_ERROR) System.out.println("Reverb initialization error: "+error);
    }
}
