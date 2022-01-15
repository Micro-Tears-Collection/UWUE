//LIGHT for light

#ifdef VERT
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inNormal;

smooth out vec2 fragUV;
#ifdef LIGHT
smooth out vec3 fragLight;
#endif

smooth out vec4 fogOut;
smooth out float fogExp;

uniform vec2 uvOffset;

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

#ifdef LIGHT

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

float spotLight(vec3 lightVec, vec3 spot, float spotCutoff) {
	float spotDot = dot(-lightVec, spot);
	float oneMcutoff = 1.0 - spotCutoff;
	return clamp((spotDot - 1.0 + oneMcutoff) / oneMcutoff, 0.0, 1.0);
}

vec3 calcLight(vec3 pos, vec3 norm, vec3 normalizedPos, int i) {
	Light light = lightsData[i];
	vec4 lightVec = light.pos;
	vec3 lightCol = light.col.rgb;
	
	if(lightVec.w > 0.5) {
		lightVec.xyz -= pos;
		float dist = length(lightVec);
		float invDist = 1.0 / dist;
		lightVec *= invDist;
		
		lightCol *= 100000. * invDist * invDist;
	
		//Spot
		float spotCutoff = light.spotDirCutoff.w;
		if(spotCutoff >= 0.) lightCol *= spotLight(lightVec.xyz, light.spotDirCutoff.xyz, spotCutoff);
	} else {
		lightVec.xyz *= -1;
	}
	
	//Diffuse
	float NdotV = max(dot(norm, lightVec.xyz), 0.);
	
	return lightCol * NdotV;
}

/*vec3 pow3(vec3 x, float y) {
	return vec3(pow(x.x, y), pow(x.y, y), pow(x.z, y));
}*/

#endif

void main()
{
	vec4 pos = modelView * vec4(inPos, 1.);
	vec3 norm = normalize((modelView * inNormal).xyz);
	
    gl_Position = project * pos;
	fragUV = inUV + uvOffset;
	
	fogOut.rgb = fogColor.rgb;
	fogOut.a = mix(
		pos.z * fogEndScale.y + fogEndScale.x,
		pos.z * fogEndScale.x * 1.4427,
		fogColor.a
	);
	fogExp = fogColor.a;
	
	#ifdef LIGHT
	vec3 lightsSumm = ambientLight.rgb;
	vec3 normalizedPos = normalize(pos.xyz);
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		lightsSumm += calcLight(pos.xyz, norm, normalizedPos, i);
	}
	
	fragLight = max(lightsSumm, 0.);
	#endif
}
#endif

#ifdef FRAG
out vec4 fragColor;

smooth in vec2 fragUV;
#ifdef LIGHT
smooth in vec3 fragLight;
#endif

smooth in vec4 fogOut;
smooth in float fogExp;

uniform sampler2D albedoMap;
uniform float alphaThreshold;

void main()
{
	vec4 tex = texture2D(albedoMap, fragUV);
	
	if(tex.a <= alphaThreshold) discard;
	
	#ifdef LIGHT
	tex.rgb *= fragLight.rgb;
	#endif
	
	float fog;
	if(fogExp > 0.5) fog = exp2(fogOut.a);
	else fog = fogOut.a;
	
	tex.rgb = mix(fogOut.rgb, tex.rgb, clamp(fog, 0., 1.));
	
    fragColor = tex;
}
#endif