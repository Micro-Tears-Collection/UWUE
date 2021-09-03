#ifdef VERT
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec3 inNormal;

smooth out vec2 uv;
smooth out vec3 lightCol;

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

uniform float glow;

vec3 pointLight(vec4 pos, vec3 norm, int i) {
	Light light = lightsData[i];
	
	vec3 lpos = light.pos.xyz;
	vec3 lcol = light.col.rgb;
	vec3 ldir = light.spotDirCutoff.xyz;
	float lspotCutoff = light.spotDirCutoff.w;

	// Compute vector from surface to light position
	vec3 lightToVec = lpos - pos.xyz;
	float dist = length(lightToVec);
	lightToVec = lightToVec / dist;
	
	// Compute attenuation
	float attenuation = 1. / (0.0001 * 0.1 * dist * dist);
	
	if(lspotCutoff >= 0.) {
		//Spot light
		float spotDot = dot(-lightToVec, ldir);
		float spotAttenuation = 1.;
		
		if(spotDot < lspotCutoff) spotAttenuation = 0.;
		else spotAttenuation = 1.0 - min(1.0, (1.0 - spotDot) / (1.0 - lspotCutoff));
		
		attenuation *= spotAttenuation;
	}
	
	float ldot = max(0., dot(norm, lightToVec));
	
	//Specularity
	/*vec3 halfVector = normalize(lightToVec + (normalize(-pos.xyz)));
	float nDotHV = max(0.0, dot(norm, halfVector));
	
	ldot *= 1.0 + pow(nDotHV, 50.0) * 3.;*/
	
	return lcol * ldot * attenuation;
}

vec3 directionalLight(vec4 pos, vec3 norm, int i) {
	Light light = lightsData[i];
	
	vec3 ldir = light.pos.xyz;
	vec3 lcol = light.col.rgb;
	
	float ldot = max(0., dot(norm, -ldir));
	
	//Specularity
	/*vec3 halfVector = normalize(ldir + (normalize(-pos.xyz)));
	float nDotHV = max(0.0, dot(norm, halfVector));

	ldot *= 1.0 + pow(nDotHV, 50.0) * 3.;*/
	
	return lcol * ldot;
}

void main()
{
	vec4 lvPos = modelView * vec4(inPos, 1.);
	vec3 lvNormal = normalize((modelView * vec4(inNormal, 0.)).xyz);
	
	vec3 lightsSumm = ambientLight.rgb;
	
	for(int i=0; i<MAX_LIGHTS; i++) {
		if(lightsData[i].pos.w > 0.5) lightsSumm += pointLight(lvPos, lvNormal, i);
		else lightsSumm += directionalLight(lvPos, lvNormal, i);
	}
	
	lightCol = mix(lightsSumm, vec3(1.), glow);
	
	fogOut.rgb = fogColor.rgb;
	fogOut.a = mix(
		fogEndScale.x + lvPos.z * fogEndScale.y,
		lvPos.z * fogEndScale.x * 1.4427,
		fogColor.a
	);
	fogExp = fogColor.a;
	
    gl_Position = project * lvPos;
	uv = inUV + uvOffset;
}
#endif

#ifdef FRAG
out vec4 fragColor;

smooth in vec2 uv;
smooth in vec3 lightCol;

smooth in vec4 fogOut;
smooth in float fogExp;

uniform sampler2D texUnit0;
uniform float alphaThreshold;

void main() 
{
	vec4 tex = texture2D(texUnit0, uv);
	
	if(tex.a <= alphaThreshold) discard;
	
	tex.rgb *= lightCol;
	
	float fog;
	if(fogExp > 0.5) fog = exp2(fogOut.a);
	else fog = fogOut.a;
	
	tex.rgb = mix(fogOut.rgb, tex.rgb, clamp(fog, 0., 1.));
	
    fragColor = tex;
}
#endif