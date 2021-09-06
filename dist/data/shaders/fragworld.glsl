#define MAX_LIGHTS 8

struct Light {
	vec4 pos;
	vec4 col;
	vec4 spotDirCutoff;
};

layout(std140) uniform lights
{
	vec4 ambientLight;
	Light lightsData[MAX_LIGHTS];
};

#ifdef VERT
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 inTangent;

smooth out vec2 fragUV;
smooth out vec3 fragPos;
smooth out vec3 fragNorm;

#ifndef GLOW
smooth out vec3[MAX_LIGHTS] lightsDirs;
smooth out vec3[MAX_LIGHTS] lightsSpotDirs;
#endif

smooth out vec4 fogOut;
smooth out float fogExp;

layout(std140) uniform mats 
{
	mat4 modelView;
	mat4 project;
};

layout(std140) uniform fog 
{
	vec4 fogColor; //+linear / density
	vec2 fogEndScale;
};

uniform vec2 uvOffset;

void main()
{
	vec4 pos = modelView * vec4(inPos, 1.);
	vec3 norm = normalize((modelView * vec4(inNormal, 0.)).xyz);
	
    gl_Position = project * pos;
	fragUV = inUV + uvOffset;
	
	fogOut.rgb = fogColor.rgb;
	fogOut.a = mix(
		pos.z * fogEndScale.y + fogEndScale.x,
		pos.z * fogEndScale.x * 1.4427,
		fogColor.a
	);
	fogExp = fogColor.a;
	
	#ifndef GLOW
	#ifdef NORMALMAP
	vec3 T = normalize((modelView * vec4(inTangent, 0.)).xyz);
	T = normalize(T - dot(T, norm) * norm);
	
	mat3 TBN = transpose(mat3(T, cross(norm, T), norm));
	#else
	mat3 TBN = mat3(vec3(1., 0., 0.), vec3(0., 1., 0.), vec3(0., 0., 1.));
	#endif
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		Light light = lightsData[i];
		
		if(light.pos.w > 0.5) {
			//Point light
			lightsDirs[i] = TBN * (light.pos.xyz - pos.xyz);
			lightsSpotDirs[i] = TBN * light.spotDirCutoff.xyz;
		} else {
			lightsDirs[i] = normalize(TBN * -light.pos.xyz) * sqrt(100000.);
		}
	}
	
	fragPos.xyz = TBN * pos.xyz;
	#else
	fragPos = pos.xyz;
	#endif
	fragNorm = norm;
}
#endif

#ifdef FRAG
out vec4 fragColor;

smooth in vec2 fragUV;
smooth in vec3 fragPos;
smooth in vec3 fragNorm;

smooth in vec4 fogOut;
smooth in float fogExp;

uniform sampler2D albedoMap;
#ifdef NORMALMAP
uniform sampler2D normalMap;
#endif
uniform float alphaThreshold;

#ifndef GLOW
const float SRGB_GAMMA = 1.0 / 2.2;
const float SRGB_INVERSE_GAMMA = 2.2;
const float SRGB_ALPHA = 0.055;
const float SRGB_ALPHAPONE = 1.055;

// Converts a single linear channel to srgb
float linear_to_srgb(float channel) {
    if(channel <= 0.0031308)
        return 12.92 * channel;
    else
        return SRGB_ALPHAPONE * pow(channel, 1.0/2.4) - SRGB_ALPHA;
}

// Converts a single srgb channel to rgb
float srgb_to_linear(float channel) {
    if (channel <= 0.04045)
        return channel / 12.92;
    else
        return pow((channel + SRGB_ALPHA) / SRGB_ALPHAPONE, 2.4);
}

// Converts a linear rgb color to a srgb color (exact, not approximated)
vec3 rgb_to_srgb(vec3 rgb) {
    return vec3(
        linear_to_srgb(rgb.r),
        linear_to_srgb(rgb.g),
        linear_to_srgb(rgb.b)
    );
}

// Converts a srgb color to a linear rgb color (exact, not approximated)
vec3 srgb_to_rgb(vec3 srgb) {
    return vec3(
        srgb_to_linear(srgb.r),
        srgb_to_linear(srgb.g),
        srgb_to_linear(srgb.b)
    );
}

smooth in vec3[MAX_LIGHTS] lightsDirs;
smooth in vec3[MAX_LIGHTS] lightsSpotDirs;

float spotLight(vec3 lightVec, vec3 spot, float spotCutoff) {
	float spotDot = dot(-lightVec, spot);
	float oneMcutoff = 1.0 - spotCutoff;
	return clamp((spotDot - 1.0 + oneMcutoff) / oneMcutoff, 0.0, 1.0);
}

#ifdef SPECULAR
float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float roughness2     = roughness * roughness;
    float NdotH  = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;
	
    float nom    = roughness2;
    float denom  = (NdotH2 * (roughness2 - 1.0) + 1.0);
    denom        = 3.1415926535 * denom * denom;
	
    return nom / denom;
}
#endif

vec3 calcLight(vec3 norm, vec3 normalizedPos, int i) {
	vec3 lightVec = lightsDirs[i];
	
	float dist = length(lightVec);
	float invDist = 1.0 / dist;
	
	lightVec *= invDist;
	
	//Diffuse
	float NdotV = max(dot(norm, lightVec), 0.);
	vec3 lightCol = lightsData[i].col.rgb * NdotV * 100000. * invDist * invDist;
	
	//Spot
	float spotCutoff = lightsData[i].spotDirCutoff.w;
	if(spotCutoff >= 0.) lightCol *= spotLight(lightVec, lightsSpotDirs[i], spotCutoff);
	
	#ifdef SPECULAR
	vec3 halfVec = normalize(lightVec - normalizedPos);
	lightCol *= 1.0 + DistributionGGX(norm, halfVec, 0.2);
	#endif
	
	return lightCol;
}

#endif

void main()
{
	vec4 tex = texture2D(albedoMap, fragUV);
	
	if(tex.a <= alphaThreshold) discard;
	
	#ifndef GLOW
	vec3 lightsSumm = ambientLight.rgb;
	
	#ifdef NORMALMAP
	vec3 norm = texture2D(normalMap, fragUV).xyz * 2.0 - vec3(1.0);
	norm.z = sqrt(1. - dot(norm.xy, norm.xy));
	#else
	vec3 norm = normalize(fragNorm);
	#endif
	
	vec3 normalizedPos = normalize(fragPos);
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		lightsSumm += calcLight(norm, normalizedPos, i);
	}
	
	tex.rgb = rgb_to_srgb(max(srgb_to_rgb(tex.rgb) * lightsSumm, 0.0));
	#endif
	
	float fog;
	if(fogExp > 0.5) fog = exp2(fogOut.a);
	else fog = fogOut.a;
	
	tex.rgb = mix(fogOut.rgb, tex.rgb, clamp(fog, 0., 1.));
	
    fragColor = tex;
}
#endif